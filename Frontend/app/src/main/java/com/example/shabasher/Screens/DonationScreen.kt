package com.example.shabasher.Screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.Model.UserRole
import com.example.shabasher.ViewModels.DonationUiState
import com.example.shabasher.ViewModels.DonationViewModel
import com.example.shabasher.ViewModels.canManageFundraise
import com.example.shabasher.data.dto.FundStatus
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.data.dto.FundraiseParticipant
import com.example.shabasher.data.dto.FundraiseParticipantStatus
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    navController: NavController,
    viewModel: DonationViewModel,
    donationId: String,
    onNavigateBack: () -> Unit = { SafeNavigation.navigate { navController.popBackStack() } }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val currentUserId by remember { derivedStateOf { viewModel.getCurrentUserId() } }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(donationId) { viewModel.loadDonationById(donationId) }

    LaunchedEffect(actionState.success, actionState.error) {
        actionState.success?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearActionState()
        }
        actionState.error?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long, withDismissAction = true)
            viewModel.clearActionState()
        }
    }

    // После успешного удаления — возвращаемся назад к списку сборов
    LaunchedEffect(actionState.deleted) {
        if (actionState.deleted) {
            SafeNavigation.navigate { navController.popBackStack() }
        }
    }

    Scaffold(
        snackbarHost = { com.example.shabasher.components.AppSnackbarHost(snackbarHostState) },
        topBar = {
            DonationTopBar(
                state = uiState,
                donationId = donationId,
                navController = navController,
                onNavigateBack = onNavigateBack,
                onRefresh = viewModel::refresh,
                onCloseFundraise = viewModel::closeFundraise,
                onDeleteFundraise = viewModel::deleteFundraise
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        DonationScreenBody(
            uiState = uiState,
            actionState = actionState,
            currentUserId = currentUserId,
            paddingValues = paddingValues,
            onMarkPaid = viewModel::markPaid,
            onConfirmPayment = viewModel::confirmPayment,
            onRevertPayment = viewModel::revertPayment,
            onRefresh = viewModel::refresh
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DonationTopBar(
    state: DonationUiState,
    donationId: String,
    navController: NavController,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onCloseFundraise: () -> Unit,
    onDeleteFundraise: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val success = state as? DonationUiState.Success
    val canManage = success?.currentUserRole?.canManageFundraise() == true
    val canClose = canManage && success?.donation?.isActive == true

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = success?.donation?.title ?: "Сбор средств",
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Обновить")
            }
            if (canManage) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Действия")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать сбор") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, null, tint = colorScheme.onSurface)
                            },
                            onClick = {
                                menuExpanded = false
                                SafeNavigation.navigate {
                                    navController.navigate(
                                        com.example.shabasher.Model.Routes.editFundraise(donationId)
                                    )
                                }
                            }
                        )
                        if (canClose) {
                            DropdownMenuItem(
                                text = { Text("Закрыть сбор") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, null, tint = colorScheme.onSurface)
                                },
                                onClick = {
                                    menuExpanded = false
                                    showCloseDialog = true
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Удалить сбор", color = colorScheme.error) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = colorScheme.error)
                            },
                            onClick = {
                                menuExpanded = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colorScheme.background,
            titleContentColor = colorScheme.onBackground,
            navigationIconContentColor = colorScheme.onBackground,
            actionIconContentColor = colorScheme.onBackground
        )
    )

    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = {
                Text(
                    "Закрыть сбор?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "После закрытия новые оплаты приниматься не будут. " +
                        "Уже подтверждённые платежи останутся. Это действие нельзя отменить.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colorScheme.error
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCloseDialog = false
                        onCloseFundraise()
                    }
                ) {
                    Text(
                        "Закрыть",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) {
                    Text(
                        "Отмена",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                )
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Удалить сбор?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Это действие необратимо. Сбор и все записи об оплатах будут удалены.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colorScheme.error
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteFundraise()
                    }
                ) {
                    Text(
                        "Удалить",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        "Отмена",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DonationScreenBody(
    uiState: DonationUiState,
    actionState: com.example.shabasher.ViewModels.DonationActionState,
    currentUserId: String?,
    paddingValues: PaddingValues,
    onMarkPaid: () -> Unit,
    onConfirmPayment: (String, BigDecimal?) -> Unit,
    onRevertPayment: (String) -> Unit,
    onRefresh: () -> Unit
) {
    AnimatedContent(
        targetState = uiState,
        modifier = Modifier.padding(paddingValues),
        transitionSpec = {
            fadeIn(tween(220)) togetherWith fadeOut(tween(180))
        },
        contentKey = {
            when (it) {
                is DonationUiState.Loading -> "loading"
                is DonationUiState.Error -> "error"
                is DonationUiState.Success -> "success"
            }
        },
        label = "donationContent"
    ) { state ->
        when (state) {
            is DonationUiState.Loading -> LoadingState()
            is DonationUiState.Error -> ErrorState(message = state.message, onRetry = state.retry)
            is DonationUiState.Success -> {
                com.example.shabasher.components.AppPullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    state = rememberPullToRefreshState(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    DonationContent(
                        donation = state.donation,
                        currentUserId = currentUserId,
                        currentUserRole = state.currentUserRole,
                        participantNames = state.participantNames,
                        actionState = actionState,
                        onMarkPaid = onMarkPaid,
                        onConfirmPayment = onConfirmPayment,
                        onRevertPayment = onRevertPayment
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Loading / Error
// ═══════════════════════════════════════════════════════

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colorScheme.primary, strokeWidth = 3.dp)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = colorScheme.onBackground
            )
            Spacer(Modifier.height(24.dp))
            FilledTonalButton(
                onClick = onRetry,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Повторить")
            }
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
    currentUserRole: UserRole,
    participantNames: Map<String, String>,
    actionState: com.example.shabasher.ViewModels.DonationActionState,
    onMarkPaid: () -> Unit,
    onConfirmPayment: (String, BigDecimal?) -> Unit,
    onRevertPayment: (String) -> Unit
) {
    var participantToConfirm by remember { mutableStateOf<FundraiseParticipant?>(null) }
    var showMarkPaidConfirm by remember { mutableStateOf(false) }
    val isAdmin = currentUserRole.canManageFundraise()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroSection(donation)

        if (!donation.description.isNullOrBlank()) {
            DescriptionCard(description = donation.description)
        }

        MyPaymentSection(
            donation = donation,
            isProcessing = actionState.isLoading && actionState.pendingParticipantId == null,
            onMarkPaid = { showMarkPaidConfirm = true }
        )

        PaymentRequisitesSection(
            phone = donation.paymentPhone,
            recipient = donation.paymentRecipient,
            isHighlighted = donation.canMarkPaid()
        )

        if (isAdmin && donation.participants != null) {
            ParticipantsSection(
                participants = donation.participants,
                currentUserId = currentUserId,
                participantNames = participantNames,
                pendingParticipantId = actionState.pendingParticipantId,
                confirmedCount = donation.confirmedCount ?: 0,
                participantsCount = donation.participantsCount ?: donation.participants.size,
                onConfirmClick = { participantToConfirm = it },
                onRevertClick = { onRevertPayment(it.userId) }
            )
        }

        Spacer(Modifier.height(24.dp))
    }

    // Confirm dialog для пользователя — "Я оплатил"
    if (showMarkPaidConfirm) {
        AlertDialog(
            onDismissRequest = { showMarkPaidConfirm = false },
            title = {
                Text(
                    "Подтвердите оплату",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Убедитесь, что перевели деньги по реквизитам выше. " +
                        "После подтверждения организатор проверит зачисление и подтвердит оплату.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMarkPaidConfirm = false
                        onMarkPaid()
                    }
                ) {
                    Text(
                        "Я оплатил(а)",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkPaidConfirm = false }) {
                    Text(
                        "Отмена",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                )
            }
        )
    }

    // Confirm dialog для админа
    participantToConfirm?.let { participant ->
        ConfirmPaymentDialog(
            participantName = participantNames[participant.userId]
                ?: "Участник ${participant.userId.take(8)}",
            initialAmount = participant.amount,
            suggestedAmount = donation.targetAmount?.let { target ->
                val left = target - donation.currentAmount
                val remaining = ((donation.participantsCount ?: 1) - (donation.confirmedCount ?: 0))
                    .coerceAtLeast(1)
                if (left > BigDecimal.ZERO) left.divide(BigDecimal(remaining), 0, RoundingMode.HALF_UP) else null
            },
            onDismiss = { participantToConfirm = null },
            onConfirm = { amount ->
                onConfirmPayment(participant.userId, amount)
                participantToConfirm = null
            }
        )
    }
}

// ═══════════════════════════════════════════════════════
// Hero / Progress
// ═══════════════════════════════════════════════════════

@Composable
private fun HeroSection(donation: Fundraise) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            colorScheme.primary.copy(alpha = 0.18f),
            colorScheme.primary.copy(alpha = 0.06f)
        )
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.background(gradient)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusChip(donation.fundStatus)
                Spacer(Modifier.height(20.dp))

                if (donation.targetAmount != null && donation.targetAmount > BigDecimal.ZERO) {
                    CircularPercentIndicator(
                        progress = donation.progressPercent / 100f,
                        size = 160.dp,
                        strokeWidth = 12.dp
                    )
                } else {
                    DonationOnlyIndicator(currentAmount = donation.currentAmount)
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        title = "Собрано",
                        value = "${formatRubles(donation.currentAmount)} ₽",
                        emphasized = true
                    )
                    if (donation.targetAmount != null) {
                        VerticalSeparator()
                        StatItem(
                            title = "Цель",
                            value = "${formatRubles(donation.targetAmount)} ₽"
                        )
                    }
                    if (donation.participantsCount != null && donation.participantsCount > 0) {
                        VerticalSeparator()
                        StatItem(
                            title = "Оплатили",
                            value = "${donation.confirmedCount ?: 0}/${donation.participantsCount}"
                        )
                    }
                }

                if (donation.targetAmount != null) {
                    val remaining = (donation.targetAmount - donation.currentAmount).coerceAtLeast(BigDecimal.ZERO)
                    if (remaining > BigDecimal.ZERO) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Осталось собрать: ${formatRubles(remaining)} ₽",
                            style = MaterialTheme.typography.labelLarge,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(title: String, value: String, emphasized: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.SemiBold,
            color = if (emphasized) colorScheme.primary else colorScheme.onSurface
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VerticalSeparator() {
    Box(
        modifier = Modifier
            .height(28.dp)
            .width(1.dp)
            .background(colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
    )
}

@Composable
private fun DonationOnlyIndicator(currentAmount: BigDecimal) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = "${formatRubles(currentAmount)} ₽",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 40.sp),
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary
        )
        Text(
            text = "собрано",
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusChip(status: FundStatus) {
    val (text, bg, fg) = when (status) {
        FundStatus.Active -> Triple("Активен", colorScheme.primary.copy(alpha = 0.2f), colorScheme.primary)
        FundStatus.Completed -> Triple("Завершён", colorScheme.tertiary.copy(alpha = 0.22f), colorScheme.tertiary)
        FundStatus.Closed -> Triple("Закрыт", colorScheme.secondary.copy(alpha = 0.5f), colorScheme.onSurface)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "О сборе",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// My payment
// ═══════════════════════════════════════════════════════

@Composable
private fun MyPaymentSection(
    donation: Fundraise,
    isProcessing: Boolean,
    onMarkPaid: () -> Unit
) {
    val branch = when {
        donation.isPaymentConfirmed() -> MyPaymentBranch.Confirmed
        donation.isPendingConfirmation() -> MyPaymentBranch.Pending
        donation.canMarkPaid() -> MyPaymentBranch.CanPay
        donation.isClosed -> MyPaymentBranch.Closed
        donation.isCompleted -> MyPaymentBranch.Completed
        else -> MyPaymentBranch.None
    }

    AnimatedContent(
        targetState = branch,
        modifier = Modifier.fillMaxWidth(),
        transitionSpec = {
            (fadeIn(tween(180)) + expandVertically()) togetherWith
                (fadeOut(tween(120)) + shrinkVertically())
        },
        label = "myPayment"
    ) { current ->
        when (current) {
            MyPaymentBranch.Confirmed -> StatusBanner(
                icon = Icons.Default.CheckCircle,
                title = "Спасибо! Ваша оплата подтверждена",
                subtitle = "Организатор зачислил ваш платёж",
                bg = colorScheme.tertiary.copy(alpha = 0.20f),
                fg = colorScheme.tertiary
            )
            MyPaymentBranch.Pending -> StatusBanner(
                icon = Icons.Default.HourglassTop,
                title = "Оплата на проверке",
                subtitle = "Организатор подтвердит её, когда увидит зачисление",
                bg = colorScheme.primary.copy(alpha = 0.18f),
                fg = colorScheme.primary
            )
            MyPaymentBranch.CanPay -> Button(
                onClick = onMarkPaid,
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
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
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Я оплатил(а)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
            MyPaymentBranch.Closed -> StatusBanner(
                icon = Icons.Default.Lock,
                title = "Сбор закрыт",
                subtitle = "Новые оплаты не принимаются",
                bg = colorScheme.secondary.copy(alpha = 0.5f),
                fg = colorScheme.onSurface
            )
            MyPaymentBranch.Completed -> StatusBanner(
                icon = Icons.Default.CheckCircle,
                title = "Сбор завершён",
                subtitle = "Цель достигнута",
                bg = colorScheme.tertiary.copy(alpha = 0.22f),
                fg = colorScheme.tertiary
            )
            MyPaymentBranch.None -> Spacer(Modifier.height(0.dp))
        }
    }
}

private enum class MyPaymentBranch { Confirmed, Pending, CanPay, Closed, Completed, None }

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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = fg, fontWeight = FontWeight.SemiBold)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = fg.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Requisites
// ═══════════════════════════════════════════════════════

@Composable
private fun PaymentRequisitesSection(
    phone: String,
    recipient: String,
    isHighlighted: Boolean
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) colorScheme.surface else colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Реквизиты для оплаты",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colorScheme.primary.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "СБП",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(color = colorScheme.surfaceVariant)

            RequisiteRow(icon = Icons.Default.Phone, label = "Телефон", value = phone)

            if (recipient.isNotBlank()) {
                RequisiteRow(icon = Icons.Default.Person, label = "Получатель", value = recipient)
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
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
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

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = colorScheme.primary.copy(alpha = 0.16f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
            }
            IconButton(
                onClick = {
                    clipboard.setText(AnnotatedString(value))
                    Toast.makeText(context, "$label скопирован", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Default.ContentCopy, "Копировать", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
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
    participantNames: Map<String, String>,
    pendingParticipantId: String?,
    confirmedCount: Int,
    participantsCount: Int,
    onConfirmClick: (FundraiseParticipant) -> Unit,
    onRevertClick: (FundraiseParticipant) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Участники сбора",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colorScheme.primary.copy(alpha = 0.14f)
            ) {
                Text(
                    text = "$confirmedCount / $participantsCount",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }

        if (participants.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Text(
                    text = "Пока никто не отметил оплату",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            participants
                .sortedWith(
                    compareBy(
                        { participantSortOrder(it.status) },
                        { it.paidAt ?: Instant.MAX }
                    )
                )
                .forEach { participant ->
                    val displayName = participantNames[participant.userId]
                        ?: ("Участник " + participant.userId.take(8))
                    ParticipantRow(
                        participant = participant,
                        displayName = displayName,
                        isCurrentUser = participant.userId == currentUserId,
                        isProcessing = pendingParticipantId == participant.userId,
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
    displayName: String,
    isCurrentUser: Boolean,
    isProcessing: Boolean,
    onConfirmClick: () -> Unit,
    onRevertClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
            Avatar(name = displayName)
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (isCurrentUser) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colorScheme.primary.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "вы",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                ParticipantStatusChip(participant.status)

                participant.paidAt?.takeIf { it.isAfter(Instant.EPOCH) }?.let { paidAt ->
                    Text(
                        text = "Отметил: ${formatDateTime(paidAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                participant.amount.takeIf { it > BigDecimal.ZERO }?.let {
                    Text(
                        text = "${formatRubles(it)} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (participant.isConfirmed) colorScheme.tertiary else colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Box(modifier = Modifier.padding(start = 8.dp), contentAlignment = Alignment.Center) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else when (participant.status) {
                    // Юзер отметил оплату — admin может подтвердить
                    FundraiseParticipantStatus.Pending,
                    FundraiseParticipantStatus.Paid -> Button(
                        onClick = onConfirmClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Принять", style = MaterialTheme.typography.labelMedium)
                    }
                    // Подтверждено — можно отменить
                    FundraiseParticipantStatus.Confirmed -> TextButton(
                        onClick = onRevertClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Undo, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Отменить", style = MaterialTheme.typography.labelMedium)
                    }
                    // NotPaid / Reverted — юзер не отметил оплату, кнопок нет.
                    // Админ может принять оплату только после того как юзер сам нажал «Я оплатил».
                    FundraiseParticipantStatus.NotPaid,
                    FundraiseParticipantStatus.Reverted -> Unit
                }
            }
        }
    }
}

@Composable
private fun Avatar(name: String) {
    // Стиль аватара такой же, как в ParticipantsPage:
    // круг surfaceVariant с иконкой Person.
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ParticipantStatusChip(status: FundraiseParticipantStatus) {
    val (text, bg, fg) = when (status) {
        FundraiseParticipantStatus.Confirmed -> Triple("Подтверждено", colorScheme.tertiary.copy(alpha = 0.20f), colorScheme.tertiary)
        FundraiseParticipantStatus.Pending,
        FundraiseParticipantStatus.Paid -> Triple("На проверке", colorScheme.primary.copy(alpha = 0.18f), colorScheme.primary)
        FundraiseParticipantStatus.Reverted -> Triple("Отклонено", colorScheme.secondary.copy(alpha = 0.45f), colorScheme.onSurface)
        FundraiseParticipantStatus.NotPaid -> Triple("Не оплачено", colorScheme.surfaceVariant, colorScheme.onSurfaceVariant)
    }
    Surface(shape = RoundedCornerShape(10.dp), color = bg) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════
// Confirm dialog (admin)
// ═══════════════════════════════════════════════════════

@Composable
private fun ConfirmPaymentDialog(
    participantName: String,
    initialAmount: BigDecimal,
    suggestedAmount: BigDecimal?,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal?) -> Unit
) {
    val initialText = when {
        initialAmount > BigDecimal.ZERO -> initialAmount.stripTrailingZeros().toPlainString()
        suggestedAmount != null && suggestedAmount > BigDecimal.ZERO -> suggestedAmount.toPlainString()
        else -> ""
    }
    var amountText by remember { mutableStateOf(initialText) }
    val parsedAmount = amountText.replace(',', '.').toBigDecimalOrNull()
    val isValid = amountText.isBlank() || (parsedAmount != null && parsedAmount >= BigDecimal.ZERO)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Подтвердить оплату",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = participantName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Укажите фактическую сумму, которую перевёл участник. Поле можно оставить пустым.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
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
                if (suggestedAmount != null && initialAmount.compareTo(BigDecimal.ZERO) == 0) {
                    Text(
                        text = "Подсказка: чтобы достичь цели сбора — около ${formatRubles(suggestedAmount)} ₽",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(parsedAmount) },
                enabled = isValid
            ) {
                Text(
                    "Подтвердить",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isValid) colorScheme.primary else colorScheme.onSurfaceVariant
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Отмена",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = colorScheme.onSurface,
                modifier = Modifier.size(36.dp)
            )
        }
    )
}

// ═══════════════════════════════════════════════════════
// Circular indicator
// ═══════════════════════════════════════════════════════

@Composable
private fun CircularPercentIndicator(
    progress: Float,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = clamped,
        animationSpec = tween(durationMillis = 700, easing = LinearOutSlowInEasing),
        label = "donationProgress"
    )
    val sweep = animated * 360f
    val track = colorScheme.onSurface.copy(alpha = 0.08f)
    val gradient = Brush.sweepGradient(
        colors = listOf(
            colorScheme.primary.copy(alpha = 0.6f),
            colorScheme.primary,
            colorScheme.primary.copy(alpha = 0.6f)
        )
    )

    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val diameter = this.size.minDimension - strokeWidth.toPx()
            val radius = diameter / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            drawCircle(
                color = track,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                brush = gradient,
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
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Text(
                text = "собрано",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// Utils
// ═══════════════════════════════════════════════════════

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

private fun formatDateTime(instant: Instant): String =
    instant.atZone(ZoneId.systemDefault()).format(dateFormatter)

private fun formatRubles(amount: BigDecimal): String {
    val rounded = amount.setScale(0, RoundingMode.DOWN).toPlainString()
    return rounded.replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1 ")
}
