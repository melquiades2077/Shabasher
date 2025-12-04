package com.example.shabasher.data.network

import android.content.Context
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class NameRepository(private val context: Context) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val baseUrl = Config.BASE_URL

    suspend fun setUserName(userId: String, name: String, token: String): Result<Unit> {
        return try {
            val body = UpdateUserNameRequest(id = userId, name = name)

            val response: HttpResponse = client.patch("$baseUrl/api/Users") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(body)
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val message = response.body<String>()
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class UpdateUserNameRequest(
    val id: String,
    val name: String
)
