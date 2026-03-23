package com.example.shabasher.Model

data class Suggestion(
    val id: Int,
    val userId: String,
    val userName: String,
    val avatar: String,
    val text: String,
    val likes: Int,
    val dislikes: Int,
    val timestamp: String,
    val liked: Boolean,
    val disliked: Boolean
)

data class VoteResult(
    val likes: Int,
    val dislikes: Int,
    val liked: Boolean,
    val disliked: Boolean
)