package com.example.shabasher.data

import kotlinx.coroutines.delay

class EventInvitesRepository {

    // Заглушка
    suspend fun getInviteLink(eventId: String): String {
        delay(100) // имитация API
        return "https://shabasher.app/j/$eventId"
    }
}
