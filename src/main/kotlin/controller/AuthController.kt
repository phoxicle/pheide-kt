package com.pheide.controller

import com.pheide.view.View
import io.ktor.server.routing.RoutingCall

val logger = org.slf4j.LoggerFactory.getLogger("AuthController")

class AuthController(private val call: RoutingCall) : BaseController(call) {
    override fun doAction(action: String?, params: Map<String, String?>, isLoggedIn: Boolean): String? {
        return when (action?.lowercase()) {
            "login" -> login()
            "logout" -> logout()
            "authenticate" -> authenticate(params["username"], params["password"])
            else -> null
        }
    }

    fun login() : String {
        val view = View("auth/login.html")
        view.vars["action_link"] = LinkBuilder.build("auth", "authenticate")
        // TODO render page
        return view.render()
    }

    fun logout() : String {
        Authenticator.logout(call)
        return PageController(call).show(isLoggedIn = false)
    }

    fun authenticate(username: String?, password: String?): String {
        logger.debug("Authenticating username: $username")
        if (username == null || password == null) {
            // TODO consistent error handling
            throw Exception("Username and password must be provided")
        }

        val success = Authenticator.authenticate(call, username, password)

        return if (success) {
            PageController(call).show(isLoggedIn = true)
        } else {
            restricted()
        }
    }

    fun restricted() : String {
        val view = View("auth/restricted.html")
        // TODO render page
        return view.render()
    }

}
