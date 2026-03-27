package com.example.shabasher.data.network

import android.content.Context
import android.content.SharedPreferences
import com.example.shabasher.data.local.TokenManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

class InviteRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
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

    suspend fun createInvite(eventId: String): Result<String> {
        return try {
            val token = tokenManager.getToken()
            if (token == null) {
                return Result.failure(Exception("Не авторизован"))
            }

            val cleanToken = token.trim().removeSurrounding("\"")

            val response: HttpResponse = client.post("$baseUrl/api/Invites/create") {
                header("Authorization", "Bearer $cleanToken")
                contentType(ContentType.Application.Json)
                parameter("shabashId", eventId)
            }

            if (response.status.isSuccess()) {
                val inviteLink = response.body<String>()
                Result.success(inviteLink)
            } else {
                val errorText = response.body<String>()
                Result.failure(Exception("Ошибка при получении ссылки: $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
