package com.example.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.time
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table() {
    val id = integer("id_user").autoIncrement()
    val fullName = varchar("fio", 100)
    val phoneNumber = varchar("user_phone", 20)
    val password = varchar("password", 255)

    override val primaryKey = PrimaryKey(id)
}

object Companies : Table() {
    val idCompany = integer("id_company").autoIncrement()
    val enterpriseName = varchar("company_name", 100)
    val city = varchar("city", 50)
    val address = varchar("address", 200)
    val enterprisePhoneNumber = varchar("company_phone", 20)

    override val primaryKey = PrimaryKey(idCompany)
}

object Projects : Table() {
    val idProject = integer("id_project").autoIncrement()
    val access = varchar("access", 20)
    val id_user  = integer("id_user").references(Users.id)
    val id_company = integer("id_company")references(Companies.idCompany)

    override val primaryKey = PrimaryKey(idProject)
}

object Services : Table("services") {
    val idServices = integer("id_services").autoIncrement()
    val idCompany = integer("id_company").references(Companies.idCompany)
    val serviceName = varchar("service_name", 100)
    val price = decimal("price", 10, 2)
    val currency = varchar("currency", 3)
    val length = integer("length")
    val breakDuration = integer("break")

    override val primaryKey = PrimaryKey(idServices)
}

object Employees : Table("employee") {
    val id = integer("id_employee").autoIncrement()
    val idCompany = integer("id_company").references(Companies.idCompany)
    val employee_fio = varchar("employee_fio", 100)
    val employee_phone = varchar("employee_phone", 20)
    val position = varchar("position", 50)
    val access = varchar("access", 50)

    override val primaryKey = PrimaryKey(id)
}

object EmployeeServices : Table("employee_services") {
    val id_employee = integer("id_employee").references(Employees.id)
    val id_services = integer("id_services").references(Services.idServices)
}

object ScheduleTypes : Table("schedule_types") {
    val id = integer("id_schedule_types").autoIncrement()
    val name = varchar("schedule_types_name", 50)

    override val primaryKey = PrimaryKey(id)
}

object ScheduleSubtypes : Table("schedule_subtypes") {
    val id = integer("id_schedule_subtypes").autoIncrement()
    val name = varchar("schedule_subtypes_name", 50)

    override val primaryKey = PrimaryKey(id)
}

object SchedulePatterns : Table("schedule_patterns") {
    val id = integer("id_schedule_patterns").autoIncrement()
    val daysWork = integer("days_work")
    val daysRest = integer("days_rest")

    override val primaryKey = PrimaryKey(id)
}

object ScheduleWeekDays : Table("schedule_week_days") {
    val id = integer("id_schedule_week_days").autoIncrement()
    val dayOfWeek = integer("day_of_week")
    val isWorking = bool("is_working")

    override val primaryKey = PrimaryKey(id)
}

object ScheduleExceptions : Table("schedule_exceptions") {
    val id = integer("id_schedule_exceptions").autoIncrement()
    val exceptionDate = date("exception_date")
    val isWorking = bool("is_working")

    override val primaryKey = PrimaryKey(id)
}

object WorkTimeSlots : Table("work_time_slots") {
    val id = integer("id_work_time_slots").autoIncrement()
    val startTime = time("start_time")
    val endTime = time("end_time")
    val validFrom = date("valid_from")
    val validTo = date("valid_to")

    override val primaryKey = PrimaryKey(id)
}

object WorkBreaks : Table("work_breaks") {
    val id = integer("id_work_breaks").autoIncrement()
    val idWorkTimeSlot = integer("id_work_time_slots").references(WorkTimeSlots.id)
    val breakStartTime = time("break_start_time")
    val breakEndTime = time("break_end_time")

    override val primaryKey = PrimaryKey(id)
}

object EmployeeSchedules : Table("employee_schedules") {
    val id = integer("id_employee_schedules").autoIncrement()
    val idEmployee = integer("id_employee").references(Employees.id)
    val idScheduleType = integer("id_schedule_types").references(ScheduleTypes.id).nullable()
    val idScheduleSubtype = integer("id_schedule_subtypes").references(ScheduleSubtypes.id).nullable()
    val idSchedulePattern = integer("id_schedule_patterns").references(SchedulePatterns.id).nullable()
    val idScheduleWeekDay = integer("id_schedule_week_days").references(ScheduleWeekDays.id).nullable()
    val idScheduleException = integer("id_schedule_exceptions").references(ScheduleExceptions.id).nullable()
    val idWorkTimeSlot = integer("id_work_time_slots").references(WorkTimeSlots.id).nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

fun initDatabase() {
    Database.connect("jdbc:mysql://localhost:3306/booking",
        driver = "com.mysql.cj.jdbc.Driver", user = "pma", password = "your_password")
    transaction {
        SchemaUtils.create(
            Users,
            Companies,
            Projects,
            Services,
            Employees,
            EmployeeServices,
            ScheduleTypes,
            ScheduleSubtypes,
            SchedulePatterns,
            ScheduleWeekDays,
            ScheduleExceptions,
            WorkTimeSlots,
            WorkBreaks,
            EmployeeSchedules
        )
    }
}