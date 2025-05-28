package com.pheide.controller

import com.pheide.repository.PageRepository
import com.pheide.repository.TabRepository

object LinkBuilder {
    fun link(controller: String, action: String, params: Map<String, String?> = emptyMap()): String {
        val prettyPrefix = "/go"

        if (controller == "page" && action == "show"
            && params.containsKey("page_id") && params["page_id"] != null) {

            // TODO dependency injection, generally clean up
            val page = PageRepository().selectById(params["page_id"]!!.toInt())
            // TODO consider actually url encoding
            return "$prettyPrefix/${page?.title}".lowercase().replace(" ", "%20")

        } else if (controller == "tab" && action == "show"
            && params.containsKey("page_id") && params["page_id"] != null
            && params.containsKey("tab_id") && params["tab_id"] != null) {

            val page = PageRepository().selectById(params["page_id"]!!.toInt())
            val tab = TabRepository().selectById(params["tab_id"]!!.toInt())
            return "$prettyPrefix/${page?.title}/${tab?.title}".lowercase().replace(" ", "%20")
        }

        val paramString = params.entries.joinToString("&") { (key, value) ->
            "$key=$value"
        }
        return "/?controller=$controller&action=$action&$paramString"
    }
}
