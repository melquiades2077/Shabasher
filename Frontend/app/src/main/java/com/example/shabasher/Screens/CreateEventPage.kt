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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventPage(
    navController: NavController,
    viewModel: CreateEventViewModel = viewModel()


) {
    val ui = viewModel.uiState.value

    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val titleFocus = remember { FocusRequester() }
    val descFocus = remember { FocusRequester() }
    val addressFocus = remember { FocusRequester() }

    val titleBring = remember { BringIntoViewRequester() }
    val descBring = remember { BringIntoViewRequester() }
    val addressBring = remember { BringIntoViewRequester() }

    LaunchedEffect(ui.successEventId) {
        ui.successEventId?.let {
            navController.navigate("${Routes.EVENT}/$it") {
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
                    SafeNavigation.navigate { viewModel.createEvent() }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Создать")
            }
        }
    ) { innerPadding ->

        if (ui.isLoading) {
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
                .fillMaxSize()
                .imePadding(),
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
                    value = ui.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    modifier = Modifier
                        .focusRequester(titleFocus)
                        .bringIntoViewRequester(titleBring)
                        .onFocusEvent {
                            if (it.isFocused) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    titleBring.bringIntoView()
                                }
                            }
                        },
                    imeAction = ImeAction.Next,
                    onImeAction = {
                        descFocus.requestFocus()
                    }
                )
            }

            // Описание
            item {
                InputField(
                    label = "Описание",
                    value = ui.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    singleLine = false,
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier
                        .height(150.dp)
                        .focusRequester(descFocus)
                        .bringIntoViewRequester(descBring)
                        .onFocusEvent {
                            if (it.isFocused) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    descBring.bringIntoView()
                                }
                            }
                        },
                    imeAction = ImeAction.Next,
                    onImeAction = {
                        addressFocus.requestFocus()
                    }
                )
            }

            // Адрес
            item {
                InputField(
                    label = "Адрес",
                    value = ui.address,
                    onValueChange = { viewModel.updateAddress(it) },
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier
                        .focusRequester(addressFocus)
                        .bringIntoViewRequester(addressBring)
                        .onFocusEvent {
                            if (it.isFocused) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    addressBring.bringIntoView()
                                }
                            }
                        },
                    imeAction = ImeAction.Next,
                    onImeAction = {
                        keyboardController?.hide()
                        showDatePicker.value = true
                    }
                )
            }

            // Дата
            item {
                InputField(
                    label = "Дата",
                    value = ui.date,
                    onValueChange = { },
                    readOnly = true,
                    imeAction = ImeAction.Next,
                    onImeAction = {
                        keyboardController?.hide()
                        showDatePicker.value = true
                    },
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
                    value = ui.time,
                    onValueChange = { },
                    readOnly = true,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        keyboardController?.hide()
                        showTimePicker.value = true
                    },
                    trailing = {
                        IconButton(onClick = { showTimePicker.value = true }) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Время")
                        }
                    }
                )
            }
            //Время
            item {
                ui.error?.let {
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

        if (showDatePicker.value) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker.value = false },
                onDateSelected = {
                    viewModel.setDate(it)
                },
                onAfterSelect = {
                    showTimePicker.value = true // 👈 сразу открываем время
                }
            )
        }

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
    onDateSelected: (String) -> Unit,
    onAfterSelect: (() -> Unit)? = null
) {
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - 86400000
            }
        }
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismissRequest,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            headlineContentColor = MaterialTheme.colorScheme.onSurface,
            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

            dayContentColor = MaterialTheme.colorScheme.onSurface,
            disabledDayContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),

            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
            selectedDayContainerColor = MaterialTheme.colorScheme.primary,

            todayContentColor = MaterialTheme.colorScheme.primary,
            todayDateBorderColor = MaterialTheme.colorScheme.primary,

            yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
            selectedYearContainerColor = MaterialTheme.colorScheme.primary
        ),
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis

                    if (millis != null) {
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        val formatted = "%04d-%02d-%02d".format(
                            localDate.year,
                            localDate.monthValue,
                            localDate.dayOfMonth
                        )

                        onDateSelected(formatted)
                        onDismissRequest()
                        onAfterSelect?.invoke()
                    } else {
                        onDismissRequest()
                    }
                }
            ) {
                Text(
                    "ОК",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    "Отмена",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,

                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                dayContentColor = MaterialTheme.colorScheme.onSurface,
                disabledDayContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),

                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,

                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary,

                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary
            )
        )
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
        containerColor = MaterialTheme.colorScheme.surface, // фон диалога
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timeState.hour, timeState.minute)
                    onDismissRequest()
                }
            ) {
                Text(
                    "ОК",
                    color = MaterialTheme.colorScheme.primary // акцент
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    "Отмена",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            // 👇 ВАЖНО: принудительно задаём цвета
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface
            ) {
                TimePicker(
                    state = timeState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        periodSelectorBorderColor = MaterialTheme.colorScheme.primary,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    )
}




