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

    suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return try {
            val response = client.post("$baseUrl/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(name, email, password))
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
            val response: HttpResponse = client.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }

            if (response.status.isSuccess()) {
                val rawToken: String = response.body()
                println("[AuthRepository] RAW TOKEN BODY: '$rawToken'")

                val cleanToken = rawToken.trim().removeSurrounding("\"")
                println("[AuthRepository] CLEAN TOKEN: '$cleanToken'")

                // Сохраняем email для дальнейшего использования
                sharedPrefs.edit()
                    .putString("user_email", email)
                    .apply()
                println("[AuthRepository] Email сохранен: $email")

                tokenManager.saveToken(cleanToken)
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