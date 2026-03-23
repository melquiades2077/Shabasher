package com.example.shabasher.Screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.shabasher.Model.ParticipantOfDonation
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.ViewModels.DonationViewModel
import com.example.shabasher.ui.theme.ShabasherTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    navController: NavController,
    viewModel: DonationViewModel,
    donationId: String,
    onPaymentConfirmed: () -> Unit,
    onNavigateToProfile: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCopiedToast by remember { mutableStateOf(false) }

    // Фильтр участников
    var participantFilter by remember { mutableStateOf(ParticipantFilter.ALL) }

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(donationId) {
        viewModel.loadDonationById(donationId)
    }

    val donation = state.donation
    if (donation == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Вычисляем прогресс
    val progress = if (donation.targetAmount > 0) {
        donation.collectedAmount.toFloat() / donation.targetAmount.toFloat()
    } else 0f

    // Фильтруем участников
    val filteredParticipants = donation.participants.filter { participant ->
        when (participantFilter) {
            ParticipantFilter.ALL -> true
            ParticipantFilter.PAID -> participant.paidAmount > 0
            ParticipantFilter.NOT_PAID -> participant.paidAmount == 0
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(donation.title) },
                navigationIcon = {
                    IconButton(onClick = { SafeNavigation.navigate { navController.popBackStack() } }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(48.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            // 📊 Крупный индикатор прогресса
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CustomCircularPercentIndicator(
                        progress = progress.coerceIn(0f, 1f),
                        size = 160.dp,
                        strokeWidth = 8.dp,
                        progressColor = colorScheme.primary,
                        trackColor = colorScheme.surfaceVariant,
                        textColor = colorScheme.onSurface,
                        animationDuration = 600
                    )

                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 💰 Сумма: крупно + подпись
                    Text(
                        text = "${donation.collectedAmount} ₽",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "из ${donation.targetAmount} ₽ собрано",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colorScheme.error
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }



            // 📝 Описание сбора
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = donation.description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = colorScheme.error
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            }

            // 💳 Кнопка перевода
            item {
                Button(
                    onClick = {
                        // Копируем реквизиты и показываем тост
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("PaymentDetails", donation.paymentDetails)
                        clipboard.setPrimaryClip(clip)
                        showCopiedToast = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "Перевести организатору",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 👥 Секция участников
            item {
                ParticipantsSection(
                    participants = filteredParticipants,
                    totalParticipants = donation.participants.size,
                    paidCount = donation.participants.count { it.paidAmount > 0 },
                    currentFilter = participantFilter,
                    onFilterChange = { participantFilter = it },
                    onUserClick = onNavigateToProfile,
                    colorScheme = colorScheme
                )
            }
        }
    }

    // 🍞 Тост о копировании
    if (showCopiedToast) {
        LaunchedEffect(Unit) {
            delay(2000)
            showCopiedToast = false
        }
        ToastOverlay(
            text = "Реквизиты скопированы!",
            colorScheme = colorScheme
        )
    }
}


// Enum для фильтра
enum class ParticipantFilter {
    ALL, PAID, NOT_PAID
}

@Composable
fun ParticipantsSection(
    participants: List<ParticipantOfDonation>,
    totalParticipants: Int,
    paidCount: Int,
    currentFilter: ParticipantFilter,
    onFilterChange: (ParticipantFilter) -> Unit,
    onUserClick: (String) -> Unit,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Заголовок: Участники + статистика
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Участники",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
            )

            Text(
                text = "сдали $paidCount из $totalParticipants",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colorScheme.error
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Фильтры: Все / Не сдали / Сдали
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                label = "Все",
                selected = currentFilter == ParticipantFilter.ALL,
                onClick = { onFilterChange(ParticipantFilter.ALL) },
                count = totalParticipants
            )
            FilterChip(
                label = "Не сдали",
                selected = currentFilter == ParticipantFilter.NOT_PAID,
                onClick = { onFilterChange(ParticipantFilter.NOT_PAID) },
                count = totalParticipants - paidCount,
                inactiveColor = colorScheme.error
            )
            FilterChip(
                label = "Сдали",
                selected = currentFilter == ParticipantFilter.PAID,
                onClick = { onFilterChange(ParticipantFilter.PAID) },
                count = paidCount,
                activeColor = colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список участников
        if (participants.isEmpty()) {
            Text(
                text = "Нет участников для отображения",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(participants, key = { it.id }) { participant ->
                    ParticipantRow(
                        participant = participant,
                        onClick = { onUserClick(participant.userId) },
                        colorScheme = colorScheme
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    count: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outline
) {
    val backgroundColor = if (selected) {
        activeColor.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (selected) {
        activeColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = "($count)",
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ParticipantRow(
    participant: ParticipantOfDonation,
    onClick: () -> Unit,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар + имя
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Аватар
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFBB86FC),
                                    Color(0xFF03DAC6)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = participant.avatar.take(2).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Имя
                Text(
                    text = participant.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Сумма
            Text(
                text = if (participant.paidAmount > 0) {
                    "${participant.paidAmount} ₽"
                } else {
                    "—"
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (participant.paidAmount > 0) {
                        colorScheme.primary
                    } else {
                        colorScheme.error
                    },
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun ToastOverlay(text: String, colorScheme: ColorScheme) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.scrim, // ← используем scrim из вашей схемы!
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            Text(
                text = text,
                color = Color.White, // или colorScheme.inverseOnSurface, если нужно
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(colorScheme.primary)
        )
        Text(
            text = "${(progress * 100).roundToInt()}%",
            color = colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@PreviewLightDark
@Composable
fun DonationScreenPreview() {
    ShabasherTheme {
        DonationScreen(
            navController = rememberNavController(),
            viewModel = viewModel(),
            donationId = "2",
            onPaymentConfirmed = { },
            modifier = Modifier
        )
    }
}