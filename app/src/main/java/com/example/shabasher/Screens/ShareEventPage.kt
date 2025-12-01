package com.example.shabasher.Screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.R
import com.example.shabasher.ViewModels.ShareEventViewModel
import com.example.shabasher.components.ShabasherSecondaryButton
import com.example.shabasher.ui.theme.Typography

@Composable
fun ShareEventPage(
    navController: NavController,
    viewModel: ShareEventViewModel = viewModel()
) {
    Scaffold(
        modifier = Modifier.Companion.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(40.dp),
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(vertical = 32.dp, horizontal = 16.dp)
                        .fillMaxWidth()

                ) {
                    Text(
                        text = "Поделитесь событием",
                        style = Typography.headlineMedium
                    )
                    Image(
                        painter = painterResource(R.drawable.qr_code_temp),
                        contentDescription = "QR код",
                        modifier = Modifier
                            .size(200.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = viewModel.link.value,
                            style = Typography.bodyLarge
                        )
                        IconButton(
                            onClick = { },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.content_copy),
                                contentDescription = "Копировать",
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                    }
                }
                ShabasherSecondaryButton(
                    text = "Продолжить",
                    onClick = {
                        SafeNavigation.navigate {
                            navController.navigate(Routes.EVENT) {
                                popUpTo(Routes.MAIN) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                              },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
