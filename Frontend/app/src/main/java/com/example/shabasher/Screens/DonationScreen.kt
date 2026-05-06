package com.example.shabasher.Screens

import android.widget.Toast
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.ViewModels.DonationUiState
import com.example.shabasher.ViewModels.DonationViewModel
import com.example.shabasher.data.dto.FundStatus
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.data.dto.FundraiseParticipant
import com.example.shabasher.data.dto.FundraiseParticipantStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    navController: NavController,
    viewModel: DonationViewModel,
    donationId: String,
    onNavigateBack: () -> Unit = {
        SafeNavigation.navigate { navController.popBackStack() }
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val currentUserId by remember { derivedStateOf { viewModel.getCurrentUserId() } }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(donationId) { viewModel.loadDonationById(donationId) }

    LaunchedEffect(actionState) {
        actionState.success?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearActionState()
        }
        actionState.error?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long, withDismissAction = true)
            viewModel.clearActionState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = (uiState as? DonationUiState.Success)?.donation?.title ?: "Сбор средств",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground,
                    navigationIconContentColor = colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DonationUiState.Loading -> LoadingState(Modifier.padding(paddingValues))
            is DonationUiState.Error -> ErrorState(
                message = state.message,
                onRetry = state.retry,
                modifier = Modifier.padding(paddingValues)
            )
            is DonationUiState.Success -> DonationContent(
                donation = state.donation,
                currentUserId = currentUserId,
                isProcessing = actionState.isLoading,
                onMarkPaid = viewModel::markPaid,
                onConfirmPayment = { userId, amount -> viewModel.confirmPayment(userId, amount) },
                onRevertPayment = viewModel::revertPayment,
                onCloseFundraise = viewModel::closeFundraise,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// Loading / Error
// ═══════════════════════════════════════════════════════

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colorScheme.primary)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            FilledTonalButton(onClick = onRetry) { Text("Повторить") }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Main content
// ═══════════════════════════════════════════════════════

