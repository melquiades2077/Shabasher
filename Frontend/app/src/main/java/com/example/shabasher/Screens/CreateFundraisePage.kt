package com.example.shabasher.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shabasher.Model.Routes
import com.example.shabasher.ViewModels.CreateFundraiseState
import com.example.shabasher.ViewModels.CreateFundraiseViewModel
import com.example.shabasher.ViewModels.FundraiseAction
import com.example.shabasher.ViewModels.FundraiseEvent
import com.example.shabasher.ViewModels.FundraiseUiState
import com.example.shabasher.ViewModels.FundraisesViewModel
import com.example.shabasher.components.InputField
import io.ktor.websocket.Frame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFundraisePage(
    navController: NavController,
    viewModel: CreateFundraiseViewModel
) {
    // 👇 Используем state из нового ViewModel
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val titleFocus = remember { FocusRequester() }
    val descFocus = remember { FocusRequester() }
    val phoneFocus = remember { FocusRequester() }
    val recipientFocus = remember { FocusRequester() }
    val amountFocus = remember { FocusRequester() }

    // 🔥 EVENTS
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FundraiseEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        event.onAction?.invoke()
                    }
                }
                is FundraiseEvent.NavigateToDetails -> {
                    // ✅ ИСПРАВЛЕНО: используем правильный роут "donation/{id}"
                    navController.navigate("donation/${event.id}") {
                        popUpTo(Routes.DONATION_LIST) { inclusive = false } // 🔥 Возврат к списку, а не к самому роуту
                        launchSingleTop = true // 🔥 Не создавать дубликаты
                    }
                }
            }
        }
    }

    // 🔥 локальный state формы
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Frame.Text("Создать сбор") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // 👇 Вызываем метод create() напрямую, а не onAction
                    val parsedAmount = amount.toBigDecimalOrNull()
                    viewModel.create(
                        title = title,
                        description = description,
                        target = parsedAmount,
                        phone = phone,
                        recipient = recipient
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (uiState is CreateFundraiseState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            }
        }
    ) { padding ->

        // 👇 Используем CreateFundraiseState
        if (uiState is CreateFundraiseState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                InputField(
                    label = "Название сбора",
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.focusRequester(titleFocus),
                    imeAction = ImeAction.Next,
                    onImeAction = { descFocus.requestFocus() }
                )
            }
            item {
                InputField(
                    label = "Описание",
                    value = description,
                    onValueChange = { description = it },
                    singleLine = false,
                    modifier = Modifier
                        .height(120.dp)
                        .focusRequester(descFocus),
                    imeAction = ImeAction.Next,
                    onImeAction = { amountFocus.requestFocus() }
                )
            }
            item {
                InputField(
                    label = "Сумма (необязательно)",
                    value = amount,
                    onValueChange = { amount = it },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.focusRequester(amountFocus),
                    imeAction = ImeAction.Next,
                    onImeAction = { phoneFocus.requestFocus() }
                )
            }
            item {
                InputField(
                    label = "Телефон для оплаты",
                    value = phone,
                    onValueChange = { phone = it },
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.focusRequester(phoneFocus),
                    imeAction = ImeAction.Next,
                    onImeAction = { recipientFocus.requestFocus() }
                )
            }
            item {
                InputField(
                    label = "Получатель",
                    value = recipient,
                    onValueChange = { recipient = it },
                    modifier = Modifier.focusRequester(recipientFocus),
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                )
            }

            // 👇 Ошибка из нового стейта
            if (uiState is CreateFundraiseState.Error) {
                item {
                    Text(
                        text = (uiState as CreateFundraiseState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}