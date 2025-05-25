package com.pheide.controller

import com.pheide.view.View
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondRedirect
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("AuthController")

class AuthController(call: ApplicationCall) : BaseController(call) {

    override suspend fun doAction(action: String?, params: Map<String, String?>) {
        when (action?.lowercase()) {
            "login" -> login()
            "logout" -> logout()
            "authenticate" -> authenticate(params["username"], params["password"])
            "success" -> success()
            else -> null
        }
    }

    suspend fun login() {
        val view = View("auth/login.html")
        view.vars["action_link"] = LinkBuilder.build("auth", "authenticate")
        respond(renderPage(view))
    }

    suspend fun logout() {
        Authenticator.verifyAccess(call)
        Authenticator.logout(call)
        redirect(LinkBuilder.build("page", "show"))
    }

    suspend fun authenticate(username: String?, password: String?) {
        logger.debug("Authenticating username: $username")
        if (username == null || password == null) {
            // TODO consistent error handling
            throw Exception("Username and password must be provided")
        }

        val success = Authenticator.authenticate(call, username, password)
        return if (success) {
            redirect(LinkBuilder.build("auth", "success"))
        } else {
            redirect(LinkBuilder.build("auth", "restricted"))
        }
    }

    suspend fun restricted() {
        val view = View("auth/restricted.html")
        respond(renderPage(view))
    }

    suspend fun success() {
        Authenticator.verifyAccess(call)
        val view = View("auth/success.html")
        respond(renderPage(view))
    }

}
