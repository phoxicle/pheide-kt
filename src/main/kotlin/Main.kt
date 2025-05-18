package com.pheide

import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                val param = call.request.queryParameters["param"] ?: "No parameter provided"
                call.respondText("You sent: $param", ContentType.Text.Plain)
            }
        }
    }.start(wait = true)
}
