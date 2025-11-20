package com.example.shabasher.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.components.InputField
import com.example.shabasher.ViewModels.NameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamePage(
    navController: NavController,
    onNameSuccess: () -> Unit,
    viewModel: NameViewModel = viewModel()) {
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

@Preview
@Composable
fun NamePagePreview() {
    //LoginPage()
}