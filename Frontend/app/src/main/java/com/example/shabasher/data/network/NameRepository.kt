package com.example.shabasher.data.network

import com.example.shabasher.data.dto.SetNameRequest
import com.example.shabasher.data.dto.SetNameResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class NameRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val baseUrl = Config.BASE_URL

    suspend fun setUserName(name: String, token: String): Result<SetNameResponse> {
        println("USING TOKEN: ${token.take(20)}...")
        return try {
            println("Отправка имени '$name' с токеном: ${token.take(10)}...")

            val response: HttpResponse = client.post("$baseUrl/api/Auth/set-name") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(SetNameRequest(name))
            }

            println("Ответ сервера: ${response.status}")

            if (response.status.isSuccess()) {
                val result: SetNameResponse = response.body()
                println("Имя сохранено: $result")
                Result.success(result)
            } else {
                val errorText = response.body<String>()
                println("Ошибка сервера: $errorText")
                Result.failure(Exception(errorText))
            }
        } catch (e: Exception) {
            println("Ошибка сети: ${e.message}")
            Result.failure(e)
        }
    }
}