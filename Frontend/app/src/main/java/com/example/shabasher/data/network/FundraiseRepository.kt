package com.example.shabasher.data.network

import android.util.Log
import com.example.shabasher.data.dto.CreateFundraiseRequestDto
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.data.dto.FundraiseDetailsResponseDto
import com.example.shabasher.data.dto.FundraisesListResponseDto
import com.example.shabasher.data.dto.toDomain
import com.example.shabasher.data.local.TokenManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class FundraisesRepository(private val tokenManager: TokenManager) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                // ✅ Для корректной сериализации null и чисел
                encodeDefaults = true
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
        // ✅ Два базовых пути!
        private const val SHABASHES_BASE = "/api/Shabashes"
        private const val FUNDRAISES_BASE = "/api/fundraises"
        private const val TAG = "FundraisesRepo"
    }

    // ✅ Вспомогательные методы
    private fun decodeUserId(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payload = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            val json = String(payload)
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

    private fun isValidUuid(uuid: String): Boolean = try {
        UUID.fromString(uuid); true
    } catch (e: IllegalArgumentException) { false }

    private fun cleanToken(raw: String): String = raw.trim().removeSurrounding("\"")

    // ✅ 1. Получение списка сборов для события
    // ⚠️ Эндпоинт: GET /api/Shabashes/{shabashId}/fundraises
    suspend fun getAllFundraises(shabashId: String): Result<List<Fundraise>> {
        return try {
            val rawToken = tokenManager.getToken() ?: return Result.failure(Exception("Не авторизован"))
            val token = cleanToken(rawToken)
            val userId = decodeUserId(token) ?: return Result.failure(Exception("Не удалось извлечь userId"))

            val url = "$baseUrl$SHABASHES_BASE/$shabashId/fundraises"
            Log.d(TAG, "GET $url")

            val response = client.get(url) {
                header("Authorization", "Bearer $token")
            }

            when {
                response.status.isSuccess() -> {
                    val body: FundraisesListResponseDto = response.body()
                    Result.success(body.fundraisings.map { it.toDomain(userId) })
                }
                response.status == HttpStatusCode.Forbidden ->
                    Result.failure(SecurityException("Нет доступа к событию"))
                response.status == HttpStatusCode.NotFound ->
                    Result.failure(NoSuchElementException("Событие не найдено"))
                else -> {
                    val errorText = response.bodyAsText()
                    Log.e(TAG, "GET failed: ${response.status} - $errorText")
                    Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in getAllFundraises", e)
            Result.failure(e)
        }
    }

    // ✅ 2. Получение деталей сбора
    // ⚠️ Эндпоинт: GET /api/fundraises/{fundraiseId}
    suspend fun getFundraiseDetails(fundraiseId: String): Result<Fundraise> {
        return try {
            val rawToken = tokenManager.getToken() ?: return Result.failure(Exception("Не авторизован"))
            val token = cleanToken(rawToken)
            val userId = decodeUserId(token) ?: return Result.failure(Exception("Не удалось извлечь userId"))

            val url = "$baseUrl$FUNDRAISES_BASE/$fundraiseId"
            Log.d(TAG, "GET $url")

            val response = client.get(url) {
                header("Authorization", "Bearer $token")
            }

            when {
                response.status.isSuccess() -> {
                    val body: FundraiseDetailsResponseDto = response.body()
                    Result.success(body.toDomain(userId))
                }
                response.status == HttpStatusCode.Forbidden ->
                    Result.failure(SecurityException("Нет доступа к сбору"))
                response.status == HttpStatusCode.NotFound ->
                    Result.failure(NoSuchElementException("Сбор не найден"))
                else -> {
                    val errorText = response.bodyAsText()
                    Log.e(TAG, "GET failed: ${response.status} - $errorText")
                    Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in getFundraiseDetails", e)
            Result.failure(e)
        }
    }

    // ✅ 3. Создание нового сбора
    // ⚠️ Эндпоинт: POST /api/Shabashes/{shabashId}/fundraises
    // 🔐 Только Admin/CoAdmin
    suspend fun createFundraise(
        shabashId: String,
        title: String,
        description: String?,
        targetAmount: BigDecimal?,
        paymentPhone: String,
        paymentRecipient: String
    ): Result<Fundraise> {
        return try {
            if (title.isBlank()) return Result.failure(IllegalArgumentException("Название не может быть пустым"))
            if (paymentPhone.isBlank()) return Result.failure(IllegalArgumentException("Телефон не может быть пустым"))
            if (paymentRecipient.isBlank()) return Result.failure(IllegalArgumentException("Получатель не может быть пустым"))

            // В методе createFundraise(), ПЕРЕД запросом:
            Log.d(TAG, "=== createFundraise DEBUG ===")
            Log.d(TAG, "shabashId: $shabashId")
            Log.d(TAG, "Title: $title")

            val rawToken = tokenManager.getToken()
            Log.d(TAG, "Raw token from manager: ${rawToken?.take(50)}...") // первые 50 символов

            val token = cleanToken(rawToken ?: "")
            Log.d(TAG, "Cleaned token: ${token.take(50)}...")

            val userId = decodeUserId(token)
            Log.d(TAG, "Decoded userId: $userId")

            if (token.isBlank()) {
                Log.e(TAG, "❌ TOKEN IS EMPTY!")
                return Result.failure(Exception("Токен пуст"))
            }
            if (userId.isNullOrBlank()) {
                Log.e(TAG, "❌ FAILED TO DECODE userId FROM TOKEN!")
                return Result.failure(Exception("Не удалось декодировать userId"))
            }

            val url = "$baseUrl$SHABASHES_BASE/$shabashId/fundraises"
            Log.d(TAG, "POST URL: $url")

            val requestDto = CreateFundraiseRequestDto(
                title = title.trim(),
                description = description?.trim(),
                targetAmount = targetAmount,
                paymentPhone = paymentPhone.trim(),
                paymentRecipient = paymentRecipient.trim()
            )

            val response = client.post(url) {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(requestDto)
            }

            when {
                response.status.isSuccess() -> {
                    val body: FundraiseDetailsResponseDto = response.body()
                    Result.success(body.toDomain(userId))
                }
                response.status == HttpStatusCode.Forbidden -> {
                    val errorBody = response.bodyAsText()
                    Log.e(TAG, "❌ 403 Forbidden! Response body: $errorBody")
                    // 🔥 Попробуем декодировать токен ещё раз для отладки
                    Log.d(TAG, "Token length: ${token.length}")
                    Log.d(TAG, "Token parts count: ${token.split(".").size}")
                    Result.failure(SecurityException("Нет доступа к событию. Роль: проверьте права пользователя."))

                }
                response.status == HttpStatusCode.NotFound ->
                    Result.failure(NoSuchElementException("Событие не найдено"))
                response.status == HttpStatusCode.BadRequest -> {
                    val errorText = response.bodyAsText()
                    Result.failure(IllegalArgumentException(errorText.ifBlank { "Неверные данные" }))
                }
                else -> {
                    val errorText = response.bodyAsText()
                    Log.e(TAG, "POST failed: ${response.status} - $errorText")
                    Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in createFundraise", e)
            Result.failure(e)
        }
    }

    // ✅ 4. Закрытие сбора
    // ⚠️ Эндпоинт: POST /api/fundraises/{fundraiseId}/close
    // 🔐 Только Admin/CoAdmin
    suspend fun closeFundraise(fundraiseId: String): Result<Unit> {
        return try {
            val rawToken = tokenManager.getToken() ?: return Result.failure(Exception("Не авторизован"))
            val token = cleanToken(rawToken)

            val url = "$baseUrl$FUNDRAISES_BASE/$fundraiseId/close"
            Log.d(TAG, "POST $url")

            val response = client.post(url) {
                header("Authorization", "Bearer $token")
            }

            when {
                response.status.isSuccess() -> Result.success(Unit)
                response.status == HttpStatusCode.Forbidden ->
                    Result.failure(SecurityException("Только администраторы могут закрывать сборы"))
                response.status == HttpStatusCode.NotFound ->
                    Result.failure(NoSuchElementException("Сбор не найден"))
                else -> {
                    val errorText = response.bodyAsText()
                    Log.e(TAG, "POST failed: ${response.status} - $errorText")
                    Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in closeFundraise", e)
            Result.failure(e)
        }
    }

    // ✅ 5. Отметить оплату (участник платит)
    // ⚠️ Эндпоинт: POST /api/fundraises/{fundraiseId}/mark-paid
    // 📝 Создаёт запись со статусом Pending
    suspend fun markPaid(fundraiseId: String): Result<Unit> {
        return try {
            val rawToken = tokenManager.getToken() ?: return Result.failure(Exception("Не авторизован"))
            val token = cleanToken(rawToken)

            val url = "$baseUrl$FUNDRAISES_BASE/$fundraiseId/mark-paid"
            Log.d(TAG, "POST $url")

            val response = client.post(url) {
                header("Authorization", "Bearer $token")
            }

            when {
                response.status.isSuccess() -> Result.success(Unit)
                response.status == HttpStatusCode.Conflict ->
                    Result.failure(IllegalStateException("Вы уже отметили оплату"))
                response.status == HttpStatusCode.Forbidden ->
                    Result.failure(SecurityException("Нет доступа к событию"))
                response.status == HttpStatusCode.NotFound ->
                    Result.failure(NoSuchElementException("Сбор не найден"))
                response.status == HttpStatusCode.BadRequest -> {
                    val errorText = response.bodyAsText()
                    Result.failure(Exception(errorText.ifBlank { "Ошибка: ${response.status}" }))
                }
                else -> {
                    val errorText = response.bodyAsText()
                    Log.e(TAG, "POST failed: ${response.status} - $errorText")
                    Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in markPaid", e)
            Result.failure(e)
        }
    }

    // ✅ 6. Подтвердить оплату участника (админ)
    // ⚠️ Эндпоинт: POST /api/fundraises/{fundraiseId}/participants/{userId}/confirm
    // ⚠️ Тело запроса: голое число или null (НЕ объект!)
    // 🔐 Только Admin/CoAdmin
    suspend fun confirmPayment(fundraiseId: String, targetUserId: String, amount: BigDecimal?): Result<Unit> {
        return try {
            if (amount != null && amount < BigDecimal.ZERO) {
                return Result.failure(IllegalArgumentException("Сумма не может быть отрицательной"))
            }

            val rawToken = tokenManager.getToken() ?: return Result.failure(Exception("Не авторизован"))
            val token = cleanToken(rawToken)

            val url = "$baseUrl$FUNDRAISES_BASE/$fundraiseId/participants/$targetUserId/confirm"
            Log.d(TAG, "POST $url with amount=$amount")

            val response = client.post(url) {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                // ✅ КРИТИЧНО: отправляем голый примитив, не объект!
                // Бэкенд: [FromBody] decimal? amount
                setBody(amount?.toDouble()?.let { JsonPrimitive(it) } ?: JsonPrimitive(null))
            }

            when {
                response.status.isSuccess() -> Result.success(Unit)
                response.status == HttpStatusCode.Forbidden ->
                    Result.failure(SecurityException("Только администраторы могут подтверждать оплаты"))
                response.status == HttpStatusCode.NotFound ->
                    Result.failure(NoSuchElementException("Участник или сбор не найден"))
                response.status == HttpStatusCode.BadRequest -> {
                    val errorText = response.bodyAsText()
                    Result.failure(IllegalArgumentException(errorText.ifBlank { "Неверные данные" }))
                }
                else -> {
                    val errorText = response.bodyAsText()
                    Log.e(TAG, "POST failed: ${response.status} - $errorText")
                    Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in confirmPayment", e)
            Result.failure(e)
        }
    }

    // ✅ 7. Отменить подтверждение оплаты (админ)
    // ⚠️ Эндпоинт: POST /api/fundraises/{fundraiseId}/participants/{userId}/revert
    // 🔐 Только Admin/CoAdmin
    suspend fun revertPayment(fundraiseId: String, targetUserId: String): Result<Unit> {
        return try {
            val rawToken = tokenManager.getToken() ?: return Result.failure(Exception("Не авторизован"))
            val token = cleanToken(rawToken)

            val url = "$baseUrl$FUNDRAISES_BASE/$fundraiseId/participants/$targetUserId/revert"
            Log.d(TAG, "POST $url")

            val response = client.post(url) {
                header("Authorization", "Bearer $token")
            }

            when {
                response.status.isSuccess() -> Result.success(Unit)
                response.status == HttpStatusCode.Forbidden ->
                    Result.failure(SecurityException("Только администраторы могут отменять подтверждения"))
                response.status == HttpStatusCode.NotFound ->
                    Result.failure(NoSuchElementException("Участник или сбор не найден"))
                else -> {
                    val errorText = response.bodyAsText()
                    Log.e(TAG, "POST failed: ${response.status} - $errorText")
                    Result.failure(Exception(errorText.ifBlank { "Ошибка сервера: ${response.status}" }))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in revertPayment", e)
            Result.failure(e)
        }
    }

    // ✅ Утилиты
    fun getCurrentUserId(): String? {
        return try {
            val rawToken = tokenManager.getToken() ?: return null
            decodeUserId(cleanToken(rawToken))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current userId", e)
            null
        }
    }

    fun close() { client.close() }
}