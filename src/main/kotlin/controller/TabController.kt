package com.pheide.controller

import com.pheide.repository.Page
import com.pheide.repository.PageRepository
import com.pheide.repository.TabRepository
import com.pheide.view.View
import org.slf4j.LoggerFactory

class TabController : BaseController() {

    private val logger = LoggerFactory.getLogger(TabController::class.java)

    override fun doAction(action: String?, params: Map<String, String?>): String? {
        when (action?.lowercase()) {
            "show" -> {
                val pageId = params["page_id"] ?: return "Page id not set"
                val page = PageRepository().selectById(pageId.toInt()) ?: return "Page not found"
                return show(page, params["tab_id"]?.toIntOrNull())
            }
            else -> return null
        }
    }

    fun show(page: Page, tabId: Int? = null): String {
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
        val headerImagesHtml = PageRepository()
            .selectAll()
            .joinToString("") { page ->
                val v = View()
                v.vars["css_id"] = page.headerCssId
                v.vars["title"] = page.title
                // TODO link builder
                v.vars["link"] = "/?controller=page&action=show&page_id=${page.id}"
                v.render("header_image.html")
            }
        view.vars["header_images"] = headerImagesHtml

        // Tab bar vars
        val tabBarHtml = tabRepository
            .selectAllByPageId(page.id)
            .joinToString("") { tab ->
                val v = View()
                v.vars["tab_class"] = if (tab.id == tabId) "activeTab" else ""
                v.vars["tab_title"] = tab.title
                // TODO link builder
                v.vars["tab_link"] = "/?controller=tab&action=show&page_id=${page.id}&tab_id=${tab.id}"
                v.render("tab.html")
            }
        view.vars["tab_bar"] = tabBarHtml

        return view.renderPage("tab/show.html")
    }


}
