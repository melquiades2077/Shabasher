package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateShabashRequest(
    val name: String,
    val description: String,
    val address: String,
    val startDate: String,
    val startTime: String
)