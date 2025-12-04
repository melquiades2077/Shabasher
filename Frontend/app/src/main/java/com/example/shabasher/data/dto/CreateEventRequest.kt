package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest(
    val title: String,
    val description: String,
    val address: String,
    val dateTime: String,
    val userIds: List<String>? = null
)