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

class AuthRepository(context: Context) {
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

    private val baseUrl = "http://10.0.2.2:5053"

    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            val name = email.substringBefore("@")

            val response: HttpResponse = client.post("$baseUrl/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(name, email, password))
            }

            if (response.status.isSuccess()) {
                val loginResult = login(email, password)

                if (loginResult.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Регистрация успешна, но вход не удался"))
                }
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
                println("RAW TOKEN BODY: '$rawToken'")

                val cleanToken = rawToken.trim().removeSurrounding("\"")
                println("CLEAN TOKEN: '$cleanToken'")

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
}