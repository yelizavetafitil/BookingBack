package com.example.booking.plugins

import com.example.booking.models.RegistrationResponse
import com.example.booking.models.UserRegistration
import com.example.database.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/register") {
            try {
                val userRegistration = call.receive<UserRegistration>()
                println("Received registration data: $userRegistration")

                // Проверка на существование пользователя
                val existingUser = transaction {
                    Users.select { Users.phoneNumber eq userRegistration.phoneNumber }.singleOrNull()
                }

                if (existingUser != null) {
                    call.respond(HttpStatusCode.Conflict, "Пользователь с таким номером телефона уже существует.")
                    return@post
                }

                // Хэшируем пароль
                val hashedPassword = BCrypt.hashpw(userRegistration.password, BCrypt.gensalt())

                val userId = transaction {
                    Users.insert {
                        it[fullName] = userRegistration.fullName
                        it[phoneNumber] = userRegistration.phoneNumber
                        it[password] = hashedPassword
                    } get Users.id
                }

                val response = RegistrationResponse(userId)
                call.respond(HttpStatusCode.Created, response)
            } catch (e: ContentTransformationException) {
                println("Failed to parse JSON: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
            } catch (e: Exception) {
                println("Error: ${e.stackTraceToString()}")
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.localizedMessage}")
            }
        }
    }
}