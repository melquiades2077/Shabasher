package com.example.shabasher.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.Routes
import com.example.shabasher.components.InputField
import com.example.shabasher.ViewModels.NameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamePage(
    navController: NavController,
    onNameSuccess: () -> Unit,
    viewModel: NameViewModel = viewModel()) {
    Scaffold(
        modifier = Modifier.Companion.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
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
                    if (viewModel.validate()) {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "FAB")
            }

        }

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize().padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Создайте профиль",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            "Познакомимся ближе?",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

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

                    InputField(
                        label = "Имя",
                        value = viewModel.name.value,
                        onValueChange = { viewModel.name.value = it },
                        keyboardType = KeyboardType.Email
                    )

                    viewModel.error.value?.let { error ->
                        Text(error)
                    }
                }
            }
            // пробрасываем callback наверх
            LaunchedEffect(viewModel.name.value) {
                // ничего — просто пример, что можно подписывать события
            }
        }

    }
}