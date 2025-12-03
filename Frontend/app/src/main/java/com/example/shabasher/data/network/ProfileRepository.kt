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
    }

    private val baseUrl = "http://10.0.2.2:5053"

    suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            val token = tokenManager.getToken()
            if (token == null) {
                return Result.failure(Exception("Не авторизован"))
            }

            val cleanToken = token.trim().removeSurrounding("\"")

            val response: HttpResponse = client.get("$baseUrl/api/user/profile") {
                header("Authorization", "Bearer $cleanToken")
            }

            if (response.status.isSuccess()) {
                val profile: ProfileResponse = response.body()
                Result.success(profile)
            } else {
                val errorText = response.body<String>()
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}