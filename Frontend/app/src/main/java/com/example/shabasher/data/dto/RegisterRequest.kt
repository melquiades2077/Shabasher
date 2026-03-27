package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val aboutMe: String = "",
    val telegram: String = ""
)