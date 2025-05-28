package com.pheide

import com.pheide.controller.Authenticator
import com.pheide.controller.ControllerFactory
import com.pheide.controller.PageController
import com.pheide.repository.DAL
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import java.io.File

val logger = LoggerFactory.getLogger("Main")

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
                logger.info("GET: $controllerName::$action, isAuthenticated: ${Authenticator.isLoggedIn(call)}")

                // Parameters to pass to controller
                val params = mapOf(
                    "page_id" to call.request.queryParameters["page_id"],
                    "tab_id" to call.request.queryParameters["tab_id"],
                    "direction" to call.request.queryParameters["direction"],
                    "header_css_id" to call.request.queryParameters["header_css_id"],
                )

                // TODO error handling
                val controller = ControllerFactory.get(controllerName, call)
                controller?.doAction(action, params)
                    ?: "Controller or action not found"
            }
            post("/") {
                val controllerName = call.request.queryParameters["controller"] ?: "auth"
                val action = call.request.queryParameters["action"] ?: "authenticate"
                logger.info("POST: $controllerName::$action, isAuthenticated: ${Authenticator.isLoggedIn(call)}")

                // Parameters to pass to controller
                val formParameters = call.receiveParameters()
                logger.debug("Form parameters: $formParameters")
                val params = mapOf(
                    "username" to formParameters["username"],
                    "password" to formParameters["password"],
                    "page_id" to formParameters["page_id"],
                    "tab_id" to formParameters["tab_id"],
                    "title" to formParameters["title"],
                    "content" to formParameters["content"],
                    "aside" to formParameters["aside"],
                    "is_default" to formParameters["is_default"],
                )
                logger.debug("Params: $params")

                val controller = ControllerFactory.get(controllerName, call)
                controller?.doAction(action, params)
                    ?: "Controller or action not found"
            }
            get("/go/{pageTitle}") {
                val pageTitle = call.parameters["pageTitle"]

                if (pageTitle != null) {
                    PageController(call).showByTitle(pageTitle)
                } else {
                    call.respondText("Page or Tab not found", status = io.ktor.http.HttpStatusCode.NotFound)
                }
            }
            get("/go/{pageTitle}/{tabTitle}") {
                val pageTitle = call.parameters["pageTitle"]
                val tabTitle = call.parameters["tabTitle"]

                if (pageTitle != null) {
                    PageController(call).showByTitle(pageTitle, tabTitle)
                } else {
                    call.respondText("Page or Tab not found", status = io.ktor.http.HttpStatusCode.NotFound)
                }
            }

        }
    }.start(wait = true)
}



