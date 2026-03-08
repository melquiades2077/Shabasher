package com.example.shabasher.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.shabasher.Model.Donation
import com.example.shabasher.Model.DonationPaymentStatus
import com.example.shabasher.Model.DonationStatus
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.ViewModels.DonationListViewModel
import com.example.shabasher.ui.theme.ShabasherTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationListScreen(
    navController: NavController,
    viewModel: DonationListViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Сборы") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SafeNavigation.navigate {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.donations.isEmpty()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                        .fillMaxSize()
                        .weight(4f)
                ) {
                    Image(
                        painter = painterResource(id = com.example.shabasher.R.drawable.manulnotlogin),
                        contentDescription = null,
                        modifier = Modifier
                            .size(280.dp)
                    )
                    Text(
                        text = "В этом событии пока нет сборов",
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.donations) { donation ->
                        DonationCard(
                            donation = donation,
                            onClick = {
                                navController.navigate("donation/${donation.id}")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Приложение не принимает платежи, оно лишь фиксирует факт оплаты",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colorScheme.outline
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DonationCard(
    donation: Donation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = donation.title,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = when (donation.status) {
                        DonationStatus.ACTIVE -> "Активен"
                        DonationStatus.COMPLETED -> "Завершён"
                        DonationStatus.CLOSED -> "Закрыт"
                    },
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .background(
                            color = when (donation.status) {
                                DonationStatus.ACTIVE -> colorScheme.primary
                                else -> colorScheme.secondary
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Text(
                text = "Собрано ${donation.collectedAmount} ₽ из ${donation.targetAmount} ₽",
                color = colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            LinearProgressIndicator(
                progress = {
                    (donation.collectedAmount.toFloat() / donation.targetAmount.toFloat())
                        .coerceIn(0f, 1f)
                           },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colorScheme.primary,
                trackColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (donation.paymentStatus) {
                        DonationPaymentStatus.PAID -> "Оплачено"
                        DonationPaymentStatus.NOT_PAID -> "Не оплачено"
                    },
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                            .background(
                            color = colorScheme.secondary,
                    shape = RoundedCornerShape(16.dp)
                )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Text(
                    text = "Оплатили: ${donation.paidParticipants} из ${donation.totalParticipants}",
                    color = colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun DonationListScreenPreview() {
    ShabasherTheme {
        DonationListScreen(
            navController = rememberNavController(),
            viewModel = viewModel(),
            modifier = Modifier
        )
    }
}