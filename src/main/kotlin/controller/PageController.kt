package com.pheide.controller

import com.pheide.controller.Authenticator.verifyAccess
import com.pheide.controller.LinkBuilder.link
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
            "delete" -> delete(params["page_id"]!!.toInt())
            else -> null
        }
    }

    suspend fun show(pageId: Int? = null) {
        val page = if (pageId == null) {
            val defaultPageId = pageRepository.selectDefault()?.id
            // TODO error handling
            TabController(call).show(defaultPageId!!)
        } else {
            TabController(call).show(pageId)
        }
    }

    suspend fun delete(pageId: Int) {
        verifyAccess(call)
        pageRepository.delete(pageId)
        redirect(link("page", "show"))
    }
}
