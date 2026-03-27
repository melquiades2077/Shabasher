package com.example.shabasher.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object DateTimeUtils {

    private val serverFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
    private val displayFormatter = DateTimeFormatter.ofPattern("dd.MM HH:mm")

    /**
     * Парсит серверный формат "2026-03-24T06:20:47.4242422" и возвращает относительное время
     * Примеры: "только что", "5 мин назад", "вчера", "24.03 14:30"
     */
    fun formatRelativeTime(isoTimestamp: String): String {
        return try {
            val cleaned = isoTimestamp.replace("Z", "")
            val instant = java.time.Instant.parse(
                if (cleaned.length > 23) cleaned.take(23) + "Z" else cleaned + "Z"
            )

            val now = java.time.Instant.now()
            val minutes = java.time.temporal.ChronoUnit.MINUTES.between(instant, now)
            val hours = java.time.temporal.ChronoUnit.HOURS.between(instant, now)
            val days = java.time.temporal.ChronoUnit.DAYS.between(instant, now)

            when {
                minutes < 1 -> "только что"
                minutes < 60 -> "$minutes мин назад"
                hours < 24 -> "$hours ч назад"
                days == 1L -> "вчера"
                days < 7 -> "$days дн назад"
                else -> instant.atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM HH:mm"))
            }
        } catch (e: Exception) {
            isoTimestamp.takeIf { it.isNotBlank() } ?: "неизвестно"
        }
    }

    /**
     * Возвращает исходный ISO-таймстемп для сортировки/сравнения
     */
    fun parseIsoTimestamp(isoTimestamp: String): Instant? {
        return try {
            val cleaned = isoTimestamp.replace("Z", "")
            Instant.parse(if (cleaned.length > 23) cleaned.take(23) + "Z" else cleaned + "Z")
        } catch (e: Exception) {
            null
        }
    }
}