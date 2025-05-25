package com.pheide.controller

import io.ktor.http.Cookie
import io.ktor.server.application.ApplicationCall

object Authenticator {

    private const val COOKIE_NAME = "auth_cookie"
    // TODO settings for top secret cookie value
    private const val COOKIE_VALUE = "cookie_val"


    fun authenticate(call: ApplicationCall, username: String, password: String): Boolean {
        // TODO settings for username and password
        val isValid = username == "admin" && password == "pass"
        if (isValid) {
            setCookie(call)
        }
        return isValid
    }

    fun isLoggedIn(call: ApplicationCall): Boolean {
        val cookieValue = call.request.cookies[COOKIE_NAME]

        return cookieValue != null && cookieValue == hashValue(COOKIE_VALUE)
    }

    private fun hashValue(value: String): String {
        return value.hashCode().toString()
    }

    private fun setCookie(call: ApplicationCall) {
        call.response.cookies.append(
            Cookie(
                name = COOKIE_NAME,
                value = hashValue(COOKIE_VALUE),
                path = "/"
            )
        )
    }
}
