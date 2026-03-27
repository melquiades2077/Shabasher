package com.example.shabasher.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateRoleRequest(
    @SerialName("ShabashId") val shabashId: String,
    @SerialName("UserId") val userId: String,
    @SerialName("AdminId") val adminId: String, // ← Обязательно!
    @SerialName("Role") val role: String
)