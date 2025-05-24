package com.pheide.controller

import com.pheide.repository.Page
import com.pheide.repository.PageRepository

class PageController : BaseController() {

    // TODO get rid of nullables
    override fun doAction(action: String?, params: Map<String, String?>): String? {
        return when (action?.lowercase()) {
            "show" -> show(params["page_id"]?.toIntOrNull())
            else -> null
        }
    }

    fun show(pageId: Int? = null): String {
        val pageRepository = PageRepository()
        val page = if (pageId == null) {
            pageRepository.selectDefault()
        } else {
            pageRepository.selectById(pageId)
        } ?: throw NoSuchElementException("Page with id $pageId not found")

        return TabController().show(page)
    }
}
