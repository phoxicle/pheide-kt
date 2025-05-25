package com.pheide

import com.pheide.controller.Authenticator
import com.pheide.controller.ControllerFactory
import com.pheide.repository.DAL
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

val logger = org.slf4j.LoggerFactory.getLogger("Main")

fun main() {
    // Initialize the database connection
    // TODO check location
    DAL.connect()
//    DAL.clearTestData()
    DAL.createSchemaAndPopulateData()

    embeddedServer(Netty, port = 8080) {
        routing {
            staticFiles("/resources", File("public/static"))

            get("/") {
                // TODO look at proper routing
                val controllerName = call.request.queryParameters["controller"] ?: "page"
                val action = call.request.queryParameters["action"] ?: "show"
                val isLoggedIn = Authenticator.isLoggedIn(call)
                logger.info("Is logged in: $isLoggedIn")

                // Parameters to pass to controller
                val params = mapOf(
                    "page_id" to call.request.queryParameters["page_id"],
                    "tab_id" to call.request.queryParameters["tab_id"]
                )

                // TODO error handling
                val controller = ControllerFactory.get(controllerName, call)
                val responseText = controller?.doAction(action, params, isLoggedIn)
                    ?: "Controller or action not found"

                call.respondText(responseText, ContentType.Text.Html)
            }
            post("/") {
                val controllerName = call.request.queryParameters["controller"] ?: "auth"
                val action = call.request.queryParameters["action"] ?: "authenticate"
                val isLoggedIn = Authenticator.isLoggedIn(call)
                logger.info("Is logged in: $isLoggedIn")

                // Parameters to pass to controller
                val formParameters = call.receiveParameters()
                val params = mapOf(
                    "username" to formParameters["username"],
                    "password" to formParameters["password"]
                )

                val controller = ControllerFactory.get(controllerName, call)
                val responseText = controller?.doAction(action, params, isLoggedIn)
                    ?: "Controller or action not found"

                call.respondText(responseText, ContentType.Text.Html)
            }
        }
    }.start(wait = true)
}



