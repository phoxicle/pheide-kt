package com.pheide.controller

import io.ktor.server.routing.RoutingCall

// TODO sealed interface instead?
abstract class BaseController(private val call: RoutingCall) {

    abstract fun doAction(action: String?, params: Map<String, String?>, isLoggedIn: Boolean): String?

}

object ControllerFactory {
    fun get(controllerName: String, call: RoutingCall): BaseController? {
        return when (controllerName.lowercase()) {
            "page" -> PageController(call)
            "tab" -> TabController(call)
            "auth" -> AuthController(call)
            else -> null
        }
    }
}
