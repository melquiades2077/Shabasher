package com.example.shabasher.Views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shabasher.Model.Routes

@Composable
fun MainPage(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(text = "Вы успешно вошли в Шабашер!")

            Button(
                onClick = {
                    navController.navigate(Routes.WELCOME) { popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Text(text = "Выйти")
            }

            Button(
                onClick = { }
            ) {
                Text(text = "Сменить тему")
            }


        }
    }
}
