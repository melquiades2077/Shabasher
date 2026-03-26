package com.example.shabasher.data.network

import android.util.Log
import com.example.shabasher.Model.Suggestion
import com.example.shabasher.data.dto.SuggestionResponseDto
import com.example.shabasher.data.dto.SuggestionsListResponseDto
import com.example.shabasher.data.dto.VoteResultDto
import com.example.shabasher.data.local.TokenManager
import com.example.shabasher.utils.JwtUtils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException
import java.util.UUID

class SuggestionsRepository(private val tokenManager: TokenManager) {

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

    private companion object {
        private const val SHABASHES_BASE = "/api/Shabashes"
        private const val SUGGESTIONS_BASE = "/api/suggestions"
        private const val TAG = "SuggestionsRepo"
    }

    // ✅ Вспомогательный метод: извлечение userId из JWT (как в ProfileRepository)
    private fun decodeUserId(token: String): String? {
        return try {
            // JWT = header.payload.signature
            val parts = token.split(".")
            if (parts.size != 3) return null

            // Декодируем payload (base64url)
            val payload = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            val json = String(payload)

            // Простой парсинг без дополнительной библиотеки
            val userIdKey = "\"userId\":\""
            val startIndex = json.indexOf(userIdKey)
            if (startIndex == -1) return null

            val valueStart = startIndex + userIdKey.length
            val valueEnd = json.indexOf('"', valueStart)
            if (valueEnd == -1) return null

            val userId = json.substring(valueStart, valueEnd)
            if (userId.isBlank() || !isValidUuid(userId)) return null

            userId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode userId from token", e)
            null
        }
    }

    private fun isValidUuid(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    // ✅ Внутренний метод для получения списка предложений (избегает дублирования)
    private suspend fun getSuggestionsInternal(eventId: String, token: String): Result<List<Suggestion>> {
        return try {
            val url = "$baseUrl$SHABASHES_BASE/$eventId/suggestions"
            Log.d(TAG, "GET $url")

            val response: HttpResponse = client.get(url) {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                val body: SuggestionsListResponseDto = response.body()
                Result.success(body.suggestions.map { it.toDomain() })
            } else {
                val errorText = response.bodyAsText()
                Log.e(TAG, "GET failed: ${response.status} - $errorText")
                Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in getSuggestionsInternal", e)
            Result.failure(e)
        }
    }

    // ✅ Основной публичный метод
    suspend fun getSuggestions(eventId: String): Result<List<Suggestion>> {
        return try {
            val rawToken = tokenManager.getToken()
                ?: return Result.failure(Exception("Не авторизован"))

            val cleanToken = rawToken.trim().removeSurrounding("\"")
            val userId = decodeUserId(cleanToken)
                ?: return Result.failure(Exception("Не удалось извлечь userId из токена"))

            // Опционально: можно добавить проверку, что пользователь — участник события
            getSuggestionsInternal(eventId, cleanToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error in getSuggestions", e)
            Result.failure(e)
        }
    }

    // ✅ Внутренний метод для создания предложения
    private suspend fun createSuggestionInternal(eventId: String, text: String, token: String): Result<Suggestion> {
        return try {
            val url = "$baseUrl$SHABASHES_BASE/$eventId/suggestions"
            Log.d(TAG, "POST $url")
            Log.d(TAG, "Request body (JSON string): \"$text\"")

            val response = client.post(url) {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                // ✅ ASP.NET Core [FromBody] string ожидает валидную JSON-строку в кавычках
                setBody("\"${text.replace("\"", "\\\"")}\"")
            }

            Log.d(TAG, "Response status: ${response.status.value}")

            if (response.status.isSuccess()) {
                val body: SuggestionResponseDto = response.body()
                Result.success(body.toDomain())
            } else {
                val errorText = response.bodyAsText()
                Log.e(TAG, "POST failed: ${response.status} - $errorText")
                Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in createSuggestionInternal", e)
            Result.failure(e)
        }
    }

    // ✅ Публичный метод создания предложения
    suspend fun createSuggestion(eventId: String, text: String): Result<Suggestion> {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return Result.failure(Exception("Текст не может быть пустым"))

        return try {
            val rawToken = tokenManager.getToken()
                ?: return Result.failure(Exception("Не авторизован"))

            val cleanToken = rawToken.trim().removeSurrounding("\"")
            val userId = decodeUserId(cleanToken)
                ?: return Result.failure(Exception("Не удалось извлечь userId из токена"))

            createSuggestionInternal(eventId, trimmedText, cleanToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error in createSuggestion", e)
            Result.failure(e)
        }
    }

    // ✅ Голосование за предложение
    suspend fun vote(suggestionId: String, action: String): Result<VoteResultDto> {
        return try {
            val rawToken = tokenManager.getToken()
                ?: return Result.failure(Exception("Не авторизован"))

            val cleanToken = rawToken.trim().removeSurrounding("\"")

            val url = "$baseUrl$SUGGESTIONS_BASE/$suggestionId/vote"
            Log.d(TAG, "POST $url")

            val response = client.post(url) {
                header("Authorization", "Bearer $cleanToken")
                contentType(ContentType.Application.Json)
                // ✅ action тоже строка — оборачиваем в кавычки для валидного JSON
                setBody("\"$action\"")
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorText = response.bodyAsText()
                Log.e(TAG, "Vote failed: ${response.status} - $errorText")
                Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in vote", e)
            Result.failure(e)
        }
    }

    // ✅ Удаление предложения
    suspend fun deleteSuggestion(suggestionId: String): Result<Unit> {
        return try {
            val rawToken = tokenManager.getToken()
                ?: return Result.failure(Exception("Не авторизован"))

            val cleanToken = rawToken.trim().removeSurrounding("\"")

            val url = "$baseUrl$SUGGESTIONS_BASE/$suggestionId"
            Log.d(TAG, "DELETE $url")

            val response = client.delete(url) {
                header("Authorization", "Bearer $cleanToken")
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorText = response.bodyAsText()
                Log.e(TAG, "Delete failed: ${response.status} - $errorText")
                Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in deleteSuggestion", e)
            Result.failure(e)
        }
    }

    // ✅ Получение текущего userId (для оптимистичного UI)
    fun getCurrentUserId(): String? {
        return try {
            val rawToken = tokenManager.getToken() ?: return null
            val cleanToken = rawToken.trim().removeSurrounding("\"")
            decodeUserId(cleanToken)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current userId", e)
            null
        }
    }

    fun getCurrentUserName(): String? {
        return try {
            val rawToken = tokenManager.getToken() ?: return null
            val cleanToken = rawToken.trim().removeSurrounding("\"")
            JwtUtils.decodeUserName(cleanToken)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode userName from token", e)
            null
        }
    }

    // ✅ Очистка ресурсов (вызывайте при уничтожении ViewModel, если нужно)
    fun close() {
        client.close()
    }
}