package com.example.shabasher.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.EventShort
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.R
import com.example.shabasher.ViewModels.LoginViewModel
import com.example.shabasher.ViewModels.MainPageViewModel
import com.example.shabasher.ViewModels.ThemeViewModel
import com.example.shabasher.components.EventCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    navController: NavController,
    viewModel: MainPageViewModel = viewModel()
) {
    val ui = viewModel.uiState.value

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Шабашер") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SafeNavigation.navigate { navController.navigate(Routes.PROFILE) }
                        }
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "В профиль")
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
                    SafeNavigation.navigate { navController.navigate(Routes.CREATEEVENT) }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить событие")
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when {
                ui.isLoading -> {
                    MainLoadingScreen()
                }

                ui.error != null -> {
                    ErrorScreen(
                        message = ui.error!!,
                        onRetry = { viewModel.loadEvents() }
                    )
                }

                ui.events.isEmpty() -> {
                    EmptyEventsScreen()
                }

                else -> {
                    EventsList(
                        events = ui.events,
                        onClick = { eventId ->
                            SafeNavigation.navigate {
                                navController.navigate(/*"${Routes.EVENT}/$eventId"*/ Routes.EVENT)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyEventsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Пока нет событий",
            style = MaterialTheme.typography.headlineMedium,
        )

        Image(
            painter = painterResource(id = R.drawable.cat2),
            contentDescription = "No events",
            modifier = Modifier.size(200.dp)
        )

        Text(
            text = "Создайте новое или\nприсоединитесь по ссылке",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}

@Composable
fun MainLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EventsList(
    events: List<EventShort>,
    onClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(events) { event ->
            EventCard(
                title = event.title,
                description = event.date,
                status = event.status,
                onClick = { onClick(event.id) }
            )
        }
    }
}












