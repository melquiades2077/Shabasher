package com.example.shabasher.data

import kotlinx.coroutines.delay

class ProfileRepository {

    suspend fun getProfile(): Result<ProfileData> {
        delay(300)

        val mock = ProfileData(
            name = "Андрей",
            avatarUrl = null
        )

        return Result.success(mock)
    }

    fun logout() {
        // TODO: очистить токен / session
    }
}

data class ProfileData(
    val name: String,
    val avatarUrl: String?
)
