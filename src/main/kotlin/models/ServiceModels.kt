package com.example.models

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Service(
    val id: Int,
    val serviceName: String,
    val price: Double,
    val currency: String,
    val length: Int,
    val breakDuration: Int
)

@Serializable
data class ServiceData(
    val enterpriseId: Int,
    val serviceName: String,
    val price: Double,
    val currency: String,
    val length: Int,
    val breakDuration: Int
)

@Serializable
data class ServiceEdit(
    val serviceName: String,
    val price: Double,
    val currency: String,
    val length: Int,
    val breakDuration: Int
)

@Serializable
data class ServiceEmployeeAssignment(
    val service_id: Int,
    val employee_ids: List<Int>
)

@Serializable
data class ServiceAddResponse(
    val serviceId: Int,
    val success: Boolean,
    val message: String? = null
)