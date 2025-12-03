package com.example.shabasher.data.network

import kotlinx.coroutines.delay

class NameRepository {

    suspend fun setUserName(name: String): Result<Unit> {
        delay(500) // имитация запроса
        return Result.success(Unit)
    }
}