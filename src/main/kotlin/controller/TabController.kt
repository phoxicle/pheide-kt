package com.pheide.controller

import com.pheide.repository.Page
import com.pheide.repository.PageRepository
import com.pheide.repository.TabRepository
import com.pheide.view.View
import org.slf4j.LoggerFactory

class TabController : BaseController() {

    private val logger = LoggerFactory.getLogger(TabController::class.java)

    override fun doAction(action: String?, params: Map<String, String?>): String? {
//        when (action?.lowercase()) {
//            // TODO validation
//            "show" -> return show(null, action, params, isAuthenticated, null)
//            else -> return null
//        }
        logger.info("TabController.doAction: action=$action, params=$params")
        // TODO
        return null
    }

    fun show(page: Page, tabId: Int? = null): String {
        // Retrieve Tab
        val tabRepository = TabRepository()
        val tab = if (tabId == null) {
            tabRepository.selectByPageId(page.id)
        } else {
            tabRepository.selectById(tabId)
        } ?: throw NoSuchElementException("Tab with id $tabId not found")

        // TODO decide where to populate all of these

        val view = View()
        // Populate tab-level vars
        view.vars["tab_title"] = tab.title
        view.vars["content"] = tab.content
        view.vars["aside"] = tab.aside

        // TODO move this elsewhere?
        // Populate page-level view vars
        view.vars["page_title"] = page.title
        view.vars["page_css_id"] = page.headerCssId

        // Header vars
        val headerImagesHtml = PageRepository()
            .selectAll()
            .joinToString("") { page ->
                val headerImageView = View()
                headerImageView.vars["css_id"] = page.headerCssId
                headerImageView.vars["title"] = page.title
                // TODO link builder
                headerImageView.vars["link"] = "/?controller=page&action=show&page_id=${page.id}"
                headerImageView.render("header_image.html")
            }
        view.vars["header_images"] = headerImagesHtml


        return view.renderPage("tab/show.html")
    }


}
