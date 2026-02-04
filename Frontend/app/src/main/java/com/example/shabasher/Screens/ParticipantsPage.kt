package com.example.shabasher.Screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.Participant
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.Model.UserRole
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
    val vm = remember(eventId) {
        EventViewModel(context).also {
            it.loadEvent(eventId)
        }
    }

    val uiState = vm.ui.value
    val currentUserId = vm.currentUserId.toString()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Участники") },
                navigationIcon = {
                    IconButton(onClick = { SafeNavigation.navigate { navController.popBackStack() } }) {
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
                    currentUserRole = uiState.event.currentUserRole,
                    currentUserId = currentUserId,
                    onKick = { userId ->
                        uiState.event?.id?.let { eventId ->
                            vm.kickParticipant(eventId, userId)
                        }
                    },
                    onMakeAdmin = { userId ->
                        uiState.event?.id?.let { eventId ->
                            vm.makeAdmin(eventId, userId)
                        }
                    },
                    onMakeModerator = { userId ->
                        uiState.event?.id?.let { eventId ->
                            vm.makeModerator(eventId, userId)
                        }
                    },
                    onRevokeRole = { userId ->
                        uiState.event?.id?.let { eventId ->
                            vm.revokeRole(eventId, userId)
                        }
                    },
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    navController = navController
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
    currentUserRole: UserRole,
    currentUserId: String,
    onKick: (String) -> Unit = {},
    onMakeAdmin: (String) -> Unit = {},
    onMakeModerator: (String) -> Unit = {},
    onRevokeRole: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    navController: NavController
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(participants) { participant ->
            ParticipantRow(
                participant = participant,
                currentUserRole = currentUserRole,
                currentUserId = currentUserId,
                onKick = onKick,
                onMakeAdmin = onMakeAdmin,
                onMakeModerator = onMakeModerator,
                onRevokeRole = onRevokeRole,
                onParticipantClick = { userId ->
                    if (userId == currentUserId) {
                        // Это я — идём на свой профиль без ID
                        navController.navigate(Routes.PROFILE)
                    } else {
                        // Чужой — с ID
                        navController.navigate("profile/$userId")
                    }
                }
            )
        }
    }
}

@Composable
fun ParticipantRow(
    participant: Participant,
    currentUserRole: UserRole,
    currentUserId: String, // ← нужно знать, это я или нет
    onKick: (String) -> Unit = {},
    onMakeAdmin: (String) -> Unit = {},
    onMakeModerator: (String) -> Unit = {},
    onRevokeRole: (String) -> Unit = {}, // для разжалования модератора
    modifier: Modifier = Modifier,
    onParticipantClick: (String) -> Unit = {}
) {
    var showContextMenu by remember { mutableStateOf(false) }

    // Определяем, можно ли показывать меню и какие пункты
    val isSelf = participant.id == currentUserId
    var menuItems by remember { mutableStateOf<List<MenuItem>>(emptyList()) }

    // Пересчитываем доступные действия при изменении ролей
    LaunchedEffect(currentUserRole, participant.role, isSelf) {
        menuItems = when {
            isSelf -> emptyList() // нельзя взаимодействовать с собой

            currentUserRole == UserRole.ADMIN -> {
                when (participant.role) {
                    UserRole.MEMBER -> listOf(
                        MenuItem("Сделать организатором", { onMakeAdmin(participant.id) }),
                        MenuItem("Сделать модератором", { onMakeModerator(participant.id) }),
                        MenuItem("Исключить", { onKick(participant.id) })
                    )
                    UserRole.MODERATOR -> listOf(
                        MenuItem("Сделать организатором", { onMakeAdmin(participant.id) }),
                        MenuItem("Разжаловать", { onRevokeRole(participant.id) }),
                        MenuItem("Исключить", { onKick(participant.id) })
                    )
                    UserRole.ADMIN -> emptyList() // других админов не трогаем
                }
            }

            currentUserRole == UserRole.MODERATOR && participant.role == UserRole.MEMBER -> {
                listOf(MenuItem("Удалить из группы", { onKick(participant.id) }))
            }

            else -> emptyList()
        }
    }

    val canShowMenu = menuItems.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onParticipantClick(participant.id) },
                onLongClick = {
                    if (canShowMenu) {
                        showContextMenu = true
                    }
                }
            )
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
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

            // Роль справа
            if (participant.role == UserRole.ADMIN) {
                Text(
                    text = "Организатор",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else if (participant.role == UserRole.MODERATOR) {
                Text(
                    text = "Модератор",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Контекстное меню
        if (showContextMenu) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { showContextMenu = false }
            ) {
                menuItems.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.text) },
                        onClick = {
                            showContextMenu = false
                            item.action()
                        }
                    )
                }
            }
        }
    }
}

// Вспомогательный класс для пунктов меню
private data class MenuItem(val text: String, val action: () -> Unit)
