package com.example.booking.plugins

import com.example.booking.models.EnterpriseRegistration
import com.example.booking.models.EnterpriseRegistrationResponse
import com.example.booking.models.RegistrationResponse
import com.example.booking.models.UserRegistration
import com.example.database.Companies
import com.example.database.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Application.configureRouting() {
    routing {

        post("/register") {
            try {
                val userRegistration = call.receive<UserRegistration>()
                println("Received registration data: $userRegistration")

                when {
                    userRegistration.fullName.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "ФИО обязательно для заполнения")
                        return@post
                    }
                    userRegistration.fullName.length > 100 -> {
                        call.respond(HttpStatusCode.BadRequest, "ФИО слишком длинное (макс. 100 символов)")
                        return@post
                    }
                    !userRegistration.fullName.matches(Regex("^[\\p{L} .'-]+$")) -> {
                        call.respond(HttpStatusCode.BadRequest, "Недопустимые символы в ФИО")
                        return@post
                    }
                    userRegistration.phoneNumber.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "Номер телефона обязателен")
                        return@post
                    }
                    !userRegistration.phoneNumber.matches(Regex("^\\+?[0-9]{10,15}$")) -> {
                        call.respond(HttpStatusCode.BadRequest, "Неверный формат номера телефона")
                        return@post
                    }
                    userRegistration.password.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "Пароль обязателен")
                        return@post
                    }
                    userRegistration.password.length < 8 -> {
                        call.respond(HttpStatusCode.BadRequest, "Пароль слишком короткий (мин. 8 символов)")
                        return@post
                    }
                    userRegistration.password.length > 50 -> {
                        call.respond(HttpStatusCode.BadRequest, "Пароль слишком длинный (макс. 50 символов)")
                        return@post
                    }
                    !userRegistration.password.any { it.isDigit() } -> {
                        call.respond(HttpStatusCode.BadRequest, "Пароль должен содержать цифры")
                        return@post
                    }
                    !userRegistration.password.any { it.isLetter() } -> {
                        call.respond(HttpStatusCode.BadRequest, "Пароль должен содержать буквы")
                        return@post
                    }
                }

                val existingUser = transaction {
                    Users.select { Users.phoneNumber eq userRegistration.phoneNumber }.singleOrNull()
                }

                if (existingUser != null) {
                    call.respond(HttpStatusCode.Conflict, "Пользователь с таким номером телефона уже существует.")
                    return@post
                }

                val hashedPassword = BCrypt.hashpw(userRegistration.password, BCrypt.gensalt())

                val userId = transaction {
                    Users.insert {
                        it[fullName] = userRegistration.fullName.trim()
                        it[phoneNumber] = userRegistration.phoneNumber.trim()
                        it[password] = hashedPassword
                    } get Users.id
                }

                val response = RegistrationResponse(userId)
                call.respond(HttpStatusCode.Created, response)
            }  catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Неверный формат данных")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Внутренняя ошибка сервера: ${e.message}")
            }
        }

        post("/enterpriseRegistration") {
            try {
                val enterpriseRegistration = call.receive<EnterpriseRegistration>()
                println("Received enterprise registration data: $enterpriseRegistration")

                when {
                    enterpriseRegistration.enterpriseName.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "Имя предприятия обязательно для заполнения")
                        return@post
                    }
                    enterpriseRegistration.enterpriseName.length > 100 -> {
                        call.respond(HttpStatusCode.BadRequest, "Имя предприятия слишком длинное (макс. 100 символов)")
                        return@post
                    }
                    !enterpriseRegistration.enterpriseName.matches(Regex("^[\\p{L} .'-]+$")) -> {
                        call.respond(HttpStatusCode.BadRequest, "Недопустимые символы в имени предприятия")
                        return@post
                    }
                    enterpriseRegistration.city.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "Город обязателен")
                        return@post
                    }
                    enterpriseRegistration.city.length > 50 -> {
                        call.respond(HttpStatusCode.BadRequest, "Город слишком длинный (макс. 50 символов)")
                        return@post
                    }
                    !enterpriseRegistration.city.matches(Regex("^[\\p{L} .'-]+$")) -> {
                        call.respond(HttpStatusCode.BadRequest, "Недопустимые символы в имени города")
                        return@post
                    }
                    enterpriseRegistration.address.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "Адрес обязателен")
                        return@post
                    }
                    enterpriseRegistration.address.length > 100 -> {
                        call.respond(HttpStatusCode.BadRequest, "Адрес слишком длинный (макс. 100 символов)")
                        return@post
                    }
                    !enterpriseRegistration.address.matches(Regex("^[\\p{L} .'-]+$")) -> {
                        call.respond(HttpStatusCode.BadRequest, "Недопустимые символы в адресе")
                        return@post
                    }
                    enterpriseRegistration.enterprisePhoneNumber.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "Номер телефона предприятия обязателен")
                        return@post
                    }
                    !enterpriseRegistration.enterprisePhoneNumber.matches(Regex("^\\+?[0-9]{10,15}\$")) -> {
                        println("Enterprise Phone Number: '${enterpriseRegistration.enterprisePhoneNumber}'")
                        call.respond(HttpStatusCode.BadRequest, "Неверный формат номера телефона предприятия")
                        return@post
                    }
                }

                val existingCompany = transaction {
                    Companies.select { Companies.enterprisePhoneNumber eq enterpriseRegistration.enterprisePhoneNumber }.singleOrNull()
                }

                if (existingCompany != null) {
                    call.respond(HttpStatusCode.Conflict, "Компания с таким номером телефона уже существует.")
                    return@post
                }

                val idCompany = transaction {
                    Companies.insert {
                        it[enterpriseName] = enterpriseRegistration.enterpriseName.trim()
                        it[city] = enterpriseRegistration.city.trim()
                        it[address] = enterpriseRegistration.address.trim()
                        it[enterprisePhoneNumber] = enterpriseRegistration.enterprisePhoneNumber.trim()
                    } get Companies.idCompany
                }

                val response = EnterpriseRegistrationResponse(idCompany)
                call.respond(HttpStatusCode.Created, response)
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Неверный формат данных")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Внутренняя ошибка сервера: ${e.message}")
            }
        }

        delete("/enterpriseBack/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@delete
            }

            try {
                transaction {
                    Users.deleteWhere { Users.id eq id }
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Внутренняя ошибка сервера: ${e.message}")
            }
        }
    }
}