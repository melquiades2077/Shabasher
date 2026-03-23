package com.example.shabasher.data.network

import com.example.shabasher.Model.Suggestion
import com.example.shabasher.Model.VoteResult
import kotlinx.coroutines.delay
import kotlin.random.Random

class FakeSuggestionsRepository : SuggestionsRepository {

    private val suggestions = mutableListOf(
        Suggestion(
            id = 1,
            userId = "user_1",
            userName = "Alex",
            avatar = "A",
            text = "Давайте поедем на природу",
            likes = 4,
            dislikes = 1,
            timestamp = "now",
            liked = false,
            disliked = false
        ),
        Suggestion(
            id = 2,
            userId = "user_2",
            userName = "Maria",
            avatar = "M",
            text = "Сходить в квест",
            likes = 2,
            dislikes = 0,
            timestamp = "now",
            liked = false,
            disliked = false
        )
    )

    override suspend fun getSuggestions(eventId: String): List<Suggestion> {
        delay(300)
        return suggestions.sortedByDescending { it.id }
    }

    override suspend fun createSuggestion(eventId: String, text: String): Suggestion {
        delay(200)

        val suggestion = Suggestion(
            id = Random.nextInt(),
            userId = "user_1",
            userName = "You",
            avatar = "Y",
            text = text,
            likes = 0,
            dislikes = 0,
            timestamp = "now",
            liked = false,
            disliked = false
        )

        suggestions.add(0, suggestion)

        return suggestion
    }

    override suspend fun vote(suggestionId: Int, action: String): VoteResult {

        val index = suggestions.indexOfFirst { it.id == suggestionId }
        val item = suggestions[index]

        var likes = item.likes
        var dislikes = item.dislikes
        var liked = item.liked
        var disliked = item.disliked

        when (action) {

            "like" -> {
                when {
                    liked -> {
                        likes--
                        liked = false
                    }
                    disliked -> {
                        dislikes--
                        likes++
                        disliked = false
                        liked = true
                    }
                    else -> {
                        likes++
                        liked = true
                    }
                }
            }

            "dislike" -> {
                when {
                    disliked -> {
                        dislikes--
                        disliked = false
                    }
                    liked -> {
                        likes--
                        dislikes++
                        liked = false
                        disliked = true
                    }
                    else -> {
                        dislikes++
                        disliked = true
                    }
                }
            }
        }

        suggestions[index] = item.copy(
            likes = likes,
            dislikes = dislikes,
            liked = liked,
            disliked = disliked
        )

        return VoteResult(likes, dislikes, liked, disliked)
    }

    override suspend fun deleteSuggestion(suggestionId: Int) {
        suggestions.removeIf { it.id == suggestionId }
    }
}