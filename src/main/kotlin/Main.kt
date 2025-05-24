package com.pheide

import com.pheide.controller.ControllerFactory
import com.pheide.repository.DAL
import com.pheide.repository.PageTable
import com.pheide.repository.TabTable
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun main() {
    // Initialize the database connection
    // TODO check location
    DAL.connect()
    DAL.createSchemaAndPopulateData()

    embeddedServer(Netty, port = 8080) {
        routing {
            staticFiles("/resources", File("public/static"))

            get("/") {
                // TODO look at proper routing
                var controllerName = call.request.queryParameters["controller"]
                var action = call.request.queryParameters["action"]
                val pageId = call.request.queryParameters["page_id"]
                val tabId = call.request.queryParameters["tab_id"]
                val username = call.request.queryParameters["username"]
                val password = call.request.queryParameters["password"]

                // Parameters to pass to controller
                val params = mapOf(
                    "page_id" to pageId,
                    "tab_id" to tabId,
                    "username" to username,
                    "password" to password
                )

                // Default action is page::show
                if (controllerName.isNullOrEmpty()) {
                    controllerName = "page"
                    action = "show"
                }

                // Simulate calling a controller's action
                val factory = ControllerFactory()
                val controller = factory.get(controllerName)
                val responseText = controller?.doAction(action, params)
                    ?: "Controller or action not found"

                call.respondText(responseText, ContentType.Text.Html)
            }
        }
    }.start(wait = true)
}



