package com.example.shabasher.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.components.InputField
import com.example.shabasher.ViewModels.RegisterViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPage(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()) {

    Scaffold(
        modifier = Modifier.Companion.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SafeNavigation.navigate { navController.popBackStack() }
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
                    SafeNavigation.navigate {
                        viewModel.register()
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
                            "Начнём!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            "Введите адрес эл. почты и пароль",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    InputField(
                        label = "Email",
                        value = viewModel.email.value,
                        onValueChange = { viewModel.email.value = it },
                        keyboardType = KeyboardType.Email
                    )

                    InputField(
                        label = "Пароль",
                        value = viewModel.password.value,
                        onValueChange = { viewModel.password.value = it },
                        isPassword = true,
                        keyboardType = KeyboardType.Password
                    )

                    InputField(
                        label = "Повторите пароль",
                        value = viewModel.repeatPassword.value,
                        onValueChange = { viewModel.repeatPassword.value = it },
                        isPassword = true,
                        keyboardType = KeyboardType.Password
                    )

                    viewModel.error.value?.let { error ->
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            LaunchedEffect(viewModel.success.value) {
                if (viewModel.success.value) {
                    navController.navigate("namePage?email=${viewModel.email.value}&password=${viewModel.password.value}")
                    viewModel.success.value = false
                }
            }


        }

    }
}


