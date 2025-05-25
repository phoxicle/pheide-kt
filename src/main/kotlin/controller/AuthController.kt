package com.pheide.controller

import com.pheide.view.View
import io.ktor.server.application.ApplicationCall
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("AuthController")

class AuthController(call: ApplicationCall) : BaseController(call) {
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
        return renderPage(view, isLoggedIn = false)
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
        return if (success) success() else restricted()
    }

    fun restricted() : String {
        val view = View("auth/restricted.html")
        return renderPage(view, isLoggedIn = false)
    }

    fun success() : String {
        val view = View("auth/success.html")
        return renderPage(view, isLoggedIn = true)
    }

}
