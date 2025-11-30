package com.example.shabasher.data

import kotlinx.coroutines.delay

class AuthRepository {

    // заглушки, позже интегрируем бэкенд на аспе
    suspend fun login(email: String, password: String): Result<Unit> {
        delay(1000) // имитация сети

        return if (email == "test@test.com" && password == "123456") {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Неверный email или пароль"))
        }
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        delay(1000)

        if (email == "test@test.com") {
            return Result.failure(Exception("Пользователь уже существует"))
        }

        return Result.success(Unit)
    }
}
