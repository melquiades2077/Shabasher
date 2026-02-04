package com.example.shabasher.data.network

import android.content.Context
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
import com.example.shabasher.data.dto.*
import android.content.SharedPreferences

class AuthRepository(context: Context) {
    private val tokenManager = TokenManager(context)
    private val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
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

    suspend fun register(
        name: String,
        email: String,
        password: String,
        aboutMe: String = "",       // ← опционально
        telegram: String = ""       // ← опционально
    ): Result<Unit> {
        return try {
            val response = client.post("$baseUrl/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(name, email, password, aboutMe, telegram)) // ← обновите data class!
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorText = response.body<String>()
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            // 1. Авторизация и получение токена
            val response: HttpResponse = client.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }

            if (response.status.isSuccess()) {
                val rawToken: String = response.body()
                val cleanToken = rawToken.trim().removeSurrounding("\"")

                tokenManager.saveToken(cleanToken)
                println("[AuthRepository] Токен сохранён")

                // 2. Сохраняем email
                sharedPrefs.edit()
                    .putString("user_email", email)
                    .apply()
                println("[AuthRepository] Email сохранён: $email")

                // 3. 🔥 Получаем ID пользователя по email (используя новый токен)
                val userResponse: HttpResponse = client.get("$baseUrl/api/Users/by-email") {
                    header("Authorization", "Bearer $cleanToken")
                    parameter("email", email)
                }

                if (userResponse.status.isSuccess()) {
                    val user: UserResponse = userResponse.body()
                    val userId = user.id

                    // 4. 🔥 Сохраняем актуальный user_id
                    tokenManager.saveUserId(userId)
                    println("[AuthRepository] User ID сохранён: $userId")
                } else {
                    println("[AuthRepository] Не удалось получить user ID после входа")
                    // Можно продолжить без ID, но лучше не скрывать ошибку
                }

                Result.success(Unit)
            } else {
                val errorText = response.body<String>()
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getToken(): String? {
        return tokenManager.getToken()
    }
    //Сохраненный email
    fun getSavedEmail(): String? {
        return sharedPrefs.getString("user_email", null)
    }
}