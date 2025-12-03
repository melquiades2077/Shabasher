package com.example.shabasher.data.network

import android.content.Context
import com.example.shabasher.data.dto.ProfileResponse
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

class ProfileRepository(context: Context) {
    private val tokenManager = TokenManager(context)
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        //хз без этого не работало
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
    }


    private val baseUrl = "http://10.0.2.2:5053"

    suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            println("[ProfileRepository] Начинаем загрузку профиля...")

            val token = tokenManager.getToken()
            if (token == null) {
                println("[ProfileRepository] Токен не найден!")
                return Result.failure(Exception("Не авторизован"))
            }

            val cleanToken = token.trim().removeSurrounding("\"")
            println("[ProfileRepository] Используем токен: ${cleanToken.take(20)}...")

            val url = "$baseUrl/api/auth/profile"
            println("[ProfileRepository] Запрос к: $url")

            val response: HttpResponse = client.get(url) {
                header("Authorization", "Bearer $cleanToken")
                timeout {
                    requestTimeoutMillis = 5000
                }
            }

            println("[ProfileRepository] Ответ сервера: ${response.status}")
            println("[ProfileRepository] Headers: ${response.headers}")

            if (response.status.isSuccess()) {
                val bodyText = response.body<String>()
                println("[ProfileRepository] Тело ответа: $bodyText")

                val profile: ProfileResponse = response.body()
                println("[ProfileRepository] Профиль успешно получен: ${profile.name}, ${profile.email}")
                Result.success(profile)
            } else {
                val errorText = response.body<String>()
                println("[ProfileRepository] Ошибка сервера: $errorText")
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            println("[ProfileRepository] Исключение: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}