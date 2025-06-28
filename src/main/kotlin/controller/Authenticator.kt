package com.pheide.controller

import io.ktor.http.Cookie
import io.ktor.server.application.ApplicationCall

object Authenticator {

    private const val COOKIE_NAME = "auth_cookie"

    fun authenticate(call: ApplicationCall, username: String, password: String): Boolean {
        // TODO don't allow default fallbacks here omg danger
        val adminUsername = System.getenv("ADMIN_USERNAME") ?: "admin"
        val adminPassword = System.getenv("ADMIN_PASSWORD") ?: "pass"
        val cookieVal = System.getenv("COOKIE_VALUE") ?: "cookie"
//        logger.info("Admin username: $adminUsername, password: $adminPassword")
//        logger.info("Given username: $username, password: $password")
        val isValid = username == adminUsername && password == adminPassword
        if (isValid) {
            setCookie(call, hashValue(cookieVal))
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
        val expectedCookieVal = System.getenv("COOKIE_VALUE") ?: "cookie"
        return cookieValue != null && cookieValue == hashValue(expectedCookieVal)
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
