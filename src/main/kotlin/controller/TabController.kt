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

    override suspend fun doAction(action: String?, params: Map<String, String?>) {
        when (action?.lowercase()) {
            "show" -> {
                // TODO error handling
                val pageId = params["page_id"]
                if (pageId == null) {respond("Missing page id"); return}
                val page = PageRepository().selectById(pageId.toInt())
                if (page == null) {respond("Page with id $pageId not found"); return}
                show(page, params["tab_id"]?.toIntOrNull())
            }
        }
    }

    suspend fun show(page: Page, tabId: Int? = null) {
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

        // If logged in, allow editing of content and aside
        if (Authenticator.isLoggedIn(call)) {
            val varsForEditing = mutableMapOf(
                "content_edit_action" to LinkBuilder.build("tab", "update", mapOf(
                    "page_id" to page.id.toString(),
                    "tab_id" to tab.id.toString(),
                )),
                "page_id" to page.id.toString(),
                "tab_id" to tab.id.toString(),
                "content" to tab.content,
                "aside" to tab.aside,
            )

            view.vars["content_edit"] = View("tab/content_edit.html", varsForEditing).render()
            view.vars["aside_edit"] = View("tab/aside_edit.html", varsForEditing).render()
        }

        respond(renderPage(view, page, tab))
    }

}
