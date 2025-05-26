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
import org.slf4j.LoggerFactory
import kotlin.collections.set

enum class HeaderCssId {
    BOOKS,
    BOTTLE,
    LAPTOP,
    MILK,
    MILL,
    NOTEBOOK,
    PURSE,
    SCISSORS,
    // TOOLS // Tools image is corrupted
}

abstract class BaseController(
    protected val call: ApplicationCall,
    private val pageRepository: PageRepository = PageRepository(),
    private val tabRepository: TabRepository = TabRepository()) {

    private val logger = LoggerFactory.getLogger("BaseController")

    abstract suspend fun doAction(action: String?, params: Map<String, String?>)

    suspend fun respond(responseText: String) {
        logger.info("Responding")
        call.respondText(responseText, ContentType.Text.Html)
    }

    suspend fun redirect(url: String) {
        logger.info("Redirecting to $url")
        call.respondRedirect(url)
    }

    private fun getAuthButtonHtml(): String {
        return if (Authenticator.isLoggedIn(call)) {
            View(
                "auth/partials/logout_button.html",
                mutableMapOf("action_link" to LinkBuilder.link("auth", "logout"))
            )
                .render()
        } else {
            View(
                "auth/partials/login_button.html",
                mutableMapOf("action_link" to LinkBuilder.link("auth", "login"))
            )
                .render()
        }
    }

    private fun getHeaderImagesHtml(): String {
        val pages = pageRepository.selectAll()

        val otherCssIds = HeaderCssId.entries.toMutableSet()
        val existingPagesHtml = pages
            .joinToString("") { otherPage ->
                // This cssId is already in use
                otherCssIds.remove(HeaderCssId.valueOf(otherPage.headerCssId.uppercase()))

                View("header_image.html", mutableMapOf(
                    "css_id" to otherPage.headerCssId,
                    "title" to otherPage.title,
                    "link" to link(
                        "page", "show", mapOf(
                            "page_id" to otherPage.id.toString())
                    )
                )).render()
            }

        // For creating new pages
        val otherPagesHtml = otherCssIds.joinToString("") { cssId ->
            View("header_image.html", mutableMapOf(
                "css_id" to cssId.toString().lowercase(),
                "title" to "+",
                "link" to link("page", "create", mapOf(
                        "header_css_id" to cssId.toString().lowercase()
                ))
            )).renderIf(isLoggedIn(call))
        }

        return existingPagesHtml + otherPagesHtml
    }

    private fun getPageTitleHtml(pageId: Int?): String {
        return if (pageId != null) {
            // TODO nullables / error handling
            val page = pageRepository.selectById(pageId)!!

            val deleteButton =  View("page/partials/delete_button.html", mutableMapOf(
                "action_link" to link("page", "delete", mapOf(
                    "page_id" to pageId.toString()
                ))
            )).renderIf(isLoggedIn(call))

            val titleEdit = View("page/partials/title_edit.html", mutableMapOf(
                "action_link" to link("page", "update"),
                "page_id" to pageId.toString(),
                "page_title" to page.title,
                "is_default_checked" to if (page.isDefault) "checked" else ""
            )).renderIf(isLoggedIn(call))

            View("page/partials/page_title.html", mutableMapOf(
                "page_title" to page.title,
                "page_delete_button" to deleteButton,
                "page_title_edit" to titleEdit
            )).render()

        } else {
            ""
        }
    }

    private fun getTabBarHtml(pageId: Int?, tabId: Int?): String {
        return if (pageId != null) {
            tabRepository
                .selectAllByPageId(pageId)
                .joinToString("") { otherTab ->
                    val v = if (otherTab.id == tabId) {
                        val deleteButton = View(
                            "tab/partials/delete_button.html", mutableMapOf(
                                "action_link" to link(
                                    "tab", "delete", mapOf(
                                        "page_id" to pageId.toString(),
                                        "tab_id" to tabId.toString()
                                    )
                                )
                            )
                        ).renderIf(isLoggedIn(call))
                        val shiftLeftButton = View(
                            "tab/partials/shift_left_button.html", mutableMapOf(
                                "action_link" to link(
                                    "tab", "shift", mapOf(
                                        "page_id" to pageId.toString(),
                                        "tab_id" to tabId.toString(),
                                        "direction" to "left"
                                    )
                                )
                            )
                        ).renderIf(isLoggedIn(call))
                        val shiftRightButton = View(
                            "tab/partials/shift_right_button.html", mutableMapOf(
                                "action_link" to link(
                                    "tab", "shift", mapOf(
                                        "page_id" to pageId.toString(),
                                        "tab_id" to tabId.toString(),
                                        "direction" to "right"
                                    )
                                )
                            )
                        ).renderIf(isLoggedIn(call))
                        val tabTitleEdit = View(
                            "tab/partials/tab_title_edit.html", mutableMapOf(
                                "action_link" to link("tab", "update"),
                                "page_id" to pageId.toString(),
                                "tab_id" to tabId.toString(),
                                "title" to otherTab.title,
                            )
                        ).renderIf(isLoggedIn(call))
                        View(
                            "tab/partials/active_tab.html", mutableMapOf(
                                "delete_button" to deleteButton,
                                "shift_left_button" to shiftLeftButton,
                                "shift_right_button" to shiftRightButton,
                                "tab_title_edit" to tabTitleEdit
                            )
                        )
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
        } else {
            ""
        }
    }

    fun renderPage(view: View, pageId: Int? = null, tabId: Int? = null): String {

        view.vars["auth_button"] = getAuthButtonHtml()
        view.vars["page_title"] = getPageTitleHtml(pageId)
        view.vars["header_images"] = getHeaderImagesHtml()
        view.vars["tab_bar"] = getTabBarHtml(pageId, tabId)

        // TODO probably goes inside the above
        // If logged in, add to the tab bar
        if (pageId != null) {
            view.vars["plus_tab"] = View("tab/partials/plus_tab.html", mutableMapOf(
                "action_link" to LinkBuilder.link("tab", "new", mapOf(
                    "page_id" to pageId.toString()
                ))
            )).renderIf(isLoggedIn(call) && view.vars["new_tab"].isNullOrEmpty())
        }

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
