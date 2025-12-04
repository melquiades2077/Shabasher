// data/network/EventsRepository.kt
package com.example.shabasher.data.network

import android.content.Context
import com.example.shabasher.data.dto.CreateEventRequest
import com.example.shabasher.data.dto.CreateEventResponse
import com.example.shabasher.data.dto.GetEventResponse
import com.example.shabasher.data.dto.GetEventsResponse
import com.example.shabasher.data.dto.EventShortDto
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

class EventsRepository(context: Context) {
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

    private val baseUrl = Config.BASE_URL

    // Получить все события пользователя
    suspend fun getEvents(): Result<List<EventShortDto>> {
        return try {
            val token = tokenManager.getToken()
            if (token == null) {
                return Result.failure(Exception("Не авторизован"))
            }

            val cleanToken = token.trim().removeSurrounding("\"")

            // TODO: Проверить точный endpoint для получения списка событий
            val response: HttpResponse = client.get("$baseUrl/api/Shabashes") {
                header("Authorization", "Bearer $cleanToken")
            }

            if (response.status.isSuccess()) {
                val eventsResponse: GetEventsResponse = response.body()
                Result.success(eventsResponse.events)
            } else {
                val errorText = response.body<String>()
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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

            val dateTimeIso = convertToIsoDateTime(date, time)

            val request = CreateEventRequest(
                title = title,
                description = description,
                address = address,
                dateTime = dateTimeIso,
                userIds = invitedUserIds
            )

            println("[EventsRepository] Создание события: $title, дата: $dateTimeIso")

            val response: HttpResponse = client.post("$baseUrl/api/Shabashes") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $cleanToken")
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val createResponse: CreateEventResponse = response.body()
                println("[EventsRepository] Событие создано: ${createResponse.id}")
                Result.success(createResponse.id)
            } else {
                val errorText = response.body<String>()
                println("[EventsRepository] Ошибка создания: $errorText")
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
                parameter("id", id) // TODO: Проверить параметр
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

    // Вспомогательная функция для преобразования даты
    private fun convertToIsoDateTime(date: String, time: String): String {
        // TODO: Реализовать парсинг даты из "26 февраля 2026" и времени "22:00"
        // Пока возвращаем текущую дату
        return java.time.LocalDateTime.now().toString()
    }
}


