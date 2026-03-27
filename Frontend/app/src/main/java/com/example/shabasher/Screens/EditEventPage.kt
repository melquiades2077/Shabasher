package com.example.shabasher.Screens

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
                CircularProgressIndicator()
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