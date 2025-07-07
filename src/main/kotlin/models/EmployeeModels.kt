package com.example.booking.models

import kotlinx.serialization.Serializable


@Serializable
data class Employee(
    val id: Int,
    val employee_fio: String,
    val employee_phone: String,
    val position: String,
    val access: String
)


@Serializable
data class EmployeeData(
    val enterpriseId: Int,
    val employee_fio: String,
    val employee_phone: String,
    val position: String,
    val access: String
)


@Serializable
data class EmployeeEdit(
    val employee_fio: String,
    val employee_phone: String,
    val position: String,
    val access: String
)