package com.pheide.controller

import com.pheide.view.Renderer

class TabController : BaseController() {

    override fun doAction(action: String?, params: Map<String, String?>, isAuthenticated: Boolean ): String? {
        when (action?.lowercase()) {
            "show" -> return show(action, params, isAuthenticated)
            else -> return null
        }
    }

    fun show(action: String, params: Map<String, String?>, isAuthenticated: Boolean): String {
//        return "Tab action: $action, Params: $params"
        val renderer = Renderer()
        val templateVars = mapOf(
            "content" to "hiiii"
        )

        return renderer.renderPage("tab/show.html", templateVars)

    }


}
