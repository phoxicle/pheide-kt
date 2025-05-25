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

        val view = View()
        // Populate tab-level vars
        view.vars["tab_title"] = tab.title
        view.vars["content"] = tab.content
        view.vars["aside"] = tab.aside
        view.vars["page_title"] = page.title
        view.vars["page_css_id"] = page.headerCssId

        // Header vars
        val authButton = View()
        if (isLoggedIn) {
            // TODO move template inside View constructor
            authButton.vars["action_link"] = "/?controller=auth&action=logout"
            view.vars["auth_button"] = authButton.render("auth/logout_button.html")
        } else {
            authButton.vars["action_link"] = "/?controller=auth&action=login"
            view.vars["auth_button"] = authButton.render("auth/login_button.html")
        }

        view.vars["header_images"] = PageRepository()
            .selectAll()
            .joinToString("") { otherPage ->
                val v = View()
                v.vars["css_id"] = otherPage.headerCssId
                v.vars["title"] = otherPage.title
                // TODO link builder
                v.vars["link"] = "/?controller=page&action=show&page_id=${otherPage.id}"
                v.render("header_image.html")
            }

        // Tab bar vars
        view.vars["tab_bar"] = tabRepository
            .selectAllByPageId(page.id)
            .joinToString("") { otherTab ->
                val v = View()
                v.vars["tab_title"] = otherTab.title
                // TODO link builder
                v.vars["tab_link"] = "/?controller=tab&action=show&page_id=${page.id}&tab_id=${otherTab.id}"
                if (otherTab.id == tab.id) {
                    v.render("tab/active_tab.html")
                } else {
                    v.render("tab/inactive_tab.html")
                }
            }

        return view.renderPage("tab/show.html")
    }


}
