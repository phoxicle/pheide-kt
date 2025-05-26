package com.pheide.controller

import com.pheide.controller.Authenticator.verifyAccess
import com.pheide.controller.LinkBuilder.link
import com.pheide.repository.PageRepository
import com.pheide.repository.TabRepository
import com.pheide.view.View
import io.ktor.server.application.ApplicationCall
import org.slf4j.LoggerFactory

class PageController(
    call: ApplicationCall,
    private val pageRepository: PageRepository = PageRepository(),
    private val tabRepository: TabRepository = TabRepository()
) : BaseController(call) {

    private val logger = LoggerFactory.getLogger("PageController")

    // TODO deal with nullables
    override suspend fun doAction(action: String?, params: Map<String, String?>) {
        when (action?.lowercase()) {
            "show" -> show(params["page_id"]?.toIntOrNull())
            "create" -> create(params["header_css_id"]!!)
            "delete" -> delete(params["page_id"]!!.toInt())
            "update" -> update(
                params["page_id"]!!.toInt(),
                params["title"],
                params["is_default"] != null
            )
            "error" -> error(params["message"])

            else -> null
        }
    }

    suspend fun show(pageId: Int? = null) {
        if (pageId == null) {
            val defaultPageId = pageRepository.selectDefault()?.id
            if (defaultPageId == null) {
                error("No default page found")
            } else {
                TabController(call).show(defaultPageId)
            }
        } else {
            TabController(call).show(pageId)
        }
    }

    suspend fun create(headerCssId: String) {
        verifyAccess(call)
        val pageId = pageRepository.create(title=headerCssId, headerCssId=headerCssId, isDefault=false)
        redirect(link("page", "show", mapOf(
            "page_id" to pageId.toString()
        )))
    }

    suspend fun delete(pageId: Int) {
        verifyAccess(call)
        // TODO ought to be transaction
        tabRepository.deleteAllByPageId(pageId)
        pageRepository.delete(pageId)
        redirect(link("page", "show"))
    }

    suspend fun update(pageId: Int, title: String?, isDefault: Boolean?) {
        verifyAccess(call)
        pageRepository.update(pageId, title, isDefault)

        // If setting page to default, make sure no other pages are default
        if (isDefault != null && isDefault) {
            val pages = pageRepository.selectAll()
            pages.forEach { page ->
                if (pageId != page.id && page.isDefault) {
                    pageRepository.update(page.id, isDefault = false)
                }
            }
        }

        redirect(link("page", "show", mapOf(
            "page_id" to pageId.toString()
        )))
    }

    suspend fun error(message: String?) {
        val view = View("page/error.html", mutableMapOf(
            "message" to (message ?: "Unknown error")
        ))
        respond(renderPage(view))
    }
}
