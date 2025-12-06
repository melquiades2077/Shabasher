package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShabashShortResponse(
    val id: String,
    val name: String,
    val startDate: String,
    val startTime: String,
    val status: String
)