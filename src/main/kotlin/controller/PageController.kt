package com.pheide.controller

import com.pheide.repository.PageRepository
import io.ktor.server.application.ApplicationCall

class PageController(
    call: ApplicationCall,
    private val pageRepository: PageRepository = PageRepository()
) : BaseController(call) {

    // TODO get rid of nullables
    override fun doAction(action: String?, params: Map<String, String?>, isLoggedIn: Boolean): String? {
        return when (action?.lowercase()) {
            "show" -> show(params["page_id"]?.toIntOrNull(), isLoggedIn)
            else -> null
        }
    }

    fun show(pageId: Int? = null, isLoggedIn: Boolean): String {
        val page = if (pageId == null) {
            pageRepository.selectDefault()
        } else {
            pageRepository.selectById(pageId)
        } ?: throw NoSuchElementException("Page with id $pageId not found")

        return TabController(call).show(page, isLoggedIn = isLoggedIn)
    }
}
