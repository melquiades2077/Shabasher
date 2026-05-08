package com.example.shabasher.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shabasher.utils.CalendarIntents
import com.example.shabasher.utils.MapIntents

/**
 * Кнопка «Добавить в календарь». Стиль соответствует карточкам инфо-блока
 * EventPage: surface-цвет, скруглённые углы 20dp, иконка primary.
 */
@Composable
fun AddToCalendarButton(
    title: String,
    description: String?,
    location: String?,
    dateIso: String,
    timeIso: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (dateIso.isBlank()) return

    FilledTonalButton(
        onClick = {
            CalendarIntents.addEventToCalendar(
                context = context,
                title = title,
                description = description,
                location = location,
                dateIso = dateIso,
                timeIso = timeIso
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(20.dp),
        // Серая «пассивная» подложка, но иконка акцентного цвета
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Добавить в календарь",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Кнопка «Открыть в картах». Стиль 1-в-1 как AddToCalendarButton.
 */
@Composable
fun OpenInNavigatorButton(
    address: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (address.isBlank()) return

    FilledTonalButton(
        onClick = { MapIntents.openInNavigator(context, address) },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(20.dp),
        // Серая «пассивная» подложка, но иконка акцентного цвета
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            Icons.Default.Map,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Открыть в картах",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
