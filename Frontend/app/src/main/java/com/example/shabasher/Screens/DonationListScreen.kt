package com.example.shabasher.Screens

import android.R
import android.widget.ProgressBar
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.shabasher.Model.Donation
import com.example.shabasher.Model.DonationPaymentStatus
import com.example.shabasher.Model.DonationStatus
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.ViewModels.DonationListViewModel
import com.example.shabasher.components.FundraiseCardSkeleton
import com.example.shabasher.data.dto.Fundraise
import com.example.shabasher.ui.theme.ShabasherTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import java.time.format.TextStyle

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
        },

        // FAB только для админов/модераторов события
        floatingActionButton = {
            if (uiState.canCreateFundraise) {
                FloatingActionButton(
                    onClick = {
                        SafeNavigation.navigate {
                            navController.navigate(
                                Routes.createFundraise(viewModel.eventId)
                            )
                        }
                    },
                    containerColor = colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Создать сбор")
                }
            }
        }
    ) { innerPadding ->
        com.example.shabasher.components.AppPullToRefreshBox(
            isRefreshing = uiState.isLoading && uiState.donations.isNotEmpty(),
            onRefresh = { viewModel.loadDonations() },
            state = rememberPullToRefreshState(),
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    // Первая загрузка — skeletons
                    uiState.isLoading && uiState.donations.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(Modifier.height(16.dp))
                            repeat(4) { FundraiseCardSkeleton() }
                        }
                    }

                    uiState.donations.isEmpty() -> {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Image(
                                painter = painterResource(id = com.example.shabasher.R.drawable.manulnotlogin),
                                contentDescription = null,
                                modifier = Modifier.size(280.dp)
                            )
                            Text(
                                text = "В этом событии пока нет сборов",
                                color = colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
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
                }

            }
        }
    }
}

@Composable
fun DonationCard(
    donation: Fundraise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClosed = donation.fundStatus == com.example.shabasher.data.dto.FundStatus.Closed
    val isCompleted = donation.fundStatus == com.example.shabasher.data.dto.FundStatus.Completed

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isClosed) colorScheme.surface.copy(alpha = 0.6f)
            else colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Название
                Text(
                    text = donation.title,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )

                // Прогресс — собрано / цель
                Text(
                    text = donation.getProgressText(),
                    color = colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Статусы: статус сбора + статус моей оплаты
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val (fundText, fundFg, fundBg) = when {
                        isClosed -> Triple("Закрыт", colorScheme.onSurface, colorScheme.secondary.copy(alpha = 0.45f))
                        isCompleted -> Triple("Завершён", colorScheme.tertiary, colorScheme.tertiary.copy(alpha = 0.22f))
                        else -> Triple("Активен", colorScheme.primary, colorScheme.primary.copy(alpha = 0.18f))
                    }
                    StatusPill(fundText, fundFg, fundBg)

                    val myPayment = when {
                        donation.isPaymentConfirmed() -> Triple("Оплачено", colorScheme.tertiary, colorScheme.tertiary.copy(alpha = 0.2f))
                        donation.isPendingConfirmation() -> Triple("На проверке", colorScheme.primary, colorScheme.primary.copy(alpha = 0.18f))
                        donation.canMarkPaid() -> Triple("Не оплачено", colorScheme.onSurfaceVariant, colorScheme.surfaceVariant)
                        else -> null
                    }
                    if (myPayment != null) {
                        StatusPill(myPayment.first, myPayment.second, myPayment.third)
                    }
                }
            }

            // Прогресс — круг (если есть цель) или просто иконка
            if (donation.targetAmount != null && donation.targetAmount.signum() > 0) {
                CustomCircularPercentIndicator(
                    progress = donation.progressPercent / 100f,
                    size = 76.dp,
                    strokeWidth = 6.dp,
                    showPercent = true
                )
            } else {
                // Donation-mode: показываем сумму крупно
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₽",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, fg: Color, bg: Color) {
    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(color = bg, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun CustomCircularPercentIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 7.dp,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    progressGradient: Brush? = null, // опциональный градиент
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    showPercent: Boolean = true,
    strokeLineCap: StrokeCap = StrokeCap.Round,
    animationDuration: Int = 500
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = animationDuration, easing = LinearOutSlowInEasing),
        label = "customProgress"
    )

    val sweepAngle = animatedProgress * 360f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val diameter = size.toPx() - strokeWidth.toPx()
            val radius = diameter / 2f
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)

            // 📍 Фон (трек)
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth.toPx(), cap = strokeLineCap)
            )

            // 📍 Прогресс
            if (progressGradient != null) {
                drawArc(
                    brush = progressGradient,
                    startAngle = -90f, // начинаем с 12 часов
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    size = Size(diameter, diameter),
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    style = Stroke(width = strokeWidth.toPx(), cap = strokeLineCap)
                )
            } else {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    size = Size(diameter, diameter),
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    style = Stroke(width = strokeWidth.toPx(), cap = strokeLineCap)
                )
            }
        }

        // 🔢 Текст в центре
        if (showPercent) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    color = textColor
                )
                // Опционально: подпись под процентом
                // Text("заполнено", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.7f))
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
        //ProgressBar(0.6f)
    }
}