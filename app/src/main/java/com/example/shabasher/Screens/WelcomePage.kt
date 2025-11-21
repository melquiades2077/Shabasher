package com.example.shabasher.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shabasher.components.ShabasherPrimaryButton
import com.example.shabasher.components.ShabasherSecondaryButton
import com.example.shabasher.Model.Routes

@Composable
fun Welcome(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Добро пожаловать в",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                "Шабашер",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Image(
            painter = painterResource(id = com.example.shabasher.R.drawable.cat),
            contentDescription = null,
            modifier = Modifier.size(200.dp))
        Text("Управляйте, отдыхайте, играйте\nКоснитесь кнопки, чтобы начать!",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WelcomePage(navController: NavController) {
    Scaffold(
        modifier = Modifier.Companion.fillMaxSize()

    ) { innerPadding ->


        Box(
            modifier = Modifier
                .fillMaxSize().padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 112.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Welcome()

            }
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter).padding(bottom = 16.dp)
            ) {
                ShabasherSecondaryButton(
                    "Регистрация",
                    onClick = { navController.navigate(Routes.REGISTER) })
                ShabasherPrimaryButton("Вход", onClick = { navController.navigate(Routes.LOGIN) })
            }


        }
    }
}


@PreviewLightDark
@Composable
fun WelcomePagePreview() {
    //WelcomePage()
}