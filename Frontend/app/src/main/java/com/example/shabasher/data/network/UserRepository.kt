package com.example.shabasher.data.network

import android.content.Context
import com.example.shabasher.data.dto.UserResponse
import com.example.shabasher.data.local.TokenManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class UserRepository(context: Context) {
    private val tokenManager = TokenManager(context)
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }
    }

    private val baseUrl = Config.BASE_URL

    suspend fun getUserWithParticipations(): Result<UserResponse> {
        return try {
            println("[DEBUG] Получаем пользователя с участиями...")

            val token = tokenManager.getToken()
            if (token == null) {
                println("[DEBUG] Токен не найден!")
                return Result.failure(Exception("Не авторизован"))
            }

            val cleanToken = token.trim().removeSurrounding("\"")
            println("[DEBUG] Токен: ${cleanToken.take(20)}...")

            val response: HttpResponse = client.get("$baseUrl/api/Users/by-id") {
                header("Authorization", "Bearer $cleanToken")
            }

            println("[DEBUG] Статус ответа: ${response.status}")

            if (response.status.isSuccess()) {
                val bodyText = response.body<String>()
                println("[DEBUG] Тело ответа: $bodyText")

                val userResponse: UserResponse = response.body()
                println("[DEBUG] Успешно! Участий: ${userResponse.participations.size}")
                Result.success(userResponse)
            } else {
                val errorText = response.body<String>()
                println("[DEBUG] Ошибка: $errorText")
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            println("[DEBUG] Исключение: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}