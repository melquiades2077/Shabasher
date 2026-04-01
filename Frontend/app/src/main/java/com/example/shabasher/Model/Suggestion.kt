package com.example.shabasher.Model

import java.util.Date

data class Suggestion(
    val id: String,
    val userId: String,
    val userName: String,
    val text: String,
    val likes: Int,
    val dislikes: Int,
    val timestamp: String,
    val liked: Boolean,
    val disliked: Boolean
)