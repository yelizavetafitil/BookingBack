package com.example.booking.models

import kotlinx.serialization.Serializable

@Serializable
data class UserRegistration(
    val fullName: String,
    val phoneNumber: String,
    val password: String
)