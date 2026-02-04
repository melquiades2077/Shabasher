package com.example.shabasher.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateRoleRequest(
    val shabashId: String,
    val userId: String,
    val role: String // "Admin", "Moderator", "Member"
)