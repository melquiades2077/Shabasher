package com.example.shabasher.data.network

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.shabasher.Model.ParticipationStatus
import com.example.shabasher.Model.UserRole
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
import io.ktor.http.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

    suspend fun addParticipant(eventId: String): Result<Boolean> {
        val token = tokenManager.getToken()
        if (token == null) {
            println("[EventsRepository] addParticipant: Токен не найден")
            return Result.failure(Exception("Не авторизован"))
        }

        val cleanToken = token.trim().removeSurrounding("\"")

        try {
            println("[EventsRepository] addParticipant: Отправка PATCH запроса для eventId: '$eventId'")
            println("[EventsRepository] addParticipant: URL: $baseUrl/api/Invites/join?shabashId=$eventId")
            
            val response: HttpResponse = client.patch("$baseUrl/api/Invites/join?shabashId=$eventId") {
                header("Authorization", "Bearer $cleanToken")
            }

            val statusCode = response.status.value
            println("[EventsRepository] addParticipant: HTTP статус: $statusCode (${response.status.description})")

            val responseBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "[Ошибка чтения тела: ${e.message}]"
            }

            println("[EventsRepository] addParticipant: Тело ответа: '$responseBody'")

            return if (response.status.isSuccess()) {
                println("[EventsRepository] addParticipant: УСПЕХ - пользователь добавлен в событие")
                Result.success(true)
            } else {
                println("[EventsRepository] addParticipant: ОШИБКА - $statusCode")
                Result.failure(Exception("Не удалось добавить участника: $responseBody"))
            }
        } catch (e: Exception) {
            println("[EventsRepository] addParticipant: ИСКЛЮЧЕНИЕ - ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    // Добавим метод для обновления статуса участника
    suspend fun updateParticipationStatus(eventId: String, newStatus: ParticipationStatus): Result<Boolean> {
        return try {
            val token = tokenManager.getToken()
            if (token == null) {
                return Result.failure(Exception("Не авторизован"))
            }

            val cleanToken = token.trim().removeSurrounding("\"")

            val response: HttpResponse = client.patch("$baseUrl/api/Users/status") {
                header("Authorization", "Bearer $cleanToken")
                contentType(ContentType.Application.Json)
                setBody(StatusUpdateRequest(
                    shabashId = eventId,
                    status = newStatus.code
                ))
            }

            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                val errorText = response.body<String>()
                println("[DEBUG] Ошибка обновления статуса: $errorText")
                Result.failure(Exception("Ошибка сервера: $errorText"))
            }
        } catch (e: Exception) {
            println("[DEBUG] Исключение при обновлении статуса: ${e.message}")
            Result.failure(e)
        }
    }


    // Получить все события пользователя
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getEvents(): Result<List<EventShortDto>> {
        return try {
            println("[DEBUG] === НАЧАЛО ПОЛУЧЕНИЯ СОБЫТИЙ ===")

            val token = tokenManager.getToken()
            if (token == null) {
                println("[DEBUG] Токен не найден!")
                return Result.failure(Exception("Не авторизован"))
            }

            val cleanToken = token.trim().removeSurrounding("\"")
            println("[DEBUG] Токен: ${cleanToken.take(20)}...")

            val userEmail = sharedPrefs.getString("user_email", null)
            if (userEmail == null) {
                println("[DEBUG] Email не найден в SharedPreferences!")
                return Result.failure(Exception("Не удалось определить пользователя"))
            }

            println("[DEBUG] Email пользователя: $userEmail")

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

                val userWithParticipations = getUserWithParticipations(cleanToken, user.id)

                if (userWithParticipations.participations.isEmpty()) {
                    println("[DEBUG] Нет участий, возвращаем пустой список")
                    return Result.success(emptyList())
                }

                val events = mutableListOf<EventShortDto>()

                for ((index, participation) in userWithParticipations.participations.withIndex()) {
                    println("[DEBUG] Обработка участия $index: ${participation.shabashId} - ${participation.shabashName}")

                    val event = getShabashById(cleanToken, participation.shabashId)

                    val title = event?.name ?: participation.shabashName ?: "Без названия"
                    val dateTime = event?.startDate ?: "Дата неизвестна"

                    events.add(
                        EventShortDto(
                            id = participation.shabashId,
                            title = title,
                            dateTime = dateTime,
                            status = getEventStatus(dateTime)
                        )
                    )

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

    //Потом можно все, где работаем с id перейти на эту функцию(по идее) но не сейчас
    suspend fun getCurrentUserId(): String? {
        return try {
            val cachedUserId = tokenManager.getUserId()
            if (cachedUserId != null) {
                println("[DEBUG] Используем кэшированный userId: $cachedUserId")
                return cachedUserId
            }

            val userEmail = sharedPrefs.getString("user_email", null)
            if (userEmail == null) {
                println("[DEBUG] Email не найден в SharedPreferences")
                return null
            }

            val token = tokenManager.getToken()
            if (token == null) {
                println("[DEBUG] Токен не найден")
                return null
            }

            val cleanToken = token.trim().removeSurrounding("\"")

            val response: HttpResponse = client.get("$baseUrl/api/Users/by-email") {
                header("Authorization", "Bearer $cleanToken")
                parameter("email", userEmail)
            }

            if (response.status.isSuccess()) {
                val user: UserResponse = response.body()
                println("[DEBUG] Получен userId: ${user.id}")

                tokenManager.saveUserId(user.id)

                user.id
            } else {
                println("[DEBUG] Ошибка получения userId: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("[DEBUG] Исключение при получении userId: ${e.message}")
            null
        }
    }

    private suspend fun getShabashById(token: String, shabashId: String): GetEventResponse? {
        return try {
            println("[DEBUG] Получение события по ID: $shabashId")

            val response: HttpResponse = client.get("$baseUrl/api/Shabashes/by-id") {
                header("Authorization", "Bearer $token")
                parameter("id", shabashId)
                timeout { requestTimeoutMillis = 5000 }
            }

            println("[DEBUG] Статус получения события: ${response.status}")

            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                println("[DEBUG] Тело ответа события: $responseText")

                try {
                    val event: GetEventResponse = response.body()
                    println("[DEBUG] Событие успешно получено: ${event.name ?: "без названия"}")
                    event
                } catch (e: Exception) {
                    println("[DEBUG] Ошибка десериализации события: ${e.message}")
                    // Возвращаем null если не можем распарсить
                    null
                }
            } else {
                println("[DEBUG] Ошибка HTTP при получении события: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("[DEBUG] Исключение при получении события: ${e.message}")
            null
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
    suspend fun getEventById(id: String): Result<GetEventResponse?> {
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
                try {
                    val event: GetEventResponse = response.body()
                    Result.success(event)
                } catch (e: Exception) {
                    println("[DEBUG] Ошибка десериализации события $id: ${e.message}")
                    Result.success(null) // Возвращаем null вместо ошибки
                }
            } else {
                val errorText = response.body<String>()
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveFromEvent(shabashId: String): Result<Unit> {
        return try {
            val token = tokenManager.getToken()
            if (token == null) {
                return Result.failure(Exception("Не авторизован"))
            }

            val cleanToken = token.trim().removeSurrounding("\"")

            // Используем PATCH, а не GET
            val response: HttpResponse = client.patch("$baseUrl/api/Shabashes/leave") {
                header("Authorization", "Bearer $cleanToken")
                // Передаём параметр в query-строке с правильным именем
                parameter("shabashId", shabashId)
            }

            if (response.status.isSuccess()) {
                // Успех: тело ответа пустое — просто возвращаем Unit
                Result.success(Unit)
            } else {
                // Читаем тело как строку (ошибка от сервера)
                val errorText = response.body<String>()
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // В EventsRepository.kt

    suspend fun kickParticipant(shabashId: String, targetUserId: String): Result<Unit> {
        return try {
            val token = tokenManager.getToken()
            if (token == null) return Result.failure(Exception("Не авторизован"))

            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("Не удалось определить пользователя"))

            val request = KickParticipantRequest(
                shabashId = shabashId,
                userId = targetUserId,
                adminId = currentUserId
            )

            val response: HttpResponse = client.patch("$baseUrl/api/Shabashes/kick") {
                header("Authorization", "Bearer ${token.trim().removeSurrounding("\"")}")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val error = response.body<String>()
                Result.failure(Exception("Ошибка при исключении: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateParticipantRole(shabashId: String, targetUserId: String, newRole: UserRole): Result<Unit> {
        return try {
            val token = tokenManager.getToken()
            if (token == null) return Result.failure(Exception("Не авторизован"))

            val roleString = newRole.backendValue
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("Не удалось определить пользователя"))


            val request = UpdateRoleRequest(
                shabashId = shabashId,
                userId = targetUserId,
                adminId = currentUserId, // ← Передаём себя как админа
                role = newRole.backendValue
            )

            val response: HttpResponse = client.patch("$baseUrl/api/Shabashes/roles") {
                header("Authorization", "Bearer ${token.trim().removeSurrounding("\"")}")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val error = response.body<String>()
                Result.failure(Exception("Ошибка при смене роли: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(shabashId: String): Result<Unit> {
        return try {
            val token = tokenManager.getToken()
            if (token == null) return Result.failure(Exception("Не авторизован"))

            val cleanToken = token.trim().removeSurrounding("\"")

            val response: HttpResponse = client.delete("$baseUrl/api/Shabashes") {
                header("Authorization", "Bearer $cleanToken")
                parameter("shabashId", shabashId)
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val error = response.body<String>()
                Result.failure(Exception("Ошибка удаления: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}


@RequiresApi(Build.VERSION_CODES.O)
fun getEventStatus(eventDateStr: String): String {
    val eventDate = LocalDate.parse(eventDateStr)
    val today = LocalDate.now()

    return when {
        eventDate.isBefore(today) -> "Событие завершено"
        eventDate.isEqual(today) -> "Сегодня"
        eventDate.isEqual(today.plusDays(1)) -> "Завтра"
        else -> {
            val daysUntil = ChronoUnit.DAYS.between(today, eventDate).toInt()
            // daysUntil >= 2 здесь гарантировано
            val daysText = when {
                daysUntil % 10 == 1 && daysUntil % 100 != 11 -> "день"
                daysUntil % 10 in 2..4 && daysUntil % 100 !in 12..14 -> "дня"
                else -> "дней"
            }
            "Осталось $daysUntil $daysText"
        }
    }
}



