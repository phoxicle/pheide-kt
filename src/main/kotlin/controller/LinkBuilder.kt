package com.pheide.controller

object LinkBuilder {
    fun build(controller: String, action: String, params: Map<String, String?> = emptyMap()): String {
        val paramString = params.entries.joinToString("&") { (key, value) ->
            "$key=$value"
        }
        return "/?controller=$controller&action=$action&$paramString"
    }
}
