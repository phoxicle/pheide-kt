package com.pheide.controller

import com.pheide.controller.Authenticator.isLoggedIn
import com.pheide.controller.LinkBuilder.link
import com.pheide.repository.PageRepository
import com.pheide.repository.TabRepository
import com.pheide.view.View
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import kotlin.collections.set

abstract class BaseController(
    protected val call: ApplicationCall,
    private val pageRepository: PageRepository = PageRepository(),
    private val tabRepository: TabRepository = TabRepository()) {

    abstract suspend fun doAction(action: String?, params: Map<String, String?>)

    suspend fun respond(responseText: String) {
        logger.info("Responding")
        call.respondText(responseText, ContentType.Text.Html)
    }

    suspend fun redirect(url: String) {
        logger.info("Redirecting to $url")
        call.respondRedirect(url)
    }

    fun renderPage(view: View, pageId: Int? = null, tabId: Int? = null) : String {

        // Login/logout
        view.vars["auth_button"] = if (Authenticator.isLoggedIn(call)) {
            View("auth/partials/logout_button.html",
                mutableMapOf("action_link" to LinkBuilder.link("auth", "logout")))
                .render()
        } else {
            View("auth/partials/login_button.html",
                mutableMapOf("action_link" to LinkBuilder.link("auth", "login")))
                .render()
        }

        // Header
        // TODO nullable handling
        view.vars["page_title"] = if (pageId != null) {
            pageRepository.selectById(pageId)?.title ?: ""
        } else {
            ""
        }
        view.vars["header_images"] = pageRepository
            .selectAll()
            .joinToString("") { otherPage ->
                val v = View("header_image.html")
                v.vars["css_id"] = otherPage.headerCssId
                v.vars["title"] = otherPage.title
                v.vars["link"] = LinkBuilder.link("page", "show",mapOf(
                    "page_id" to otherPage.id.toString()))
                v.render()
            }

        // Tab bar
        view.vars["tab_bar"] = if (pageId != null) {
            tabRepository
                .selectAllByPageId(pageId)
                .joinToString("") { otherTab ->
                    val v = if (otherTab.id == tabId) {
                        val deleteButton = View("tab/partials/delete_button.html", mutableMapOf(
                            "action_link" to link("tab", "delete", mapOf(
                                "page_id" to pageId.toString(),
                                "tab_id" to tabId.toString()
                            ))
                        )).renderIf(isLoggedIn(call))
                        val shiftLeftButton = View("tab/partials/shift_left_button.html", mutableMapOf(
                            "action_link" to link("tab", "shift", mapOf(
                                "page_id" to pageId.toString(),
                                "tab_id" to tabId.toString(),
                                "direction" to "left"
                            ))
                        )).renderIf(isLoggedIn(call))
                        val shiftRightButton = View("tab/partials/shift_right_button.html", mutableMapOf(
                            "action_link" to link("tab", "shift", mapOf(
                                "page_id" to pageId.toString(),
                                "tab_id" to tabId.toString(),
                                "direction" to "right"
                            ))
                        )).renderIf(isLoggedIn(call))
                        View("tab/partials/active_tab.html", mutableMapOf(
                            "delete_button" to deleteButton,
                            "shift_left_button" to shiftLeftButton,
                            "shift_right_button" to shiftRightButton
                        ))
                    } else {
                        View("tab/partials/inactive_tab.html")
                    }
                    v.vars["tab_title"] = otherTab.title
                    v.vars["tab_link"] = LinkBuilder.link(
                        "tab", "show", mapOf(
                            "page_id" to pageId.toString(),
                            "tab_id" to otherTab.id.toString()
                        )
                    )
                    v.render()
                }
            } else { "" }

        // TODO probably goes inside the above
        // If logged in, add to the tab bar
        if (pageId != null) {
            view.vars["plus_tab"] = View("tab/partials/plus_tab.html", mutableMapOf(
                "action_link" to LinkBuilder.link("tab", "new", mapOf(
                    "page_id" to pageId.toString()
                ))
            )).renderIf(isLoggedIn(call) && view.vars["new_tab"].isNullOrEmpty())
        }

        // TODO when not logged in, hide vars like edits, new tab, etc.

        return view.renderPage()
    }
}

object ControllerFactory {
    fun get(controllerName: String, call: ApplicationCall): BaseController? {
        return when (controllerName.lowercase()) {
            "page" -> PageController(call)
            "tab" -> TabController(call)
            "auth" -> AuthController(call)
            else -> null
        }
    }
}
