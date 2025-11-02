package com.example.shabasher.Pages

import android.R
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shabasher.Greeting

@Composable
fun WelcomeButton(
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {},
        modifier = modifier
            .width(300.dp)
            .height(50.dp)
    ) {
        Text(text)
    }
}

@Composable
fun WelcomeButtons(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WelcomeButton("Регистрация")
        WelcomeButton("Вход")
    }
}

@Composable
fun Welcome(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Добро пожаловать в\nШабашер", textAlign = TextAlign.Center)
        Image(
            painter = painterResource(id = com.example.shabasher.R.drawable.cat),
            contentDescription = null,
            modifier = Modifier.size(200.dp))
        Text("Управляйте, отдыхайте, играйте\nКоснитесь кнопки, чтобы начать!", textAlign = TextAlign.Center)
    }
}

@Composable
fun WelcomePage() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Центр экрана
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Welcome()
            }

            // Центр снизу
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                WelcomeButtons()
            }
        }
    }
}

@Preview
@Composable
fun WelcomePagePreview() {
    WelcomePage()
}

@Preview
@Composable
fun WelcomePreview() {
    Welcome()
}

@Preview
@Composable
fun WelcomeButtonsPreview() {
    WelcomeButtons()
}