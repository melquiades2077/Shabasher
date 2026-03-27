package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable


// SetNameResponse.kt
@Serializable
data class SetNameResponse(
    val message: String,
    val name: String
)