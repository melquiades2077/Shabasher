package com.example.shabasher.data.network

import android.content.Context
import com.example.shabasher.ViewModels.decodeUserId
import com.example.shabasher.data.dto.ProfileResponse
import com.example.shabasher.data.dto.UpdateUserProfileRequest
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
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
            requestTimeoutMillis = 20_000
        }
    }

    private val baseUrl = Config.BASE_URL

    // Основной метод — для своего профиля
    suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            val rawToken = tokenManager.getToken()
                ?: return Result.failure(Exception("Не авторизован"))

            val cleanToken = rawToken.trim().removeSurrounding("\"")
            val userId = decodeUserId(cleanToken)
                ?: return Result.failure(Exception("Не удалось извлечь userId из токена"))

            getProfileByIdInternal(userId, cleanToken)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Метод — для чужого профиля
    suspend fun getProfileById(userId: String): Result<ProfileResponse> {
        return try {
            val rawToken = tokenManager.getToken()
                ?: return Result.failure(Exception("Не авторизован"))

            val cleanToken = rawToken.trim().removeSurrounding("\"")
            getProfileByIdInternal(userId, cleanToken)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun getProfileByIdInternal(userId: String, token: String): Result<ProfileResponse> {
        val url = "$baseUrl/api/Users/by-id?id=$userId"

        val response: HttpResponse = client.get(url) {
            header("Authorization", "Bearer $token")
        }

        if (response.status.isSuccess()) {
            val body: ProfileResponse = response.body()
            return Result.success(body)
        } else {
            val errorText = response.bodyAsText()
            return Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
        }
    }

    // === НОВЫЙ МЕТОД: обновление профиля ===
    suspend fun updateProfile(request: UpdateUserProfileRequest): Result<ProfileResponse> {
        return try {
            val rawToken = tokenManager.getToken()
                ?: return Result.failure(Exception("Не авторизован"))

            val cleanToken = rawToken.trim().removeSurrounding("\"")

            val url = "$baseUrl/api/Users/profile"

            val response: HttpResponse = client.patch(url) {
                header("Authorization", "Bearer $cleanToken")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val updatedProfile: ProfileResponse = response.body()
                Result.success(updatedProfile)
            } else {
                val errorText = response.bodyAsText()
                Result.failure(Exception(errorText.ifBlank { "Ошибка обновления профиля: ${response.status}" }))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

