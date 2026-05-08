package com.example.shabasher.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun String.formatAsRussianDate(): String {
    return LocalDate.parse(this)
        .format(DateTimeFormatter.ofPattern("d MMMM yyyy 'г.'", Locale("ru", "RU")))
}
@Composable
fun EventInfo(
    title: String = "Заголовок",
    description: String = "Описание Описание ОписаниеОписание ОписаниеОписание ОписаниеОписание Описание Описание Описание ОписаниеОписаниеОписание Описание Описание Описание Описание"
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 24.dp)
            .fillMaxWidth()

    ) {
        Box(
            modifier = Modifier
                .size(340.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Image,
                contentDescription = "Add photo",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(100.dp)
            )

        }

        Text(title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center)
        Text(description,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventMoreInfo(
    date: String = "2026-12-12",
    place: String = "г. Красный Луч, ул. Маяковского 10",
    time: String = "22:00",
    actions: @Composable (() -> Unit)? = null
) {
    val parsedDate = remember(date) { parseRussianDate(date) }
    val dateText = parsedDate?.let { "${it.dayOfMonth} ${it.monthFull} ${it.year} г." }
        ?: runCatching { date.formatAsRussianDate() }.getOrDefault(date)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoLineRow(
            icon = Icons.Default.CalendarMonth,
            value = dateText.ifBlank { "Дата не указана" }
        )
        InfoLineRow(
            icon = Icons.Default.AccessTime,
            value = time.ifBlank { "Время не указано" }
        )
        InfoLineRow(
            icon = Icons.Default.LocationOn,
            value = place.ifBlank { "Адрес не указан" }
        )

        actions?.let {
            Spacer(Modifier.height(2.dp))
            it()
        }
    }
}

@Composable
private fun InfoLineRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoIcon(icon)
        Spacer(Modifier.width(14.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = androidx.compose.foundation.shape.CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ═════════════════════════════════════════════════
// Парсинг даты
// ═════════════════════════════════════════════════

private data class ParsedRusDate(
    val dayOfMonth: Int,
    val year: Int,
    val monthFull: String,
    val weekdayFull: String
)

private val russianMonthsFull = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)

private val russianWeekdaysFull = listOf(
    "понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресенье"
)

@RequiresApi(Build.VERSION_CODES.O)
private fun parseRussianDate(iso: String): ParsedRusDate? {
    if (iso.isBlank()) return null
    return try {
        val date = LocalDate.parse(iso.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
        val monthIdx = date.monthValue - 1
        val weekdayIdx = date.dayOfWeek.value - 1
        ParsedRusDate(
            dayOfMonth = date.dayOfMonth,
            year = date.year,
            monthFull = russianMonthsFull[monthIdx],
            weekdayFull = russianWeekdaysFull[weekdayIdx]
        )
    } catch (_: Exception) {
        null
    }
}