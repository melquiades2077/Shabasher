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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.ViewModels.DonationActionState
import com.example.shabasher.ViewModels.DonationUiState
import com.example.shabasher.ViewModels.DonationViewModel
import com.example.shabasher.data.dto.FundStatus
import com.example.shabasher.data.dto.FundraiseParticipant
import com.example.shabasher.data.dto.FundraiseParticipantStatus
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.ui.theme.ShabasherTheme
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════
// DonationScreen.kt
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    navController: NavController,
    viewModel: DonationViewModel = viewModel(),
    donationId: String,
    onNavigateBack: () -> Unit = { navController.popBackStack() },
    onParticipantClick: (String) -> Unit = {} // для перехода в профиль участника
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val currentUserId by remember {
        derivedStateOf { viewModel.getCurrentUserId() }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // 🔹 Загрузка данных при изменении donationId
    LaunchedEffect(donationId) {
        viewModel.loadDonationById(donationId)
    }

    // 🔹 Обработка action state (снэки)
    LaunchedEffect(actionState) {
        actionState.success?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearActionState()
        }
        actionState.error?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            viewModel.clearActionState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // ← важно!
        topBar = {
            DonationTopBar(
                onNavigateBack = onNavigateBack,
                title = when (uiState) {
                    is DonationUiState.Success -> (uiState as DonationUiState.Success).donation.title
                    else -> "Сбор средств"
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DonationUiState.Loading -> {
                LoadingState(modifier = Modifier.padding(paddingValues))
            }
            is DonationUiState.Success -> {
                DonationContent(
                    donation = state.donation,
                    currentUserId = currentUserId,
                    onMarkPaid = { viewModel.markPaid() },
                    onConfirmPayment = { participantUserId, amount ->
                        viewModel.confirmPayment(participantUserId, amount)
                    },
                    onRevertPayment = { participantUserId ->
                        viewModel.revertPayment(participantUserId)
                    },
                    onParticipantClick = onParticipantClick,
                    modifier = Modifier
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                )
            }
            is DonationUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = state.retry,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Top Bar
// ═══════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DonationTopBar(
    onNavigateBack: () -> Unit,
    title: String
) {
        CenterAlignedTopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(
                    onClick = { onNavigateBack }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

}

// ═══════════════════════════════════════════════════════
// Loading / Error States
// ═══════════════════════════════════════════════════════
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Загрузка...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Main Content
// ═══════════════════════════════════════════════════════
@Composable
private fun DonationContent(
    donation: Fundraise,
    currentUserId: String?,
    onMarkPaid: () -> Unit,
    onConfirmPayment: (String, BigDecimal?) -> Unit,
    onRevertPayment: (String) -> Unit,
    onParticipantClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 🔹 Прогресс сбора
       // DonationProgressSection(donation)


        Spacer(modifier = Modifier.height(64.dp))

        // 🔹 Реквизиты
        PaymentRequisitesSection(
            phone = donation.paymentPhone,
            recipient = donation.paymentRecipient,
            description = donation.description
        )



    }
}

// ═══════════════════════════════════════════════════════
// Progress Section with Circular Indicator
// ═══════════════════════════════════════════════════════
@Composable
private fun DonationProgressSection(donation: Fundraise) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Круговой индикатор с процентом в центре
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            CircularProgressIndicator(
                progress = { donation.progressPercent / 100f },
                modifier = Modifier.matchParentSize(),
                strokeWidth = 10.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${donation.progressPercent}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "собрано",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Сумма собрана / целевая
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${formatRubles(donation.currentAmount)} ₽",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "из ${donation.targetAmount?.let { formatRubles(it) } ?: "∞"} ₽",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Статус сбора
        if (!donation.isActive) {
            Spacer(modifier = Modifier.height(8.dp))
            StatusChip(
                status = when (donation.fundStatus) {
                    FundStatus.Closed -> "Закрыт"
                    FundStatus.Completed -> "Завершён"
                    FundStatus.Active -> "Активен"
                },
                color = when (donation.fundStatus) {
                    FundStatus.Completed -> MaterialTheme.colorScheme.tertiaryContainer
                    FundStatus.Closed -> MaterialTheme.colorScheme.surfaceVariant
                    FundStatus.Active -> MaterialTheme.colorScheme.primaryContainer
                }
            )
        }
    }
}

@Composable
private fun StatusChip(status: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ═══════════════════════════════════════════════════════
// Payment Requisites
// ═══════════════════════════════════════════════════════
@Composable
private fun PaymentRequisitesSection(
    phone: String,
    recipient: String,
    description: String?
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Реквизиты для оплаты",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Описание сбора
            description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider()

            // Номер телефона
            RequisiteRow(
                icon = Icons.Default.Phone,
                label = "Телефон",
                value = phone,
                onCopy = { /* скопировать в буфер */ }
            )

            // Получатель
            RequisiteRow(
                icon = Icons.Default.Person,
                label = "Получатель",
                value = recipient,
                onCopy = { /* скопировать в буфер */ }
            )

            // Кнопка копирования всех реквизитов
            TextButton(
                onClick = { /* копировать все реквизиты */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Скопировать реквизиты")
            }
        }
    }
}

@Composable
private fun RequisiteRow(
    icon: ImageVector,
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Копировать",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// User Payment Actions (Mark Paid / Pending / Confirmed)
// ═══════════════════════════════════════════════════════
@Composable
private fun UserPaymentActionsSection(
    donation: Fundraise,
    currentUserId: String,
    onMarkPaid: () -> Unit
) {
    val isParticipant = donation.participants?.any { it.userId == currentUserId } == true
    val myPaymentStatus = donation.myPaymentStatus

    // Если пользователь ещё не в списке участников — показываем кнопку "Я оплатил"
    if (donation.canMarkPaid()) {
        Button(
            onClick = onMarkPaid,
            modifier = Modifier.fillMaxWidth(),
            enabled = !donation.isPendingConfirmation(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Я оплатил(а)")
        }

        if (donation.isPendingConfirmation()) {
            Text(
                text = "✓ Оплата отправлена на проверку",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Если оплата уже подтверждена
    if (donation.isPaymentConfirmed()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ваша оплата подтверждена ✓",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Participants List with Admin Controls
// ═══════════════════════════════════════════════════════
@Composable
private fun ParticipantsSection(
    donation: Fundraise,
    currentUserId: String?,
    onConfirmPayment: (String, BigDecimal?) -> Unit,
    onRevertPayment: (String) -> Unit,
    onParticipantClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок секции
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Участники события",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            // Статистика
            donation.confirmedCount?.let { confirmed ->
                donation.participantsCount?.let { total ->
                    Text(
                        text = "$confirmed/$total ✓",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Список участников
        donation.participants?.let { participants ->
            if (participants.isEmpty()) {
                Text(
                    text = "Пока нет участников",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                participants.forEach { participant ->
                    ParticipantListItem(
                        participant = participant,
                        isCurrentUser = participant.userId == currentUserId,
                        isAdmin = donation.canConfirmPayments(),
                        onConfirmClick = { onConfirmPayment(participant.userId, participant.amount) },
                        onRevertClick = { onRevertPayment(participant.userId) },
                        onClick = { onParticipantClick(participant.userId) }
                    )
                }
            }
        } ?: run {
            // Заглушка, если участники ещё загружаются
            repeat(3) {
                ParticipantListItemSkeleton()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Participant List Item
// ═══════════════════════════════════════════════════════
@Composable
private fun ParticipantListItem(
    participant: FundraiseParticipant,
    isCurrentUser: Boolean,
    isAdmin: Boolean,
    onConfirmClick: () -> Unit,
    onRevertClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Аватар / инициалы
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = participant.userId.take(2).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Информация об участнике
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isCurrentUser) "Вы" else "Участник",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(вы)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Статус оплаты
                PaymentStatusChip(status = participant.status)

                // Время оплаты / подтверждения
                participant.paidAt.takeIf { !it.isAfter(Instant.EPOCH) }?.let { paidAt ->
                    Text(
                        text = "Оплата: ${formatDate(paidAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Кнопки админа
            if (isAdmin) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when {
                        participant.isPending -> {
                            // Кнопка "Подтвердить"
                            FilledTonalButton(
                                onClick = onConfirmClick,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("✓", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        participant.isConfirmed -> {
                            // Кнопка "Отменить подтверждение"
                            TextButton(
                                onClick = onRevertClick,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Отменить", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        else -> {
                            // Не оплачено
                            Text(
                                text = "Ожидается",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Сумма
                    participant.amount.takeIf { it.compareTo(BigDecimal.ZERO) > 0 }?.let { amount ->
                        Text(
                            text = "${formatRubles(amount)} ₽",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusChip(status: FundraiseParticipantStatus) {
    val (text, color) = when (status) {
        FundraiseParticipantStatus.Confirmed -> "✓ Подтверждено" to MaterialTheme.colorScheme.tertiaryContainer
        FundraiseParticipantStatus.Pending -> "⏳ На проверке" to MaterialTheme.colorScheme.primaryContainer
        FundraiseParticipantStatus.Paid -> "✓ Оплачено" to MaterialTheme.colorScheme.secondaryContainer
        FundraiseParticipantStatus.Reverted -> "✗ Отменено" to MaterialTheme.colorScheme.errorContainer
        FundraiseParticipantStatus.NotPaid -> "Не оплачено" to MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ParticipantListItemSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .width(100.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .width(80.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                )
            }
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// Close Fundraise Button (Admin Only)
// ═══════════════════════════════════════════════════════
@Composable
private fun CloseFundraiseButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        border = BorderSpec(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            )
        )
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Закрыть сбор")
    }
}

// ═══════════════════════════════════════════════════════
// Utility Functions
// ═══════════════════════════════════════════════════════
private fun formatRubles(amount: BigDecimal): String {
    return amount.toPlainString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1 ")
}

private fun formatDate(instant: Instant): String {
    // Простая реализация — можно заменить на java.time.format.DateTimeFormatter
    return instant.toString().substring(0, 16).replace("T", " ")
}

// Для BorderSpec, если не импортирован
private fun BorderSpec(
    width: Dp,
    brush: Brush
) = androidx.compose.foundation.BorderStroke(width, brush)