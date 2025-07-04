package com.example.booking.models

import kotlinx.serialization.Serializable

@Serializable
data class UserRegistration(
    val fullName: String,
    val phoneNumber: String,
    val password: String
)

@Serializable
data class RegistrationResponse(
    val userId: Int
)

@Serializable
data class LoginRequest(
    val fullName: String,
    val phoneNumber: String,
    val password: String
)

@Serializable
data class UserData(
    val fullName: String,
    val phoneNumber: String,
    val password: String
)

@Serializable
data class User(
    val id: Int,
    val fullName: String,
    val phone: String,
    val password: String
)