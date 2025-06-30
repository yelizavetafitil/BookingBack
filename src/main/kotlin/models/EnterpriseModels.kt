package com.example.booking.models

import kotlinx.serialization.Serializable

@Serializable
data class EnterpriseRegistration(
    val enterpriseName: String,
    val city: String,
    val address: String,
    val enterprisePhoneNumber: String,
    val userId: Int
)

@Serializable
data class EnterpriseRegistrationResponse(
    val enterpriseId: Int
)

@Serializable
data class EnterpriseData(
    val enterpriseName: String,
    val userId: Int,
    val enterpriseId: Int,
    val access: String,
)