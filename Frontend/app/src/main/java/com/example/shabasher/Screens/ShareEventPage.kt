package com.example.shabasher.Screens


import QRCode
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.R
import com.example.shabasher.ViewModels.ShareEventViewModel
import com.example.shabasher.ViewModels.ShareEventViewModelFactory
import com.example.shabasher.components.ShabasherSecondaryButton
import com.example.shabasher.data.network.InviteRepository
import com.example.shabasher.ui.theme.Typography

@Composable
fun ShareEventPage(
    navController: NavController,
    eventId: String,
    viewModel: ShareEventViewModel = viewModel(factory = ShareEventViewModelFactory(
        InviteRepository(LocalContext.current)
    ))
) {
    // Инициализация ViewModel с контекстом
    val context = LocalContext.current

    // Инициализируем данные внутри LaunchedEffect
    LaunchedEffect(eventId) {
        viewModel.init(eventId, context) // Передаем контекст прямо в init
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
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
                // Карточка с информацией
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

                    // QR-код с ссылкой
                    QRCode(viewModel.link.value.trim('"'))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Отображение ссылки
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = viewModel.link.value.trim('"'),
                                    style = Typography.bodyLarge
                                )
                            }

                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("invite", viewModel.link.value.trim('"')))
                                Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.content_copy),
                                    contentDescription = "copy"
                                )
                            }
                        }
                    }
                }

                // Кнопка "Продолжить"
                ShabasherSecondaryButton(
                    text = "Продолжить",
                    onClick = {
                        SafeNavigation.navigate {
                            navController.navigate("${Routes.EVENT}/$eventId") {
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



