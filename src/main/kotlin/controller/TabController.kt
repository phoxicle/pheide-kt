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
        val tab = if (tabId == null) {
            TabRepository().selectDefault(page.id)
        } else {
            TabRepository().selectById(tabId)
        } ?: throw NoSuchElementException("Tab with id $tabId not found")

        val view = View("tab/show.html", mutableMapOf(
            "tab_title" to tab.title,
            "content" to tab.content,
            "aside" to tab.aside,
        ))

        return renderPage(view, isLoggedIn, page, tab)
    }


}
