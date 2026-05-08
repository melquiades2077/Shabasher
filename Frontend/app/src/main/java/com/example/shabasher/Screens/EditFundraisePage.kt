package com.example.shabasher.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.ViewModels.EditFundraiseViewModel
import com.example.shabasher.components.InputField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFundraisePage(
    navController: NavController,
    fundraiseId: String,
    viewModel: EditFundraiseViewModel
) {
    val ui = viewModel.uiState.value

    LaunchedEffect(fundraiseId) { viewModel.loadFundraise(fundraiseId) }

    LaunchedEffect(Unit) {
        viewModel.saved.collect {
            SafeNavigation.navigate { navController.popBackStack() }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Редактировать сбор") },
                navigationIcon = {
                    IconButton(onClick = {
                        SafeNavigation.navigate { navController.popBackStack() }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        enabled = ui.isDirty && !ui.isLoading,
                        onClick = { if (ui.isDirty) viewModel.save() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Сохранить",
                            tint = if (ui.isDirty) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
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
        if (ui.isLoading && ui.fundraiseId == null) {
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
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item {
                InputField(
                    label = "Название сбора",
                    value = ui.title,
                    onValueChange = viewModel::updateTitle,
                    imeAction = ImeAction.Next
                )
            }
            item {
                InputField(
                    label = "Описание",
                    value = ui.description,
                    onValueChange = viewModel::updateDescription,
                    singleLine = false,
                    modifier = Modifier.height(120.dp),
                    imeAction = ImeAction.Next
                )
            }
            item {
                InputField(
                    label = "Целевая сумма (необязательно)",
                    value = ui.targetAmount,
                    onValueChange = viewModel::updateTargetAmount,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            }
            item {
                InputField(
                    label = "Телефон для оплаты",
                    value = ui.paymentPhone,
                    onValueChange = viewModel::updatePaymentPhone,
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            }
            item {
                InputField(
                    label = "Получатель",
                    value = ui.paymentRecipient,
                    onValueChange = viewModel::updatePaymentRecipient,
                    imeAction = ImeAction.Done
                )
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
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
