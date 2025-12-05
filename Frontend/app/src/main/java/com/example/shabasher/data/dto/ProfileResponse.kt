package com.example.shabasher.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("createdAt")
    val createdAt: String? = null // формат ISO, бэкенд возвращает строку — не парсим тут
)