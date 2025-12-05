package com.example.shabasher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.shabasher.Model.ParticipationStatus
import com.example.shabasher.Screens.EventPage
import com.example.shabasher.ui.theme.ShabasherTheme


@Composable
fun ParticipationSelector(
    selected: ParticipationStatus,
    isUpdating: Boolean = false,
    onSelect: (ParticipationStatus) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.background( color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp) )
            .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            "Ваш статус",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ParticipationButton(
                text = "Приду",
                active = selected == ParticipationStatus.GOING,
                isUpdating = isUpdating,
                onClick = { onSelect(ParticipationStatus.GOING) }
            )

            ParticipationButton(
                text = "Не смогу",
                active = selected == ParticipationStatus.NOT_GOING,
                isUpdating = isUpdating,
                onClick = { onSelect(ParticipationStatus.NOT_GOING) }
            )
        }
    }
}



@Composable
fun ParticipationButton(text: String, active: Boolean, isUpdating: Boolean = false, onClick: () -> Unit) {
    val bg = if (active)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val fg = if (active)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(bg)
            .clickable(enabled = !isUpdating, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .width(115.dp)
            .height(30.dp),
        Alignment.Center

    ) {
        Text(text, color = fg)
    }
}

@Preview(showBackground = true)
@Composable
fun ParticipationSelectorPreview() {
    ShabasherTheme(darkTheme = true) {
        ParticipationSelector(
            selected = ParticipationStatus.GOING,
            isUpdating = false,
            onSelect = { }
        )
    }
}


