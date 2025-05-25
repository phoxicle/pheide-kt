package com.pheide.controller

import com.pheide.repository.Page
import com.pheide.repository.PageRepository
import com.pheide.repository.TabRepository
import com.pheide.view.View
import io.ktor.server.routing.RoutingCall
import org.slf4j.LoggerFactory

class TabController(private val call: RoutingCall) : BaseController(call) {

    private val logger = LoggerFactory.getLogger(TabController::class.java)

    override fun doAction(action: String?, params: Map<String, String?>, isLoggedIn: Boolean): String? {
        when (action?.lowercase()) {
            "show" -> {
                val pageId = params["page_id"] ?: return "Page id not set"
                val page = PageRepository().selectById(pageId.toInt()) ?: return "Page not found"
                return show(page, params["tab_id"]?.toIntOrNull(), isLoggedIn)
            }
            else -> return null
        }
    }

    fun show(page: Page, tabId: Int? = null, isLoggedIn: Boolean): String {
        // Retrieve Tab
        val tabRepository = TabRepository()
        val tab = if (tabId == null) {
            tabRepository.selectDefault(page.id)
        } else {
            tabRepository.selectById(tabId)
        } ?: throw NoSuchElementException("Tab with id $tabId not found")

        // TODO decide where to populate all of these

        val view = View("tab/show.html")
        // Populate tab-level vars
        view.vars["tab_title"] = tab.title
        view.vars["content"] = tab.content
        view.vars["aside"] = tab.aside
        view.vars["page_title"] = page.title
        view.vars["page_css_id"] = page.headerCssId

        // Header vars
        view.vars["auth_button"] = if (isLoggedIn) {
            View("auth/logout_button.html",
                mutableMapOf("action_link" to LinkBuilder.build("auth", "logout")))
                .render()
        } else {
            View("auth/login_button.html",
                mutableMapOf("action_link" to LinkBuilder.build("auth", "login")))
                .render()
        }

        view.vars["header_images"] = PageRepository()
            .selectAll()
            .joinToString("") { otherPage ->
                val v = View("header_image.html")
                v.vars["css_id"] = otherPage.headerCssId
                v.vars["title"] = otherPage.title
                v.vars["link"] = LinkBuilder.build("page", "show",mapOf(
                    "page_id" to otherPage.id.toString()))
                v.render()
            }

        // Tab bar vars
        view.vars["tab_bar"] = tabRepository
            .selectAllByPageId(page.id)
            .joinToString("") { otherTab ->
                val v = if (otherTab.id == tab.id) {
                    View("tab/active_tab.html")
                } else {
                    View("tab/inactive_tab.html")
                }
                v.vars["tab_title"] = otherTab.title
                // TODO link builder
                v.vars["tab_link"] = LinkBuilder.build("tab", "show", mapOf(
                    "page_id" to page.id.toString(),
                    "tab_id" to otherTab.id.toString()))
                v.render()
            }

        return view.renderPage()
    }


}
