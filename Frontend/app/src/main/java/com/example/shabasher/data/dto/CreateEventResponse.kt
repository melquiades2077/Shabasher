package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventResponse(
    val id: String,
    val title: String,
    val message: String
)