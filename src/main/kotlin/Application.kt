package com.example

import com.example.booking.plugins.configureRouting
import com.example.database.initDatabase
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    initDatabase()
    install(ContentNegotiation) {
        json()
    }
    configureRouting()
}