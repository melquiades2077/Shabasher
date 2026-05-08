package com.example.shabasher.Screens

import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shabasher.Model.EventData
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.ViewModels.EditEventViewModel
import com.example.shabasher.ViewModels.EditEventViewModelFactory
import com.example.shabasher.components.InputField
import io.ktor.websocket.Frame

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventPage(
    navController: NavController,
    eventId: String,
    context: Context = LocalContext.current,
    viewModel: EditEventViewModel = viewModel(factory = EditEventViewModelFactory(context))
) {
    val ui = viewModel.uiState.value

    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.uploadAvatar(context, uri)
    }

    LaunchedEffect(eventId) {
        viewModel.loadEventById(eventId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Редактировать") },
                navigationIcon = {
                    IconButton(onClick = {
                        SafeNavigation.navigate { navController.popBackStack() }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        enabled = ui.isDirty && !ui.isLoading,
                        onClick = {
                            if (ui.isDirty) {
                                SafeNavigation.navigate { viewModel.saveEvent() }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Сохранить",
                            tint = if (ui.isDirty) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
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
        if (ui.isLoading && ui.eventId == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                EventAvatarPicker(
                    avatarUrl = ui.avatarUrl,
                    isBusy = ui.isAvatarBusy,
                    onPick = {
                        pickAvatarLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onDelete = { viewModel.deleteAvatar() }
                )
            }
            item {
                InputField(
                    label = "Название события",
                    value = ui.title,
                    onValueChange = { viewModel.updateTitle(it) }
                )
            }
            item {
                InputField(
                    label = "Описание",
                    value = ui.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    singleLine = false,
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.height(150.dp)
                )
            }
            item {
                InputField(
                    label = "Адрес",
                    value = ui.address,
                    onValueChange = { viewModel.updateAddress(it) },
                    keyboardType = KeyboardType.Text
                )
            }
            item {
                val showDatePicker = remember { mutableStateOf(false) }
                InputField(
                    label = "Дата",
                    value = ui.date,
                    onValueChange = { },
                    readOnly = true,
                    trailing = {
                        IconButton(onClick = { showDatePicker.value = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Дата")
                        }
                    }
                )
                if (showDatePicker.value) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker.value = false },
                        onDateSelected = { viewModel.setDate(it) }
                    )
                }
            }
            item {
                val showTimePicker = remember { mutableStateOf(false) }
                InputField(
                    label = "Время",
                    value = ui.time,
                    onValueChange = { },
                    readOnly = true,
                    trailing = {
                        IconButton(onClick = { showTimePicker.value = true }) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Время")
                        }
                    }
                )
                if (showTimePicker.value) {
                    TimePickerDialog(
                        onDismissRequest = { showTimePicker.value = false },
                        onTimeSelected = { h, m -> viewModel.setTime(h, m) }
                    )
                }
            }
            item {
                ui.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
private fun EventAvatarPicker(
    avatarUrl: String?,
    isBusy: Boolean,
    onPick: () -> Unit,
    onDelete: () -> Unit
) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = !isBusy) { onPick() },
            contentAlignment = Alignment.Center
        ) {
            when {
                isBusy -> CircularProgressIndicator()
                avatarUrl == null -> Icon(
                    Icons.Default.Image,
                    contentDescription = "Добавить фото",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(80.dp)
                )
                else -> AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Обложка события",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onPick, enabled = !isBusy) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(if (avatarUrl == null) "Загрузить фото" else "Изменить")
            }
            if (avatarUrl != null) {
                TextButton(onClick = onDelete, enabled = !isBusy) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
