package com.pheide.controller

import com.pheide.repository.Page
import com.pheide.repository.PageRepository
import com.pheide.repository.Tab
import com.pheide.repository.TabRepository
import com.pheide.view.View
import io.ktor.server.application.ApplicationCall
import kotlin.collections.set

abstract class BaseController(protected val call: ApplicationCall) {

    private val pageRepository = PageRepository()
    private val tabRepository = TabRepository()

    abstract fun doAction(action: String?, params: Map<String, String?>, isLoggedIn: Boolean): String?

    fun renderPage(view: View, isLoggedIn: Boolean, page: Page? = null, tab: Tab? = null) : String {
        // Login/logout
        view.vars["auth_button"] = if (isLoggedIn) {
            View("auth/logout_button.html",
                mutableMapOf("action_link" to LinkBuilder.build("auth", "logout")))
                .render()
        } else {
            View("auth/login_button.html",
                mutableMapOf("action_link" to LinkBuilder.build("auth", "login")))
                .render()
        }

        // Header
        view.vars["page_title"] = page?.title ?: ""
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
        view.vars["tab_bar"] = if (page != null) {
            tabRepository
                .selectAllByPageId(page.id)
                .joinToString("") { otherTab ->
                    val v = if (otherTab.id == tab?.id) {
                        View("tab/active_tab.html")
                    } else {
                        View("tab/inactive_tab.html")
                    }
                    v.vars["tab_title"] = otherTab.title
                    // TODO link builder
                    v.vars["tab_link"] = LinkBuilder.build(
                        "tab", "show", mapOf(
                            "page_id" to page.id.toString(),
                            "tab_id" to otherTab.id.toString()
                        )
                    )
                    v.render()
                }
            } else { "" }

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
