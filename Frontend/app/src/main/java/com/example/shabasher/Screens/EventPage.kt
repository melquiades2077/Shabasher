package com.example.shabasher.Screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.Model.UserRole
import com.example.shabasher.R
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
    val currentUserId = viewModel.currentUserId

    // Управление диалогами
    var showCannotLeaveDialog by remember { mutableStateOf(false) }
    var showConfirmLeaveDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) } // ← НОВОЕ

    var leaveEventId by remember { mutableStateOf<String?>(null) }
    var deleteEventId by remember { mutableStateOf<String?>(null) } // ← НОВОЕ

    LaunchedEffect(eventId) {
        println("[EventPage] Загрузка события с ID: '$eventId'")
        viewModel.loadEvent(eventId)
    }

    // Обработка выхода
    LaunchedEffect(leaveEventId) {
        val id = leaveEventId ?: return@LaunchedEffect
        val result = viewModel.leaveFromEventSuspend(id)
        leaveEventId = null

        when {
            result.isSuccess -> {
                navController.navigate(Routes.MAIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
            result.isFailure -> {
                // Опционально: показать ошибку
            }
        }
    }

    // Обработка удаления события
    LaunchedEffect(deleteEventId) {
        val id = deleteEventId ?: return@LaunchedEffect
        viewModel.deleteEvent(id)
        deleteEventId = null

        // После удаления — сразу на главный экран
        navController.navigate(Routes.MAIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    // Диалог ошибки (последний админ при участниках > 1)
    if (showCannotLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showCannotLeaveDialog = false },
            title = {
                Text(
                    "Нельзя покинуть событие",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Вы организатор события.\nПеред выходом назначьте другого организатора в списке участников.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            },
            confirmButton = {
                TextButton(onClick = { showCannotLeaveDialog = false }) {
                    Text(
                        "Понятно",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                )
            }
        )
    }

    // Диалог подтверждения выхода
    if (showConfirmLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmLeaveDialog = false },
            title = {
                Text(
                    "Покинуть событие?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Вы уверены, что хотите покинуть «${viewModel.ui.value.event?.title}»?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmLeaveDialog = false
                        leaveEventId = eventId
                    }
                ) {
                    Text(
                        "Выйти",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmLeaveDialog = false }) {
                    Text(
                        "Отмена",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                )
            }
        )
    }

    // Диалог подтверждения УДАЛЕНИЯ (только для админов)
    if (showConfirmDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = {
                Text(
                    "Удалить событие?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Это действие необратимо! Все участники потеряют доступ к событию, а вся информация будет удалена без возможности восстановления.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDeleteDialog = false
                        deleteEventId = eventId // ← триггер удаления
                    }
                ) {
                    Text(
                        "Удалить",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text(
                        "Отмена",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                )
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = { SafeNavigation.navigate { navController.popBackStack() } }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Кнопка "Поделиться" остаётся в тулбаре
                    IconButton(
                        onClick = {
                            ui.event?.id?.let { eventId ->
                                navController.navigate("${Routes.SHAREEVENT}/$eventId")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Поделиться")
                    }

                    // Меню "Ещё"
                    var expanded by remember { mutableStateOf(false) }
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
                            // 👇 ПУНКТ "РЕДАКТИРОВАТЬ СОБЫТИЕ" — ТОЛЬКО ДЛЯ АДМИНА
                            if (ui.event?.currentUserRole == UserRole.ADMIN) {
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            expanded = false
                                            ui.event?.id?.let { eventId ->
                                                navController.navigate("${Routes.EDITEVENT}/$eventId")
                                            }
                                        }
                                        .padding(horizontal = 17.dp, vertical = 8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Редактировать событие",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // 👇 Пункт "Выйти из события" — для всех НЕ-админов ИЛИ админов с >1 участником
                            val event = ui.event
                            if (event != null) {
                                val isCurrentUserAdmin = event.currentUserRole == UserRole.ADMIN
                                val totalParticipants = event.participants.size

                                // Показываем "Выйти", если:
                                // - пользователь НЕ админ → всегда можно выйти
                                // - пользователь админ, но участников > 1 → можно выйти (оставив других)
                                if (!isCurrentUserAdmin || (isCurrentUserAdmin && totalParticipants > 1)) {
                                    Box(
                                        modifier = Modifier
                                            .clickable {
                                                expanded = false

                                                val myRole = event.currentUserRole
                                                val participants = event.participants

                                                // 🔒 ЗАЩИТА: последний админ не может уйти, если участников > 1
                                                if (myRole == UserRole.ADMIN) {
                                                    val adminCount = participants.count { it.role == UserRole.ADMIN }
                                                    if (adminCount == 1 && participants.size > 1) {
                                                        showCannotLeaveDialog = true
                                                        return@clickable
                                                    }
                                                }

                                                // ⚠️ Подтверждение выхода — ВСЕГДА
                                                showConfirmLeaveDialog = true
                                            }
                                            .padding(horizontal = 17.dp, vertical = 8.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ExitToApp,
                                                contentDescription = null,
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

                            // 👇 Пункт "Удалить событие" — ТОЛЬКО для админов (всегда доступен, даже если один)
                            if (ui.event?.currentUserRole == UserRole.ADMIN) {
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            expanded = false
                                            showConfirmDeleteDialog = true
                                        }
                                        .padding(horizontal = 17.dp, vertical = 8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Удалить событие",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Не удалось присоединиться к событию", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Box(
                            modifier = Modifier.size(300.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Image(
                                painter = painterResource(id = com.example.shabasher.R.drawable.manulnotlogin),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            // Градиент снизу
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.scrim
                                            ),
                                            startY = 0f,
                                            endY = Float.POSITIVE_INFINITY
                                        )
                                    )
                            )
                        }
                        Text(ui.error!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }

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
            ServiceCard(
                navController = navController,
                eventId = event.id,
                currentUserRole = event.currentUserRole // ← Передаём роль
            )
        }

        /*item {
            GamesCard()
        }*/
        item {
            Spacer(Modifier.height(8.dp))
        }
    }
}
