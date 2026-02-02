package com.example.shabasher.Screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.Participant
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.ViewModels.EventViewModel
import com.example.shabasher.components.ParticipantToString
import com.example.shabasher.components.ParticipatorElem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsPage(
    navController: NavController,
    eventId: String,
    context: Context = LocalContext.current
) {
    // Создаём ViewModel вручную и запоминаем его на время жизни экрана
    val vm = remember(eventId) {
        EventViewModel(context).also {
            it.loadEvent(eventId)
        }
    }

    val uiState = vm.ui.value // ← mutableStateOf, поэтому просто .value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Участники") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.event != null -> {
                ParticipantsList(
                    participants = uiState.event.participants,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                )
            }

            uiState.error != null -> {
                Text(
                    uiState.error,
                    modifier = Modifier.padding(innerPadding),
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                Text("Нет данных", modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun ParticipantsList(
    participants: List<Participant>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(participants) { p ->
            ParticipantRow(p)
        }
    }
}

@Composable
fun ParticipantRow(participant: Participant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(participant.name, style = MaterialTheme.typography.titleMedium)
            Text(
                ParticipantToString(participant.status),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
