package com.example.shabasher.Pages

import android.widget.ImageButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.FloatingActionButton

@Composable
fun InputField(
    header: String
) {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        value = text,                    // обязательный параметр
        onValueChange = { text = it },   // обязательный параметр
        label = { Text(header) }
    )
}

@Composable
fun InputFields() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        InputField("Email")
        InputField("Пароль")
        InputField("Повторите пароль")
    }
}


@Composable
fun RegisterPage() {
    Scaffold(modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* действие */ }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Добавить"
                )
            }
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            IconButton(
                onClick = { /* Действие при клике */ },
                modifier = Modifier
                    .align(Alignment.TopStart)) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад"
                )
            }
            Text(text = "Регистрация",
                modifier = Modifier.align(Alignment.TopCenter).padding(16.dp))
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                InputFields()
            }

        }
    }
}

@Preview
@Composable
fun RegisterPagePreview() {
    RegisterPage()
}

@Preview
@Composable
fun InputFieldsPreview() {
    InputFields()
}