package com.pheide.controller

import com.pheide.controller.Authenticator.isLoggedIn
import com.pheide.controller.Authenticator.verifyAccess
import com.pheide.controller.LinkBuilder.link
import com.pheide.repository.TabRepository
import com.pheide.view.View
import io.ktor.server.application.ApplicationCall
import org.slf4j.LoggerFactory
import java.nio.file.Files.delete
import kotlin.text.toInt

class TabController(
    call: ApplicationCall,
    private val tabRepository: TabRepository = TabRepository()
) : BaseController(call) {

    enum class Direction {
        LEFT, RIGHT
    }

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
                aside = params["aside"]
            )

            "new" -> new(params["page_id"]!!.toInt())
            "create" -> create(params["page_id"]!!.toInt(), params["title"]!!)
            "delete" -> delete(params["page_id"]!!.toInt(), params["tab_id"]!!.toInt())
            "shift" -> shift(
                params["page_id"]!!.toInt(),
                params["tab_id"]!!.toInt(),
                Direction.valueOf(params["direction"]!!.uppercase())
            )

            else -> null
        }
    }

    suspend fun show(pageId: Int, tabId: Int? = null) {
        val view = View("tab/show.html")

        // Retrieve Tab
        val tab = if (tabId == null) {
            tabRepository.selectDefault(pageId)
        } else {
            tabRepository.selectById(tabId)
        }

        if (tab != null) {
            view.vars["tab_title"] = tab.title
            view.vars["content"] = tab.content
            view.vars["aside"] = tab.aside

            // If logged in, allow editing of content and aside
            val varsForEditing = mutableMapOf(
                "update_action" to LinkBuilder.link("tab", "update"),
                "page_id" to pageId.toString(),
                "tab_id" to tab.id.toString(),
                "content" to tab.content,
                "aside" to tab.aside,
            )
            view.vars["content_edit"] = View("tab/partials/content_edit.html", varsForEditing)
                .renderIf(isLoggedIn(call))
            view.vars["aside_edit"] = View("tab/partials/aside_edit.html", varsForEditing)
                .renderIf(isLoggedIn(call))

            respond(renderPage(view, pageId, tab.id))
        } else {
            respond(renderPage(view, pageId))
        }
    }

    suspend fun update(pageId: Int, tabId: Int, title: String?, content: String?, aside: String?) {
        verifyAccess(call)
        tabRepository.update(tabId, title, content, aside)
        redirect(LinkBuilder.link("tab", "show", mapOf("page_id" to pageId.toString(), "tab_id" to tabId.toString())))
    }

    suspend fun new(pageId: Int) {
        verifyAccess(call)

        // Same as tab show, except with a fake, new tab
        val view = View("tab/show.html")

        view.vars["new_tab"] = View(
            "tab/partials/new_tab.html", mutableMapOf(
                "action_link" to LinkBuilder.link("tab", "create"),
                "page_id" to pageId.toString(),
            )
        ).renderIf(isLoggedIn(call))

        respond(renderPage(view, pageId))
    }

    suspend fun create(pageId: Int, title: String) {
        verifyAccess(call)
        val tabId = tabRepository.create(pageId, title)
        redirect(LinkBuilder.link("tab", "show", mapOf("page_id" to pageId.toString(), "tab_id" to tabId.toString())))
    }

    suspend fun delete(pageId: Int, tabId: Int) {
        verifyAccess(call)
        val tabId = tabRepository.delete(tabId)
        redirect(LinkBuilder.link("tab", "show", mapOf("page_id" to pageId.toString(), "tab_id" to tabId.toString())))
    }


    suspend fun shift(pageId: Int, tabId: Int, direction: Direction) {
        verifyAccess(call)

        val tabs = tabRepository.selectAllByPageId(pageId).toMutableList()
        val currentIndex = tabs.indexOfFirst { it.id == tabId }
        val thisTab = tabs[currentIndex]

        logger.debug("Attempt to shift: (current: $currentIndex, direction: $direction)")

        val targetIndex = when {
            (direction == Direction.LEFT && currentIndex > 0) -> currentIndex - 1
            (direction == Direction.RIGHT && currentIndex < tabs.size - 1) -> currentIndex + 1
            else -> null
        }

        // If a new targetIndex was found, swap the tabs
        if (targetIndex != null) {
            val otherTab = tabs[targetIndex]
            tabs[targetIndex] = thisTab
            tabs[currentIndex] = otherTab

            // Re-save each tab with new position as sorting
            tabs.forEachIndexed { index, tab ->
                tabRepository.update(tab.id, sorting = index)
            }
        } else {
            logger.info("Tab not shiftable in given direction (current: $currentIndex, direction: $direction)")
        }

        redirect(
            link(
                "tab", "show", mapOf(
                    "page_id" to pageId.toString(),
                    "tab_id" to tabId.toString()
                )
            )
        )
    }
}
