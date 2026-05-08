package com.example.shabasher.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "IntentHelpers"

object CalendarIntents {

    /**
     * Открывает системный диалог «Добавить событие» в установленном календаре
     * (Google Calendar / Samsung / Mi и т.п.). Пользователь сам выбирает свой
     * календарь и подтверждает.
     *
     * @param dateIso дата в формате yyyy-MM-dd
     * @param timeIso время в формате HH:mm (или HH:mm:ss)
     */
    fun addEventToCalendar(
        context: Context,
        title: String,
        description: String?,
        location: String?,
        dateIso: String,
        timeIso: String?,
        durationMinutes: Long = 120
    ) {
        val startMillis = parseStartMillis(dateIso, timeIso) ?: run {
            Toast.makeText(context, "Не удалось определить дату события", Toast.LENGTH_SHORT).show()
            return
        }
        val endMillis = startMillis + durationMinutes * 60_000L

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description.orEmpty())
            putExtra(CalendarContract.Events.EVENT_LOCATION, location.orEmpty())
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.HAS_ALARM, 1)
            // Запрос на стандартный reminder за 30 минут — большинство календарей это поддерживают
            putExtra(CalendarContract.Reminders.MINUTES, 30)
        }

        try {
            context.startActivity(intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        } catch (e: Exception) {
            Log.e(TAG, "No calendar app", e)
            Toast.makeText(
                context,
                "На устройстве не найдено приложение календаря",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun parseStartMillis(dateIso: String, timeIso: String?): Long? {
        if (dateIso.isBlank()) return null
        return try {
            val date = LocalDate.parse(dateIso, DateTimeFormatter.ISO_LOCAL_DATE)
            val time = if (timeIso.isNullOrBlank()) {
                LocalTime.of(12, 0)
            } else {
                val cleaned = timeIso.take(5) // HH:mm
                LocalTime.parse(cleaned, DateTimeFormatter.ofPattern("HH:mm"))
            }
            LocalDateTime.of(date, time)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse date '$dateIso' time '$timeIso'", e)
            null
        }
    }
}

object MapIntents {

    /**
     * Открывает любое установленное навигационное приложение (Яндекс.Карты,
     * 2ГИС, Google Maps) с точкой по адресу. Использует стандартную схему `geo:`,
     * которую поддерживают все крупные карты в РФ.
     */
    fun openInNavigator(context: Context, address: String) {
        if (address.isBlank()) {
            Toast.makeText(context, "Адрес не указан", Toast.LENGTH_SHORT).show()
            return
        }
        val encoded = Uri.encode(address)
        val geoUri = Uri.parse("geo:0,0?q=$encoded")

        val intent = Intent(Intent.ACTION_VIEW, geoUri)
        try {
            context.startActivity(Intent.createChooser(intent, "Открыть карту в…"))
        } catch (_: Exception) {
            // Fallback на Google Maps URL — откроется в браузере
            val fallback = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded")
            )
            try {
                context.startActivity(fallback.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            } catch (e: Exception) {
                Toast.makeText(context, "Не удалось открыть карту", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
