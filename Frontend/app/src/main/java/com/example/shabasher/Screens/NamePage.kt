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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.components.InputField
import com.example.shabasher.ViewModels.NameViewModel
import com.example.shabasher.ViewModels.NameViewModelFactory
import com.example.shabasher.data.local.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamePage(
    navController: NavController,
    email: String,
    password: String
) {
    val context = LocalContext.current
    val viewModel: NameViewModel = viewModel(
        factory = NameViewModelFactory(
            context = context,
            email = email,
            password = password
        )
    )
    val focusRequester = remember { FocusRequester() }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }


    LaunchedEffect(viewModel.success.value) {
        if (viewModel.success.value) {
            // Проверяем, что токен действительно сохранен
            val tokenManager = TokenManager(context)
            val token = tokenManager.getToken()

            if (token != null) {
                println("DEBUG: Token found, navigating to MAIN")
                navController.navigate(Routes.MAIN) {
                    popUpTo(0) { inclusive = true }
                }
                viewModel.success.value = false  // Сбрасываем флаг
            } else {
                println("DEBUG: ERROR - No token found!")
                viewModel.error.value = "Ошибка сохранения токена"
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(

                        onClick = {
                            focusManager.clearFocus()
                            SafeNavigation.navigate { navController.popBackStack()
                            } }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                    viewModel.submit()
                          },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Continue")
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // Заголовки
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

                // Фото
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = "Add photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Поле ввода имени
                InputField(
                    label = "Имя",
                    value = viewModel.name.value,
                    onValueChange = { viewModel.name.value = it },
                    modifier = Modifier.focusRequester(focusRequester)
                )

                // Ошибка
                viewModel.error.value?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }

                // Индикатор загрузки (можно заменить на CircularProgressIndicator)
                if (viewModel.loading.value) {
                    Text("Сохраняем…", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
