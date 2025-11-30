package com.example.shabasher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shabasher.Model.Participant
import com.example.shabasher.Model.ParticipationStatus
import com.example.shabasher.Screens.EventContent
import com.example.shabasher.Screens.LoadingScreen
import com.example.shabasher.ui.theme.ShabasherTheme


fun ParticipantToString(status: ParticipationStatus): String {
    when {
        status == ParticipationStatus.GOING -> return "Придет"
        status == ParticipationStatus.NOT_GOING -> return "Не придет"
        status == ParticipationStatus.INVITED -> return "Приглашен"
        else -> return "Приглашен"
    }
}

@Composable
fun ParticipatorsCard(
    participants: List<Participant>
) {
    val showList = participants.take(3)
    val hasMore = participants.size > 3

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(top = 16.dp, start = 8.dp, end = 8.dp)
            .fillMaxWidth()
    ) {

        Text(
            "Участники",
            style = MaterialTheme.typography.titleMedium
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            showList.forEach { participant ->
                ParticipatorElem(
                    name = participant.name,
                    status = ParticipantToString(participant.status)
                )
            }

            if (hasMore) {
                IconButton(onClick = {  }) {
                    Icon(
                        Icons.Default.MoreHoriz,
                        contentDescription = "Ещё"
                    )
                }
            }
        }
    }
}

@Composable
fun ParticipatorElem(
    name: String = "Участник",
    status: String = "Придет"
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface
                )
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Add photo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                name,
                style = MaterialTheme.typography.titleMedium)
        }

        Text(status, color = MaterialTheme.colorScheme.error)
    }
}
