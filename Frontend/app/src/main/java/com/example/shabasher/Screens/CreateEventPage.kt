package com.example.shabasher.Screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.ViewModels.CreateEventViewModel
import com.example.shabasher.ViewModels.NameViewModel
import com.example.shabasher.components.InputField
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventPage(
    navController: NavController,
    viewModel: CreateEventViewModel = viewModel()
) {
    val userInterface = viewModel.uiState.value

    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }

    // Когда событие создано → переход
    LaunchedEffect(userInterface.successEventId) {
        userInterface.successEventId?.let {
            navController.navigate(Routes.EVENT) {
                popUpTo(Routes.MAIN) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Создать событие") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createEvent()
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Создать")
            }
        }
    ) { innerPadding ->

        if (userInterface.isLoading) {
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

            // Фото (пока заглушка)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Иконка загрузки
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = "Add photo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )

                    }

                    //Исправил тут name на title, с name код не компилился
                    InputField(
                        label = "Название",
                        value = viewModel.title.value,
                        onValueChange = { viewModel.title.value = it }
                    )

                    InputField(
                        label = "Описание",
                        value = viewModel.title.value,
                        onValueChange = { viewModel.title.value = it }
                    )

                    InputField(
                        label = "Адрес",
                        value = viewModel.title.value,
                        onValueChange = { viewModel.title.value = it }
                    )

                    InputField(
                        label = "Дата",
                        value = viewModel.title.value,
                        onValueChange = { viewModel.title.value = it }
                    )

                    InputField(
                        label = "Время",
                        value = viewModel.title.value,
                        onValueChange = { viewModel.title.value = it }
                    )



                    viewModel.error.value?.let { error ->
                        Text(error)
                    }
                }
            }
            //Тут тоже name na title
            // пробрасываем callback наверх
            LaunchedEffect(viewModel.title.value) {
                // ничего — просто пример, что можно подписывать события

                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Add photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            // Название
            item {
                InputField(
                    label = "Название события",
                    value = userInterface.title,
                    onValueChange = { viewModel.updateTitle(it) }
                )
            }

            // Описание
            item {
                InputField(
                    label = "Описание",
                    value = userInterface.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    singleLine = false,
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.height(150.dp)
                )
            }

            // Адрес
            item {
                InputField(
                    label = "Адрес",
                    value = userInterface.address,
                    onValueChange = { viewModel.updateAddress(it) },
                    keyboardType = KeyboardType.Text
                )
            }

            // Дата
            item {
                InputField(
                    label = "Дата",
                    value = userInterface.date,
                    onValueChange = { },
                    readOnly = true,
                    trailing = {
                        IconButton(onClick = { showDatePicker.value = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Дата")
                        }
                    }
                )
            }

            // Время
            item {
                InputField(
                    label = "Время",
                    value = userInterface.time,
                    onValueChange = { },
                    readOnly = true,
                    trailing = {
                        IconButton(onClick = { showTimePicker.value = true }) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Время")
                        }
                    }
                )
            }

            // Ошибка
            item {
                userInterface.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))

            }
        }


        
        // Date Picker
        if (showDatePicker.value) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker.value = false },
                onDateSelected = { viewModel.setDate(it) }
            )
        }

        // Time Picker
        if (showTimePicker.value) {
            TimePickerDialog(
                onDismissRequest = { showTimePicker.value = false },
                onTimeSelected = { h, m -> viewModel.setTime(h, m) }
            )
        }
    }
}




@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val date = Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(date.toString())
                }
                onDismissRequest()
            }) {
                Text("OK")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val timeState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(timeState.hour, timeState.minute)
                onDismissRequest()
            }) {
                Text("ОК")
            }
        },
        text = {
            TimePicker(state = timeState)
        }
    )
}




