package com.example.shabasher.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.shabasher.Model.EventData
import com.example.shabasher.Model.ParticipationStatus
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.ViewModels.EventViewModel
import com.example.shabasher.components.EventInfo
import com.example.shabasher.components.EventMoreInfo
import com.example.shabasher.components.GamesCard
import com.example.shabasher.components.ParticipationSelector
import com.example.shabasher.components.ParticipatorsCard
import com.example.shabasher.components.ServiceCard
import com.example.shabasher.components.ShabasherSecondaryButton
import com.example.shabasher.ui.theme.ShabasherTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventPage(
    navController: NavController,
    eventId: String,
    viewModel: EventViewModel = viewModel()
) {
    val ui = viewModel.uiState

    // загружаем событие один раз
    LaunchedEffect(Unit) {
        viewModel.loadEvent(eventId)
    }


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
                ),
                actions = {
                    Box {
                        IconButton(
                            onClick = { SafeNavigation.navigate { navController.navigate(Routes.SHAREEVENT) } }
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Поделиться"
                            )
                        }
                    }
                }
            )
        },
    ) { innerPadding ->

        when {
            ui.isLoading -> LoadingScreen()

            ui.event != null -> EventContent(
                event = ui.event,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            )
        }

    }
}

@Composable
fun LoadingScreen() {
    /*CircularProgressIndicator(
        modifier = Modifier.size(22.dp),
        color = MaterialTheme.colorScheme.onPrimary
    )*/
}

@Composable
fun EventContent(
    event: EventData,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            EventInfo(event.title, event.description)
        }

        item {
            EventMoreInfo(event.date, event.place, event.time)
        }

        item {
            ParticipatorsCard(event.participants)
        }

        item {
            ParticipationSelector(
                selected = ParticipationStatus.GOING,
                onSelect = { }
            )
        }

        item {
            ServiceCard()
        }

        item {
            GamesCard()
        }

        item {
            Spacer(Modifier.height(8.dp))
        }
    }
}




