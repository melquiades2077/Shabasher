package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventShortDto(
    val id: String,
    val title: String,
    val dateTime: String,
    val status: String
)