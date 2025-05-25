package com.pheide.controller

import com.pheide.repository.PageRepository
import io.ktor.server.application.ApplicationCall
import org.slf4j.LoggerFactory

class PageController(
    call: ApplicationCall,
    private val pageRepository: PageRepository = PageRepository()
) : BaseController(call) {

    private val logger = LoggerFactory.getLogger("PageController")

    // TODO get rid of nullables
    override suspend fun doAction(action: String?, params: Map<String, String?>) {
        when (action?.lowercase()) {
            "show" -> show(params["page_id"]?.toIntOrNull())
            else -> null
        }
    }

    suspend fun show(pageId: Int? = null) {
        val page = if (pageId == null) {
            pageRepository.selectDefault()
        } else {
            pageRepository.selectById(pageId)
        } ?: throw NoSuchElementException("Page with id $pageId not found")

        // TODO redirect?
        TabController(call).show(page)
    }
}
