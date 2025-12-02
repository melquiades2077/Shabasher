package com.example.shabasher.data

import com.example.shabasher.Model.EventData
import com.example.shabasher.Model.EventShort
import com.example.shabasher.Model.EventFull
import com.example.shabasher.Model.Participant
import com.example.shabasher.Model.ParticipationStatus
import kotlinx.coroutines.delay
import kotlin.random.Random

class EventsRepository {

    // TODO: заменить на запрос к backend
    suspend fun getEvents(): Result<List<EventShort>> {
        delay(300) // имитация сети

        val mock = listOf(
            EventShort(
                id = "1",
                title = "Корпоратив",
                date = "26 февраля 2026",
                status = "Событие завершено"
            ),
            EventShort(
                id = "2",
                title = "ДР Олега",
                date = "10 марта 2026",
                status = "Активно"
            )
        )

        return Result.success(mock)
    }

    // TODO: отправка на backend
    suspend fun createEvent(
        title: String,
        description: String,
        address: String,
        date: String,
        time: String
    ): Result<String> {
        delay(200)

        val newId = Random.nextInt(1000, 9999).toString()
        return Result.success(newId)
    }

    // TODO: запрос с backend
    suspend fun getEventById(id: String): Result<EventData> {
        delay(300)

        val mock = EventData(
            id = id,
            title = "Корпоратив",
            description = "Описание события...",
            date = "26 февраля 2026 г.",
            place = "г. Ростов-на-Дону, ул. Пушкина 12",
            time = "22:00",
            participants = listOf(
                Participant("1", "Андрей", ParticipationStatus.GOING),
                Participant("2", "Катя", ParticipationStatus.NOT_GOING),
                Participant("3", "Женя", ParticipationStatus.INVITED)
            ),
            userStatus = ParticipationStatus.INVITED
        )

        return Result.success(mock)
    }

}


