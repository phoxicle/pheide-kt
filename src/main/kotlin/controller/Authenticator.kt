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
            setCookie(call, hashValue(COOKIE_VALUE))
        }
        return isValid
    }

    fun verifyAccess(call: ApplicationCall) {
        if (!isLoggedIn(call)) {
            throw Exception("User is not logged in")
        }
    }

    fun isLoggedIn(call: ApplicationCall): Boolean {
        val cookieValue = call.request.cookies[COOKIE_NAME]
        return cookieValue != null && cookieValue == hashValue(COOKIE_VALUE)
    }

    fun logout(call: ApplicationCall) {
        setCookie(call, "")
    }

    private fun hashValue(value: String): String {
        return value.hashCode().toString()
    }

    private fun setCookie(call: ApplicationCall, value: String) {
        call.response.cookies.append(
            Cookie(
                name = COOKIE_NAME,
                value = value,
                path = "/"
            )
        )
    }
}
