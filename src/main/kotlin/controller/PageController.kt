package com.pheide.controllers

class PageController : BaseController() {

    // TODO get rid of nullables
    override fun doAction(action: String?, params: Map<String, String?>, isAuthenticated: Boolean): String? {
        return when (action?.lowercase()) {
            "show" -> show(action, params, isAuthenticated)
            else -> null
        }
    }

    fun show(action: String, params: Map<String, String?>, isAuthenticated: Boolean): String {
        // TODO get the default tab from DB

        return TabController().show(action, params, isAuthenticated)
    }
}