package com.example.shabasher.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.example.shabasher.data.dto.*

class AuthRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }

        // Для разработки - можно включить логирование
        install(io.ktor.client.plugins.logging.Logging) {
            level = io.ktor.client.plugins.logging.LogLevel.ALL
        }
    }

    //URL бека
    private val baseUrl = "http://10.0.2.2:5132"

    // заглушки, позже интегрируем бэкенд на аспе
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            println("Отправка запроса на: $baseUrl/api/auth/login")

            val response: HttpResponse = client.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }

            println("Статус ответа: ${response.status}")

            if (response.status.isSuccess()) {
                val token: String = response.body()
                println("Успешный логин! Токен: ${token.take(20)}...")
                Result.success(Unit)
            } else {
                val errorText = response.body<String>()
                println("Ошибка сервера: $errorText")
                Result.failure(Exception(errorText))
            }
        } catch (e: ClientRequestException) {
            println("Ошибка запроса: ${e.response.status}")
            Result.failure(Exception("Ошибка запроса: ${e.response.status}"))
        } catch (e: ServerResponseException) {
            println("Ошибка сервера: ${e.response.status}")
            Result.failure(Exception("Ошибка сервера: ${e.response.status}"))
        } catch (e: Exception) {
            println("Сетевая ошибка: ${e.message}")
            Result.failure(Exception("Не удалось подключиться к серверу: ${e.message}"))
        }
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            println("Отправка запроса на: $baseUrl/api/auth/register")

            // Для регистрации нужен name - используем часть email
            val name = email.substringBefore("@")

            val response: HttpResponse = client.post("$baseUrl/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(name, email, password))
            }

            println("Статус ответа: ${response.status}")

            if (response.status.isSuccess()) {
                val userResponse: UserResponse = response.body()
                println("Успешная регистрация! Пользователь: ${userResponse.email}")
                Result.success(Unit)
            } else {
                val errorText = response.body<String>()
                println("Ошибка сервера: $errorText")
                Result.failure(Exception(errorText))
            }
        } catch (e: ClientRequestException) {
            println("Ошибка запроса: ${e.response.status}")
            Result.failure(Exception("Ошибка запроса: ${e.response.status}"))
        } catch (e: ServerResponseException) {
            println("Ошибка сервера: ${e.response.status}")
            Result.failure(Exception("Ошибка сервера: ${e.response.status}"))
        } catch (e: Exception) {
            println("Сетевая ошибка: ${e.message}")
            Result.failure(Exception("Не удалось подключиться к серверу: ${e.message}"))
        }
    }
    }
