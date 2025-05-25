package com.pheide.controller

import com.pheide.view.View
import io.ktor.server.routing.RoutingCall

class AuthController(call: RoutingCall) : BaseController(call) {
    override fun doAction(action: String?, params: Map<String, String?>, isLoggedIn: Boolean): String? {
        return when (action?.lowercase()) {
            "login" -> login()
            else -> null
        }
    }

    fun login() : String {
        val view = View()
        return view.render("auth/login.html")
    }

}
