package com.pheide.controller

// TODO more modern kt approach?
class ControllerFactory {
    fun get(controllerName: String): BaseController? {
        return when (controllerName.lowercase()) {
            "page" -> PageController()
            else -> null
        }
    }
}
