package com.example.booking.plugins

import com.example.booking.models.*
import com.example.database.*
import com.example.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

                transaction{
                    val employee = Employees.select { Employees.employee_phone eq userRegistration.phoneNumber.trim() }.singleOrNull()


                    employee?.let {
                        Projects.insert {
                            it[access] = employee[Employees.access]
                            it[id_user] = userId
                            it[id_company] = employee[Employees.idCompany]
                        }
                    }
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

                transaction {
                    Projects.insert {
                        it[access] = "Админ"
                        it[id_user] = enterpriseRegistration.userId
                        it[id_company] = idCompany
                    }
                }

                call.respond(
                    HttpStatusCode.Created,
                    EnterpriseRegistrationResponse(idCompany)
                )
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

        post("/login") {
            try {
                val loginRequest = call.receive<LoginRequest>()
                println("Login attempt for: ${loginRequest.phoneNumber}")

                when {
                    loginRequest.fullName.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "ФИО обязательно для заполнения")
                        return@post
                    }
                    loginRequest.phoneNumber.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "Номер телефона обязателен")
                        return@post
                    }
                    !loginRequest.phoneNumber.matches(Regex("^\\+?[0-9]{10,15}$")) -> {
                        call.respond(HttpStatusCode.BadRequest, "Неверный формат номера телефона")
                        return@post
                    }
                    loginRequest.password.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "Пароль обязателен")
                        return@post
                    }
                }

                val (userId, storedHash) = transaction {
                    Users.select {
                        (Users.phoneNumber eq loginRequest.phoneNumber) and
                                (Users.fullName eq loginRequest.fullName)
                    }.map {
                        it[Users.id] to it[Users.password]
                    }.singleOrNull() ?: (null to null)
                }

                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Пользователь не найден")
                    return@post
                }

                if (!BCrypt.checkpw(loginRequest.password, storedHash)) {
                    call.respond(HttpStatusCode.BadRequest, "Неверный пароль")
                    return@post
                }

                println(userId.toString())

                val response = RegistrationResponse(userId)
                call.respond(HttpStatusCode.OK, response)

            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Неверный формат данных")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Ошибка сервера: ${e.message}")
            }
        }

        get("/userEnterprises/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid user ID")

            val enterprises = transaction {
                (Projects innerJoin Companies)
                    .slice(
                        Companies.idCompany,
                        Companies.enterpriseName,
                        Projects.access
                    )
                    .select { Projects.id_user eq userId }
                    .map {
                        EnterpriseData(
                            enterpriseId = it[Companies.idCompany],
                            enterpriseName = it[Companies.enterpriseName],
                            userId = userId,
                            access = it[Projects.access],
                        )
                    }
            }

            call.respond(enterprises)
        }

        get("/users/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid user ID")

            val userData = transaction {
                Users.select { Users.id eq userId }
                    .singleOrNull()
                    ?.let {
                        UserData(
                            fullName = it[Users.fullName],
                            phoneNumber = it[Users.phoneNumber],
                            password = ""
                        )
                    }
            } ?: throw NotFoundException("User not found")

            call.respond(userData)
        }

        put("/userUpdate/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid user ID")

            val updateData = call.receive<UserData>()

            transaction {
                Users.update({ Users.id eq userId }) {
                    it[fullName] = updateData.fullName
                }
            }

            call.respond(HttpStatusCode.OK)
        }

        get("/enterprise/{enterpriseId}") {
            val enterpriseId = call.parameters["enterpriseId"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")

            val Data = transaction {
                Companies.select { Companies.idCompany eq enterpriseId }
                    .singleOrNull()
                    ?.let {
                        Enterprise(
                            enterpriseName = it[Companies.enterpriseName],
                            city = it[Companies.city],
                            address = it[Companies.address],
                            enterprisePhoneNumber = it[Companies.enterprisePhoneNumber]
                        )
                    }
            } ?: throw NotFoundException("Companies not found")

            call.respond(Data)
        }

        put("/enterpriseUpdate/{enterpriseId}") {
            val enterpriseId = call.parameters["enterpriseId"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")

            val Data = call.receive<Enterprise>()

            transaction {
                Companies.update({ Companies.idCompany eq enterpriseId }) {
                    it[enterpriseName] = Data.enterpriseName
                    it[city] = Data.city
                    it[address] = Data.address
                }
            }

            call.respond(HttpStatusCode.OK)
        }

        delete("/enterpriseDelete/{enterpriseId}") {
            val id = call.parameters["enterpriseId"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@delete
            }

            try {
                transaction {
                    Companies.deleteWhere { Companies.idCompany eq id }
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Внутренняя ошибка сервера: ${e.message}")
            }
        }

        get("/enterpriseServices/{enterpriseId}") {
            val enterpriseId = call.parameters["enterpriseId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid enterprise ID")

            val services = transaction {
                Services.select { Services.idCompany eq enterpriseId }.map {
                    Service(
                        id = it[Services.idServices],
                        serviceName = it[Services.serviceName],
                        price = it[Services.price].toDouble(),
                        currency = it[Services.currency],
                        length = it[Services.length],
                        breakDuration = it[Services.breakDuration]
                    )
                }
            }

            call.respond(services)
        }

        post("/addService") {
            try {
                val addService = call.receive<ServiceData>()
                println("Received service data: $addService")

                when {
                    addService.serviceName.length > 100 -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ServiceAddResponse(
                                serviceId = 0,
                                success = false,
                                message = "Название слишком длинное (макс. 100 символов)"
                            )
                        )
                        return@post
                    }
                    !addService.serviceName.matches(Regex("^[\\p{L} .'-]+$")) -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ServiceAddResponse(
                                serviceId = 0,
                                success = false,
                                message = "Недопустимые символы в названии"
                            )
                        )
                        return@post
                    }
                }

                val insertedId = transaction {
                    Services.insert {
                        it[idCompany] = addService.enterpriseId
                        it[serviceName] = addService.serviceName.trim()
                        it[price] = addService.price.toBigDecimal()
                        it[currency] = addService.currency.trim()
                        it[length] = addService.length
                        it[breakDuration] = addService.breakDuration
                    } get Services.idServices
                }

                call.respond(
                    HttpStatusCode.Created,
                    ServiceAddResponse(
                        serviceId = insertedId,
                        success = true,
                        message = "Услуга успешно добавлена"
                    )
                )

            } catch (e: ContentTransformationException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ServiceAddResponse(
                        serviceId = 0,
                        success = false,
                        message = "Неверный формат данных: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ServiceAddResponse(
                        serviceId = 0,
                        success = false,
                        message = "Внутренняя ошибка сервера: ${e.message}"
                    )
                )
            }
        }

        get("/services/{serviceId}") {
            val serviceId = call.parameters["serviceId"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")

            val Data = transaction {
                Services.select { Services.idServices eq serviceId }
                    .singleOrNull()
                    ?.let {
                        ServiceEdit(
                            serviceName = it[Services.serviceName],
                            price = it[Services.price].toDouble(),
                            currency = it[Services.currency],
                            length = it[Services.length],
                            breakDuration = it[Services.breakDuration],
                        )
                    }
            } ?: throw NotFoundException("not found")

            call.respond(Data)
        }

        put("/updateService/{serviceId}") {
            val serviceId = call.parameters["serviceId"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")

            val Data = call.receive<ServiceEdit>()

            transaction {
                Services.update({ Services.idServices eq serviceId }) {
                    it[serviceName] = Data.serviceName
                    it[price] = Data.price.toBigDecimal()
                    it[currency] = Data.currency
                    it[length] = Data.length
                    it[breakDuration] = Data.breakDuration
                }
            }

            call.respond(HttpStatusCode.OK)
        }

        delete("/deleteService/{serviceId}") {
            val id = call.parameters["serviceId"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@delete
            }

            try {
                transaction {
                    Services.deleteWhere { Services.idServices eq id }
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Внутренняя ошибка сервера: ${e.message}")
            }
        }

        get("/enterpriseEmployee/{enterpriseId}") {
            val enterpriseId = call.parameters["enterpriseId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid enterprise ID")

            val services = transaction {
                Employees.select { Employees.idCompany eq enterpriseId }.map {
                    Employee(
                        id = it[Employees.id],
                        employee_fio = it[Employees.employee_fio],
                        employee_phone = it[Employees.employee_phone],
                        position = it[Employees.position],
                        access = it[Employees.access]
                    )
                }
            }

            call.respond(services)
        }

        post("/addEmployee") {
            try {
                val addEmployee = call.receive<EmployeeData>()
                println("Received enterprise registration data: $addEmployee")

                when {
                    addEmployee.employee_fio.length > 100 -> {
                        call.respond(HttpStatusCode.BadRequest, "ФИО слишком длинное (макс. 100 символов)")
                        return@post
                    }
                    !addEmployee.employee_fio.matches(Regex("^[\\p{L} .'-]+$")) -> {
                        call.respond(HttpStatusCode.BadRequest, "Недопустимые символы в ФИО")
                        return@post
                    }
                    addEmployee.position.length > 100 -> {
                        call.respond(HttpStatusCode.BadRequest, "Должность слишком длинное (макс. 100 символов)")
                        return@post
                    }
                    !addEmployee.position.matches(Regex("^[\\p{L} .'-]+$")) -> {
                        call.respond(HttpStatusCode.BadRequest, "Недопустимые символы в должности")
                        return@post
                    }
                    addEmployee.employee_phone.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "Номер телефона обязателен")
                        return@post
                    }
                    addEmployee.access.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, "Доступ обязателен")
                        return@post
                    }
                    !addEmployee.employee_phone.matches(Regex("^\\+?[0-9]{10,15}\$")) -> {
                        println("Enterprise Phone Number: '${addEmployee.employee_phone}'")
                        call.respond(HttpStatusCode.BadRequest, "Неверный формат номера телефона")
                        return@post
                    }
                }

                transaction {
                    Employees.insert {
                        it[idCompany] = addEmployee.enterpriseId
                        it[employee_fio] = addEmployee.employee_fio.trim()
                        it[employee_phone] = addEmployee.employee_phone.trim()
                        it[position] = addEmployee.position.trim()
                        it[access] = addEmployee.access.trim()
                    }

                    val user = Users.select { Users.phoneNumber eq addEmployee.employee_phone.trim() }.singleOrNull()

                    user?.let { existingUser ->
                        val userId = existingUser[Users.id]
                        Projects.insert {
                            it[access] = addEmployee.access.trim()
                            it[id_user] = userId
                            it[id_company] = addEmployee.enterpriseId
                        }
                    }

                }

                call.respond(HttpStatusCode.Created)
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Неверный формат данных")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Внутренняя ошибка сервера: ${e.message}")
            }
        }

        get("/employees/{employeeId}") {
            val employeeId = call.parameters["employeeId"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")

            val Data = transaction {
                Employees.select { Employees.id eq employeeId }
                    .singleOrNull()
                    ?.let {
                        EmployeeEdit(
                            employee_fio = it[Employees.employee_fio],
                            employee_phone = it[Employees.employee_phone],
                            position = it[Employees.position],
                            access = it[Employees.access]
                        )
                    }
            } ?: throw NotFoundException("not found")

            call.respond(Data)
        }

        put("/updateEmployee/{employeeId}") {
            val employeeId = call.parameters["employeeId"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid ID")

            val Data = call.receive<EmployeeEdit>()

            transaction {
                Employees.update({ Employees.id eq employeeId }) {
                    it[employee_fio] = Data.employee_fio
                    it[position] = Data.position
                    it[access] = Data.access
                }
            }

            call.respond(HttpStatusCode.OK)
        }

        delete("/deleteEmployee/{employeeId}") {
            val id = call.parameters["employeeId"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@delete
            }

            try {
                transaction {
                    Employees.deleteWhere { Employees.id eq id }
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Внутренняя ошибка сервера: ${e.message}")
            }
        }

        post("/service-employees") {
            try {
                val assignment = call.receive<ServiceEmployeeAssignment>()

                if (assignment.employee_ids.isEmpty()) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Список сотрудников не может быть пустым")
                }

                transaction {
                    assignment.employee_ids.forEach { employeeId ->
                        EmployeeServices.insert {
                            it[id_employee] = employeeId
                            it[id_services] = assignment.service_id
                        }
                    }
                }

                call.respond(HttpStatusCode.OK)
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Неверный формат данных: ${e.message}")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Внутренняя ошибка сервера: ${e.message ?: "неизвестная ошибка"}")
            }
        }

        get("/service-employees/{serviceId}") {
            try {
                val serviceId = call.parameters["serviceId"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Неверный ID услуги")

                val employees = transaction {
                    EmployeeServices
                        .slice(EmployeeServices.id_employee)
                        .select { EmployeeServices.id_services eq serviceId }
                        .map { it[EmployeeServices.id_employee] }
                }

                call.respond(employees)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError,
                    "Ошибка при получении списка сотрудников: ${e.message}")
            }
        }

        put("/service-employees/{serviceId}") {
            try {
                val serviceId = call.parameters["serviceId"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Неверный ID услуги")

                val employeeIds = call.receive<List<Int>>()

                transaction {
                    EmployeeServices.deleteWhere {
                        EmployeeServices.id_services eq serviceId
                    }

                    employeeIds.forEach { employeeId ->
                        EmployeeServices.insert {
                            it[id_employee] = employeeId
                            it[id_services] = serviceId
                        }
                    }
                }

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError,
                    "Ошибка при обновлении сотрудников: ${e.message}")
            }
        }

    }
}