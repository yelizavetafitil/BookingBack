package com.example.booking.models

import kotlinx.serialization.Serializable

@Serializable
data class EnterpriseRegistration(
    val enterpriseName: String,
    val city: String,
    val address: String,
    val enterprisePhoneNumber: String
)

@Serializable
data class EnterpriseRegistrationResponse(
    val enterpriseId: Int
)
