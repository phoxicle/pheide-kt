package com.pheide.controller

import com.pheide.repository.Page
import com.pheide.repository.TabRepository
import com.pheide.view.View

class TabController : BaseController() {

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
        view.vars["page_title"] = page.title
        view.vars["tab_title"] = tab.title
        view.vars["content"] = tab.content
        view.vars["aside"] = tab.aside

        return view.renderPage("tab/show.html")
    }


}
