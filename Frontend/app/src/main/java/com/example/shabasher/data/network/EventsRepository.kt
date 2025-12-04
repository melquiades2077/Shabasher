package com.example.shabasher.data.network

import android.content.Context
import android.content.SharedPreferences
import com.example.shabasher.data.dto.*
import com.example.shabasher.data.local.TokenManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

class EventsRepository(context: Context) {
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

    // Получить все события пользователя
    suspend fun getEvents(): Result<List<EventShortDto>> {
        return try {
            println("[DEBUG] === НАЧАЛО ПОЛУЧЕНИЯ СОБЫТИЙ ===")

            // 1. Получаем токен
            val token = tokenManager.getToken()
            if (token == null) {
                println("[DEBUG] Токен не найден!")
                return Result.failure(Exception("Не авторизован"))
            }

            val cleanToken = token.trim().removeSurrounding("\"")
            println("[DEBUG] Токен: ${cleanToken.take(20)}...")

            // 2. Получаем email из SharedPreferences
            val userEmail = sharedPrefs.getString("user_email", null)
            if (userEmail == null) {
                println("[DEBUG] Email не найден в SharedPreferences!")
                return Result.failure(Exception("Не удалось определить пользователя"))
            }

            println("[DEBUG] Email пользователя: $userEmail")

            // 3. Получаем пользователя по email
            val userByEmailUrl = "$baseUrl/api/Users/by-email?email=${userEmail}"
            println("[DEBUG] Запрос пользователя по email: $userByEmailUrl")

            val userByEmailResponse: HttpResponse = client.get(userByEmailUrl) {
                header("Authorization", "Bearer $cleanToken")
                header("Accept", "application/json")
                timeout { requestTimeoutMillis = 10000 }
            }

            println("[DEBUG] Статус ответа по email: ${userByEmailResponse.status}")

            if (userByEmailResponse.status.isSuccess()) {
                val user: UserResponse = userByEmailResponse.body()
                println("[DEBUG] Получен пользователь по email: ${user.name}, ID: ${user.id}")

                // 4. Получаем пользователя с участиями по ID
                val userWithParticipations = getUserWithParticipations(cleanToken, user.id)

                if (userWithParticipations.participations.isEmpty()) {
                    println("[DEBUG] Нет участий, возвращаем пустой список")
                    return Result.success(emptyList())
                }

                // 5. Для каждого участия получаем полную информацию о событии
                val events = mutableListOf<EventShortDto>()

                for ((index, participation) in userWithParticipations.participations.withIndex()) {
                    println("[DEBUG] Обработка участия $index: ${participation.shabashId} - ${participation.shabashName}")

                    try {
                        val event = getShabashById(cleanToken, participation.shabashId)
                        events.add(
                            EventShortDto(
                                id = event.id,
                                title = event.title,
                                dateTime = event.dateTime,
                                status = participation.status.toString()
                            )
                        )
                    } catch (e: Exception) {
                        println("[DEBUG] Ошибка получения события: ${e.message}")
                        // Добавляем с базовой информацией
                        events.add(
                            EventShortDto(
                                id = participation.shabashId,
                                title = participation.shabashName,
                                dateTime = "Дата неизвестна",
                                status = participation.status.toString()
                            )
                        )
                    }

                    // Небольшая задержка чтобы не нагружать сервер
                    if (index < userWithParticipations.participations.size - 1) {
                        delay(100)
                    }
                }

                println("[DEBUG] === УСПЕХ: Получено ${events.size} событий ===")
                Result.success(events)

            } else {
                val errorText = userByEmailResponse.body<String>()
                println("[DEBUG] Ошибка получения по email: $errorText")
                Result.failure(Exception("Не удалось получить пользователя: $errorText"))
            }

        } catch (e: Exception) {
            println("[DEBUG] Исключение в getEvents: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        } finally {
            println("[DEBUG] === КОНЕЦ ПОЛУЧЕНИЯ СОБЫТИЙ ===")
        }
    }

    private suspend fun getUserWithParticipations(token: String, userId: String): UserResponse {
        val userUrl = "$baseUrl/api/Users/by-id?id=$userId"
        println("[DEBUG] Запрос пользователя с участиями: $userUrl")

        val response: HttpResponse = client.get(userUrl) {
            header("Authorization", "Bearer $token")
            header("Accept", "application/json")
            timeout { requestTimeoutMillis = 10000 }
        }

        if (!response.status.isSuccess()) {
            throw Exception("Ошибка получения пользователя: ${response.status}")
        }

        return response.body()
    }

    private suspend fun getShabashById(token: String, shabashId: String): GetEventResponse {
        val response: HttpResponse = client.get("$baseUrl/api/Shabashes/by-id") {
            header("Authorization", "Bearer $token")
            parameter("id", shabashId)
            timeout { requestTimeoutMillis = 5000 }
        }

        if (!response.status.isSuccess()) {
            throw Exception("Ошибка получения события: ${response.status}")
        }

        return response.body()
    }

    suspend fun createEvent(
        title: String,
        description: String,
        address: String,
        date: String,
        time: String,
        invitedUserIds: List<String>? = null
    ): Result<String> {
        return try {
            val token = tokenManager.getToken()
            if (token == null) {
                return Result.failure(Exception("Не авторизован"))
            }

            val cleanToken = token.trim().removeSurrounding("\"")

            val request = CreateEventRequest(
                name = title,
                description = description,
                address = address,
                startDate = date,
                startTime = "$time:00"
            )

            println("[EventsRepository] Создание события: $title, дата: $date")

            val response: HttpResponse = client.post("$baseUrl/api/Shabashes") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $cleanToken")
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val responseText = response.body<String>()
                println("[DEBUG] Ответ создания: '$responseText'")

                val eventId = responseText.trim().removeSurrounding("\"")
                println("[SUCCESS] Событие создано! ID: $eventId")

                Result.success(eventId)
            } else {
                val errorText = response.body<String>()
                println("[ERROR] $errorText")
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            println("[EventsRepository] Исключение: ${e.message}")
            Result.failure(e)
        }
    }

    // Получить событие по ID
    suspend fun getEventById(id: String): Result<GetEventResponse> {
        return try {
            val token = tokenManager.getToken()
            if (token == null) {
                return Result.failure(Exception("Не авторизован"))
            }

            val cleanToken = token.trim().removeSurrounding("\"")

            val response: HttpResponse = client.get("$baseUrl/api/Shabashes/by-id") {
                header("Authorization", "Bearer $cleanToken")
                parameter("id", id)
            }

            if (response.status.isSuccess()) {
                val event: GetEventResponse = response.body()
                Result.success(event)
            } else {
                val errorText = response.body<String>()
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


