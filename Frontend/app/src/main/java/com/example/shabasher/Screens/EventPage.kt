package com.example.shabasher.Screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.Participant
import com.example.shabasher.ViewModels.EventViewModel
import com.example.shabasher.components.EventInfo
import com.example.shabasher.components.EventMoreInfo
import com.example.shabasher.components.GamesCard
import com.example.shabasher.components.ParticipationSelector
import com.example.shabasher.components.ParticipatorsCard
import com.example.shabasher.components.ServiceCard
import com.example.shabasher.Model.Routes
import com.example.shabasher.ViewModels.EventUiState

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventPage(
    navController: NavController,
    eventId: String,
    viewModel: EventViewModel = viewModel()
) {
    val ui = viewModel.ui.value

    var leaveEventId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(eventId) {
        println("[EventPage] Загрузка события с ID: '$eventId'")
        viewModel.loadEvent(eventId)
    }

    LaunchedEffect(leaveEventId) {
        val id = leaveEventId ?: return@LaunchedEffect
        val result = viewModel.leaveFromEventSuspend(id) // ← сделаем suspend-версию!
        leaveEventId = null // сбросить триггер

        when {
            result.isSuccess -> {
                // Опционально: обновить данные (перезагрузить событие)
                // viewModel.loadEvent(id)

                // Навигация
                navController.navigate(Routes.MAIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
            result.isFailure -> {
                // показать ошибку
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = ""
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    var expanded by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = {
                            ui.event?.id?.let { eventId ->
                                navController.navigate("${Routes.SHAREEVENT}/$eventId")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Поделиться")
                    }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Ещё")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .width(IntrinsicSize.Min)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        expanded = false
                                        leaveEventId = eventId // ← триггер
                                    }
                                    .padding(horizontal = 17.dp, vertical = 8.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp) // отступ между иконкой и текстом
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = null, // или "Выйти из события"
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Выйти из события",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        when {
            ui.isLoading -> {
                LoadingScreen()
            }

            ui.event != null -> {
                EventContent(
                    event = ui.event,
                    navController = navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    vm = viewModel
                )
            }

            ui.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ui.error!!, color = MaterialTheme.colorScheme.error)
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Событие не найдено")
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventContent(
    event: com.example.shabasher.Model.EventData,
    navController: NavController,
    modifier: Modifier,
    vm: EventViewModel
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            EventInfo(event.title, event.description)
        }

        item {
            EventMoreInfo(event.date, event.place, event.time)
        }

        item {
            ParticipatorsCard(
                participants = event.participants,
                onClick = {
                    vm.ui.value.event?.id?.let { eventId ->
                        navController.navigate("${Routes.PARTICIPANTS}/$eventId")
                    }
                }
            )
        }

        item {
            ParticipationSelector(
                selected = event.userStatus,
                isUpdating = vm.ui.value.isUpdatingStatus || vm.ui.value.isJoining, // ← блокируем, если joining
                onSelect = { status ->
                    vm.updateMyParticipationStatus(status)
                }
            )
        }

        item {
            ServiceCard()
        }

        item {
            GamesCard()
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
    }
}
