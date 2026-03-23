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
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.ViewModels.DonationListViewModel
import com.example.shabasher.ui.theme.ShabasherTheme
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // ← Общий паддинг карточки
            horizontalArrangement = Arrangement.SpaceBetween, // ← Текст слева, индикатор справа
            verticalAlignment = Alignment.Top // ← Выравнивание по верху для корректного переноса
        ) {
            // 📝 Левая колонка с информацией (занимает всё доступное место)
            Column(
                modifier = Modifier
                    .weight(1f) // ← Ключевое: позволяет тексту переноситься, не толкая индикатор
                    .padding(end = 12.dp), // ← Отступ от индикатора
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = donation.title,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                    // Текст автоматически переносится, так как Column имеет weight(1f)
                )

                Text(
                    text = "Собрано ${donation.collectedAmount} ₽ из ${donation.targetAmount} ₽",
                    color = colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Статус оплаты в «чипсе»
                val statusText = when (donation.paymentStatus) {
                    DonationPaymentStatus.PAID -> "Оплачено"
                    DonationPaymentStatus.NOT_PAID -> "Не оплачено"
                }
                val statusColor = when (donation.paymentStatus) {
                    DonationPaymentStatus.PAID -> colorScheme.primary
                    DonationPaymentStatus.NOT_PAID -> colorScheme.onSurface
                }
                val statusBgColor = when (donation.paymentStatus) {
                    DonationPaymentStatus.PAID -> colorScheme.primary.copy(alpha = 0.2f)
                    DonationPaymentStatus.NOT_PAID -> colorScheme.secondary.copy(alpha = 0.4f)
                }

                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .background(
                            color = statusBgColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // 📊 Индикатор прогресса (фиксированный размер, не сжимается)
            CustomCircularPercentIndicator(
                progress = if (donation.targetAmount > 0) {
                    donation.collectedAmount.toFloat() / donation.targetAmount
                } else 0f,
                size = 80.dp, // ← Чуть компактнее для карточки
                strokeWidth = 6.dp,
                showPercent = true,
                modifier = Modifier
                    .align(Alignment.Top) // ← Явное выравнивание по верху
            )
        }
    }
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