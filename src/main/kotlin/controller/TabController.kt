package com.pheide.controller

import com.pheide.controller.Authenticator.verifyAccess
import com.pheide.repository.Page
import com.pheide.repository.PageRepository
import com.pheide.repository.TabRepository
import com.pheide.view.View
import io.ktor.server.application.ApplicationCall
import org.slf4j.LoggerFactory
import kotlin.text.toInt

class TabController(
    call: ApplicationCall,
    private val tabRepository: TabRepository = TabRepository()
) : BaseController(call) {

    private val logger = LoggerFactory.getLogger("TabController")

    override suspend fun doAction(action: String?, params: Map<String, String?>) {
        when (action?.lowercase()) {
            "show" -> {
                // TODO error handling
                show(params["page_id"]!!.toInt(), params["tab_id"]?.toIntOrNull())
            }
            "update" -> {
                update(params["page_id"]!!.toInt(), params["tab_id"]!!.toInt(), params["content"], params["aside"])
            }
            else -> null
        }
    }

    suspend fun show(pageId: Int, tabId: Int? = null) {
        // Retrieve Tab
        val tab = if (tabId == null) {
            tabRepository.selectDefault(pageId)
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
                "update_action" to LinkBuilder.build("tab", "update"),
                "page_id" to pageId.toString(),
                "tab_id" to tab.id.toString(),
                "content" to tab.content,
                "aside" to tab.aside,
            )

            view.vars["content_edit"] = View("tab/content_edit.html", varsForEditing).render()
            view.vars["aside_edit"] = View("tab/aside_edit.html", varsForEditing).render()
        }

        // TODO nullable/error handling...
        respond(renderPage(view, pageId, tab.id))
    }

    suspend fun update(pageId: Int, tabId: Int, content: String?, aside: String?) {
        verifyAccess(call)
        tabRepository.update(tabId, content, aside)
        redirect(LinkBuilder.build("tab", "show", mapOf("page_id" to pageId.toString(), "tab_id" to tabId.toString())))
    }

}
