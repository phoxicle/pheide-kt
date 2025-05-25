package com.pheide.controller

import com.pheide.repository.Page
import com.pheide.repository.PageRepository
import com.pheide.repository.TabRepository
import com.pheide.view.View
import io.ktor.server.application.ApplicationCall
import org.slf4j.LoggerFactory

class TabController(
    call: ApplicationCall,
    private val tabRepository: TabRepository = TabRepository()
) : BaseController(call) {

    private val logger = LoggerFactory.getLogger("TabController")

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
            tabRepository.selectDefault(page.id)
        } else {
            tabRepository.selectById(tabId)
        } ?: throw NoSuchElementException("Tab with id $tabId not found")

        val view = View("tab/show.html", mutableMapOf(
            "tab_title" to tab.title,
            "content" to tab.content,
            "aside" to tab.aside,
        ))

        return renderPage(view, isLoggedIn, page, tab)
    }


}