@Composable
private fun DonationContent(
    donation: Fundraise,
    currentUserId: String?,
    isProcessing: Boolean,
    onMarkPaid: () -> Unit,
    onConfirmPayment: (String, BigDecimal?) -> Unit,
    onRevertPayment: (String) -> Unit,
    onCloseFundraise: () -> Unit,
    modifier: Modifier = Modifier
) {
    var participantToConfirm by remember { mutableStateOf<FundraiseParticipant?>(null) }
    var showCloseDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ProgressSection(donation)

        if (!donation.description.isNullOrBlank()) {
            DescriptionCard(description = donation.description)
        }

        MyPaymentSection(
            donation = donation,
            isProcessing = isProcessing,
            onMarkPaid = onMarkPaid
        )

        PaymentRequisitesSection(
            phone = donation.paymentPhone,
            recipient = donation.paymentRecipient
        )

        if (donation.canConfirmPayments() && donation.participants != null) {
            ParticipantsSection(
                participants = donation.participants,
                currentUserId = currentUserId,
                confirmedCount = donation.confirmedCount ?: 0,
                participantsCount = donation.participantsCount ?: donation.participants.size,
                onConfirmClick = { participantToConfirm = it },
                onRevertClick = { onRevertPayment(it.userId) }
            )
        }

        if (donation.canCloseFundraise() && donation.isActive) {
            CloseFundraiseButton(onClick = { showCloseDialog = true })
        }

        Spacer(Modifier.height(8.dp))
    }

    participantToConfirm?.let { participant ->
        ConfirmPaymentDialog(
            initialAmount = participant.amount,
            onDismiss = { participantToConfirm = null },
            onConfirm = { amount ->
                onConfirmPayment(participant.userId, amount)
                participantToConfirm = null
            }
        )
    }

    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Закрыть сбор?") },
            text = { Text("После закрытия новые оплаты приниматься не будут. Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCloseDialog = false
                        onCloseFundraise()
                    }
                ) { Text("Закрыть", color = colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) { Text("Отмена") }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════
// Progress
// ═══════════════════════════════════════════════════════

@Composable
private fun ProgressSection(donation: Fundraise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularPercentIndicator(
                progress = donation.progressPercent / 100f,
                size = 140.dp,
                strokeWidth = 10.dp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "${formatRubles(donation.currentAmount)} ₽",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )
            donation.targetAmount?.let { target ->
                Text(
                    text = "из ${formatRubles(target)} ₽",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            } ?: Text(
                text = "цель не задана",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )

            donation.participantsCount?.let { total ->
                val confirmed = donation.confirmedCount ?: 0
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Оплатили: $confirmed из $total",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            StatusChip(donation.fundStatus)
        }
    }
}

@Composable
private fun StatusChip(status: FundStatus) {
    val (text, bg, fg) = when (status) {
        FundStatus.Active -> Triple("Активен", colorScheme.primary.copy(alpha = 0.18f), colorScheme.primary)
        FundStatus.Completed -> Triple("Завершён", colorScheme.tertiary.copy(alpha = 0.20f), colorScheme.tertiary)
        FundStatus.Closed -> Triple("Закрыт", colorScheme.secondary.copy(alpha = 0.40f), colorScheme.onSurface)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════
// Description
// ═══════════════════════════════════════════════════════

@Composable
private fun DescriptionCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Описание",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// My payment section
// ═══════════════════════════════════════════════════════

@Composable
private fun MyPaymentSection(
    donation: Fundraise,
    isProcessing: Boolean,
    onMarkPaid: () -> Unit
) {
    when {
        donation.isPaymentConfirmed() -> {
            StatusBanner(
                icon = Icons.Default.CheckCircle,
                title = "Ваша оплата подтверждена",
                subtitle = "Спасибо за участие!",
                bg = colorScheme.tertiary.copy(alpha = 0.20f),
                fg = colorScheme.tertiary
            )
        }
        donation.isPendingConfirmation() -> {
            StatusBanner(
                icon = Icons.Default.HourglassTop,
                title = "Оплата отправлена на проверку",
                subtitle = "Ожидайте подтверждения от организатора",
                bg = colorScheme.primary.copy(alpha = 0.18f),
                fg = colorScheme.primary
            )
        }
        donation.canMarkPaid() -> {
            Button(
                onClick = onMarkPaid,
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Я оплатил(а)", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
        donation.isClosed -> {
            StatusBanner(
                icon = Icons.Default.Lock,
                title = "Сбор закрыт",
                subtitle = "Новые оплаты не принимаются",
                bg = colorScheme.secondary.copy(alpha = 0.4f),
                fg = colorScheme.onSurface
            )
        }
        donation.isCompleted -> {
            StatusBanner(
                icon = Icons.Default.CheckCircle,
                title = "Сбор завершён",
                subtitle = "Цель достигнута",
                bg = colorScheme.tertiary.copy(alpha = 0.20f),
                fg = colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun StatusBanner(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    bg: Color,
    fg: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = fg, fontWeight = FontWeight.SemiBold)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = fg.copy(alpha = 0.85f))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Requisites
// ═══════════════════════════════════════════════════════

@Composable
private fun PaymentRequisitesSection(phone: String, recipient: String) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
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
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )

            HorizontalDivider(color = colorScheme.surfaceVariant)

            RequisiteRow(
                icon = Icons.Default.Phone,
                label = "Телефон (СБП)",
                value = phone
            )

            if (recipient.isNotBlank()) {
                RequisiteRow(
                    icon = Icons.Default.Person,
                    label = "Получатель",
                    value = recipient
                )
            }

            TextButton(
                onClick = {
                    val text = buildString {
                        appendLine("Телефон: $phone")
                        if (recipient.isNotBlank()) appendLine("Получатель: $recipient")
                    }.trimEnd()
                    clipboard.setText(AnnotatedString(text))
                    Toast.makeText(context, "Реквизиты скопированы", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Скопировать всё")
            }
        }
    }
}

@Composable
private fun RequisiteRow(icon: ImageVector, label: String, value: String) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = colorScheme.onSurface)
        }
        IconButton(
            onClick = {
                clipboard.setText(AnnotatedString(value))
                Toast.makeText(context, "$label скопирован", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Копировать", modifier = Modifier.size(18.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════
// Participants (admin only)
// ═══════════════════════════════════════════════════════

@Composable
private fun ParticipantsSection(
    participants: List<FundraiseParticipant>,
    currentUserId: String?,
    confirmedCount: Int,
    participantsCount: Int,
    onConfirmClick: (FundraiseParticipant) -> Unit,
    onRevertClick: (FundraiseParticipant) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Участники сбора",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$confirmedCount / $participantsCount",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.primary
            )
        }

        if (participants.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Text(
                    text = "Пока никто не отметил оплату",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            participants
                .sortedWith(
                    compareBy(
                        { participantSortOrder(it.status) },
                        { it.paidAt }
                    )
                )
                .forEach { participant ->
                    ParticipantRow(
                        participant = participant,
                        isCurrentUser = participant.userId == currentUserId,
                        onConfirmClick = { onConfirmClick(participant) },
                        onRevertClick = { onRevertClick(participant) }
                    )
                }
        }
    }
}

private fun participantSortOrder(status: FundraiseParticipantStatus): Int = when (status) {
    FundraiseParticipantStatus.Pending -> 0
    FundraiseParticipantStatus.Paid -> 1
    FundraiseParticipantStatus.Confirmed -> 2
    FundraiseParticipantStatus.Reverted -> 3
    FundraiseParticipantStatus.NotPaid -> 4
}

@Composable
private fun ParticipantRow(
    participant: FundraiseParticipant,
    isCurrentUser: Boolean,
    onConfirmClick: () -> Unit,
    onRevertClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) colorScheme.primary.copy(alpha = 0.10f)
            else colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = participant.userId.take(2).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = if (isCurrentUser) "Вы" else "Участник ${participant.userId.take(8)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )
                ParticipantStatusChip(participant.status)
                if (participant.paidAt.isAfter(Instant.EPOCH)) {
                    Text(
                        text = "Отметил: ${formatDate(participant.paidAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                participant.amount.takeIf { it > BigDecimal.ZERO }?.let {
                    Text(
                        text = "${formatRubles(it)} ₽",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            when (participant.status) {
                FundraiseParticipantStatus.Pending,
                FundraiseParticipantStatus.Paid -> {
                    FilledTonalButton(
                        onClick = onConfirmClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Принять", style = MaterialTheme.typography.labelMedium)
                    }
                }
                FundraiseParticipantStatus.Confirmed -> {
                    TextButton(
                        onClick = onRevertClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                    ) {
                        Text("Отменить", style = MaterialTheme.typography.labelMedium)
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun ParticipantStatusChip(status: FundraiseParticipantStatus) {
    val (text, bg, fg) = when (status) {
        FundraiseParticipantStatus.Confirmed -> Triple("Подтверждено", colorScheme.tertiary.copy(alpha = 0.20f), colorScheme.tertiary)
        FundraiseParticipantStatus.Pending,
        FundraiseParticipantStatus.Paid -> Triple("На проверке", colorScheme.primary.copy(alpha = 0.18f), colorScheme.primary)
        FundraiseParticipantStatus.Reverted -> Triple("Отклонено", colorScheme.secondary.copy(alpha = 0.40f), colorScheme.onSurface)
        FundraiseParticipantStatus.NotPaid -> Triple("Не оплачено", colorScheme.surfaceVariant, colorScheme.onSurfaceVariant)
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════
// Confirm dialog
// ═══════════════════════════════════════════════════════

@Composable
private fun ConfirmPaymentDialog(
    initialAmount: BigDecimal,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal?) -> Unit
) {
    var amountText by remember {
        mutableStateOf(
            if (initialAmount > BigDecimal.ZERO) initialAmount.toPlainString() else ""
        )
    }
    val parsedAmount = amountText.replace(',', '.').toBigDecimalOrNull()
    val isValid = amountText.isBlank() || (parsedAmount != null && parsedAmount >= BigDecimal.ZERO)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Подтвердить оплату") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Введите сумму, которую перевёл участник. Можно оставить пустым.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { new -> amountText = new.filter { it.isDigit() || it == '.' || it == ',' } },
                    label = { Text("Сумма, ₽") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = !isValid,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(parsedAmount) },
                enabled = isValid
            ) { Text("Подтвердить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

// ═══════════════════════════════════════════════════════
// Close button
// ═══════════════════════════════════════════════════════

@Composable
private fun CloseFundraiseButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.error)
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Закрыть сбор")
    }
}

// ═══════════════════════════════════════════════════════
// Circular indicator
// ═══════════════════════════════════════════════════════

@Composable
private fun CircularPercentIndicator(
    progress: Float,
    size: androidx.compose.ui.unit.Dp = 140.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 10.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = clamped,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "donationProgress"
    )
    val sweep = animated * 360f
    val track = colorScheme.surfaceVariant
    val fill = colorScheme.primary

    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val diameter = this.size.minDimension - strokeWidth.toPx()
            val radius = diameter / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            drawCircle(color = track, radius = radius, center = center, style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round))
            drawArc(
                color = fill,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                size = Size(diameter, diameter),
                topLeft = Offset(center.x - radius, center.y - radius),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animated * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Text(
                text = "собрано",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// Utils
// ═══════════════════════════════════════════════════════

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

private fun formatDate(instant: Instant): String =
    instant.atZone(ZoneId.systemDefault()).format(dateFormatter)

private fun formatRubles(amount: BigDecimal): String =
    amount.setScale(0, java.math.RoundingMode.DOWN)
        .toPlainString()
        .replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1 ")
