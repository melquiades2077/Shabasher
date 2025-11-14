package com.example.shabasher.Views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.ViewModels.LoginViewModel
import com.example.shabasher.ViewModels.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(
    navController: NavController,
              onLoginSuccess: () -> Unit,
              viewModel: LoginViewModel = viewModel()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
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

                    viewModel.error.value?.let { error ->
                        Text(error)
                    }
                }
            }
            // пробрасываем callback наверх
            LaunchedEffect(viewModel.email.value, viewModel.password.value) {
                // ничего — просто пример, что можно подписывать события
            }
        }

}

@Preview
@Composable
fun LoginPagePreview() {
    //LoginPage()
}