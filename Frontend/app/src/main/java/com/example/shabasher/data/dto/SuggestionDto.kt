package com.example.shabasher.data.dto

import com.example.shabasher.Model.Suggestion
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date



@Serializable
data class SuggestionResponseDto(
    val id: String,
    val userId: String,
    val userName: String,

    @SerialName("description")  // ← Маппим JSON "description" на поле "text"
    val text: String,

    val likes: Int,
    val dislikes: Int,

    @SerialName("createdAt")    // ← Маппим JSON "createdAt" на поле "timestamp"
    val timestamp: String,      // ← DateTime с сервера придёт как ISO-строка

    val liked: Boolean,
    val disliked: Boolean
) {
    fun toDomain() = Suggestion(
        id = id,
        userId = userId,
        userName = userName,
        text = text,
        likes = likes,
        dislikes = dislikes,
        timestamp = timestamp,
        liked = liked,
        disliked = disliked
    )
}

@Serializable
data class SuggestionsListResponseDto(
    val suggestions: List<SuggestionResponseDto>
) {
    fun toDomain() = suggestions.map { it.toDomain() }
}

@Serializable
data class VoteResultDto(
    val likes: Int,
    val dislikes: Int,
    val liked: Boolean,
    val disliked: Boolean
)
