package com.pheide.controller

import com.pheide.repository.Page
import com.pheide.repository.PageRepository
import com.pheide.repository.Tab
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
            View("auth/logout_button.html",
                mutableMapOf("action_link" to LinkBuilder.build("auth", "logout")))
                .render()
        } else {
            View("auth/login_button.html",
                mutableMapOf("action_link" to LinkBuilder.build("auth", "login")))
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
                v.vars["link"] = LinkBuilder.build("page", "show",mapOf(
                    "page_id" to otherPage.id.toString()))
                v.render()
            }

        // Tab bar
        view.vars["tab_bar"] = if (pageId != null) {
            tabRepository
                .selectAllByPageId(pageId)
                .joinToString("") { otherTab ->
                    val v = if (otherTab.id == tabId) {
                        View("tab/active_tab.html")
                    } else {
                        View("tab/inactive_tab.html")
                    }
                    v.vars["tab_title"] = otherTab.title
                    v.vars["tab_link"] = LinkBuilder.build(
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
        if (pageId != null && Authenticator.isLoggedIn(call)) {
            view.vars["plus_tab"] = View("tab/plus_tab.html", mutableMapOf(
                "action_link" to LinkBuilder.build("tab", "new", mapOf(
                    "page_id" to pageId.toString()
                ))
            )).render()
        } else {
            view.vars["plus_tab"] = ""
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
