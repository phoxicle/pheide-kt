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
        // TODO error handling
        when (action?.lowercase()) {
            "show" -> show(params["page_id"]!!.toInt(), params["tab_id"]?.toIntOrNull())
            "update" -> update(
                params["page_id"]!!.toInt(),
                params["tab_id"]!!.toInt(),
                title = params["title"],
                content = params["content"],
                aside = params["aside"])
            "new" -> new(params["page_id"]!!.toInt())
            "create" -> create(params["page_id"]!!.toInt(), params["title"]!!)
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

            view.vars["content_edit"] = View("tab/partials/content_edit.html", varsForEditing).render()
            view.vars["aside_edit"] = View("tab/partials/aside_edit.html", varsForEditing).render()
        }

        // TODO nullable/error handling...
        respond(renderPage(view, pageId, tab.id))
    }

    suspend fun update(pageId: Int, tabId: Int, title: String?, content: String?, aside: String?) {
        verifyAccess(call)
        tabRepository.update(tabId, title, content, aside)
        redirect(LinkBuilder.build("tab", "show", mapOf("page_id" to pageId.toString(), "tab_id" to tabId.toString())))
    }

    suspend fun new(pageId: Int) {
        verifyAccess(call)

        // Same as tab show, except with a fake, new tab
        val view = View("tab/show.html")

        // TODO hide plus tab
        view.vars["new_tab"] = View("tab/partials/new_tab.html", mutableMapOf(
            "action_link" to LinkBuilder.build("tab", "create"),
            "page_id" to pageId.toString(),
        )).render()

        respond(renderPage(view, pageId))
    }

    suspend fun create(pageId: Int, title: String) {
        verifyAccess(call)
        val tabId = tabRepository.create(pageId, title)
        redirect(LinkBuilder.build("tab", "show", mapOf("page_id" to pageId.toString(), "tab_id" to tabId.toString())))
    }

}
