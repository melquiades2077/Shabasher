package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserProfileRequest(
    val name: String,
    val aboutMe: String? = null,
    val telegram: String? = null
)