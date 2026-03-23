package com.example.shabasher.data.network

import com.example.shabasher.Model.Suggestion
import com.example.shabasher.Model.VoteResult

interface SuggestionsRepository {

    suspend fun getSuggestions(eventId: String): List<Suggestion>

    suspend fun createSuggestion(
        eventId: String,
        text: String
    ): Suggestion

    suspend fun vote(
        suggestionId: Int,
        action: String
    ): VoteResult

    suspend fun deleteSuggestion(
        suggestionId: Int
    )
}