package com.example.shabasher.data

import kotlinx.coroutines.delay

class ProfileRepository {

    suspend fun setUserName(name: String): Result<Unit> {
        delay(500) // имитация запроса
        return Result.success(Unit)
    }
}
