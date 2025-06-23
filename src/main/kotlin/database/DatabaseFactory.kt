package com.example.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table() {
    val id = integer("id").autoIncrement()
    val fullName = varchar("fio", 100)
    val phoneNumber = varchar("user_phone", 20)
    val password = varchar("password", 255)

    override val primaryKey = PrimaryKey(id)
}

fun initDatabase() {
    Database.connect("jdbc:mysql://localhost:3306/booking",
        driver = "com.mysql.cj.jdbc.Driver", user = "pma", password = "your_password")
    transaction {
        SchemaUtils.create(Users)
    }
}