package com.example.shabasher.data.network

import android.content.Context
import com.example.shabasher.ViewModels.decodeUserId
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
class ProfileRepository(private val tokenManager: TokenManager) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
            requestTimeoutMillis = 20_000
        }
    }

    private val baseUrl = Config.BASE_URL

    suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            val rawToken = tokenManager.getToken()
                ?: return Result.failure(Exception("Не авторизован"))

            val cleanToken = rawToken.trim().removeSurrounding("\"")
            val userId = decodeUserId(cleanToken)
                ?: return Result.failure(Exception("Не удалось извлечь userId из токена"))

            // GET /api/Users/by-id?id=<userId>
            val url = "$baseUrl/api/Users/by-id?id=$userId"

            val response: HttpResponse = client.get(url) {
                header("Authorization", "Bearer $cleanToken")
            }

            if (response.status.isSuccess()) {
                val body: ProfileResponse = response.body()
                Result.success(body)
            } else {
                val errorText = response.bodyAsText()
                Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}


