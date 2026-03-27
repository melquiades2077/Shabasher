package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SetNameRequest(
    val name: String
)