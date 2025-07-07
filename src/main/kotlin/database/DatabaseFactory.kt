package com.example.database

import org.jetbrains.exposed.sql.*
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

fun initDatabase() {
    Database.connect("jdbc:mysql://localhost:3306/booking",
        driver = "com.mysql.cj.jdbc.Driver", user = "pma", password = "your_password")
    transaction {
        SchemaUtils.create(Users)
        SchemaUtils.create(Companies)
    }
}