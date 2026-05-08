package com.example.shabasher.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════
// Базовые примитивы
// ═══════════════════════════════════════════════════════

/** Mod-функция: рисует серый фон + бегущую полосу блика. */
@Composable
fun Modifier.shimmerSkeleton(shape: Shape = RoundedCornerShape(8.dp)): Modifier {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val translate by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeletonTranslate"
    )
    val base = colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val highlight = colorScheme.surfaceVariant.copy(alpha = 0.95f)

    return this
        .background(base, shape)
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val width = size.width
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, highlight, Color.Transparent),
                    start = Offset(width * (translate - 0.5f), 0f),
                    end = Offset(width * (translate + 0.5f), size.height)
                )
            )
        }
}

@Composable
fun SkeletonBox(
    width: Dp,
    height: Dp,
    shape: Shape = RoundedCornerShape(6.dp),
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(width = width, height = height).shimmerSkeleton(shape))
}

@Composable
fun SkeletonText(
    fraction: Float = 1f,
    height: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(fraction)
            .height(height)
            .shimmerSkeleton(RoundedCornerShape(4.dp))
    )
}

@Composable
fun SkeletonCircle(diameter: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(diameter).shimmerSkeleton(CircleShape))
}

// ═══════════════════════════════════════════════════════
// Скелет EventCard (главная)
// Силуэт точно повторяет EventCard.kt:
//   Row [hor16/vert8 padding, surface bg, 20dp shape, inner 16dp]
//   Box 96dp (картинка) | Spacer 16 | Column[weight=1] { title, descr 3 lines, status }
// ═══════════════════════════════════════════════════════

@Composable
fun EventCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        SkeletonBox(width = 96.dp, height = 96.dp, shape = RoundedCornerShape(20.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Заголовок (titleMedium ≈ 16sp)
            SkeletonText(fraction = 0.75f, height = 18.dp)
            Spacer(Modifier.height(8.dp))
            // Описание — 2 строки
            SkeletonText(fraction = 0.95f, height = 14.dp)
            Spacer(Modifier.height(4.dp))
            SkeletonText(fraction = 0.6f, height = 14.dp)
            Spacer(Modifier.height(12.dp))
            // Статус (labelMedium)
            SkeletonText(fraction = 0.35f, height = 12.dp)
        }
    }
}

// ═══════════════════════════════════════════════════════
// Скелет DonationCard (список сборов)
// Силуэт повторяет DonationCard:
//   Card 20dp shape, padding 16dp
//   Row align center { Column(weight=1) { title, progressText, Row{pill, pill} }, Circle 76dp }
// ═══════════════════════════════════════════════════════

@Composable
fun FundraiseCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Заголовок
            SkeletonText(fraction = 0.85f, height = 18.dp)
            // Прогресс-текст «N из M ₽»
            SkeletonText(fraction = 0.55f, height = 14.dp)
            // Ряд бейджей статуса
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonBox(width = 64.dp, height = 20.dp, shape = RoundedCornerShape(10.dp))
                SkeletonBox(width = 88.dp, height = 20.dp, shape = RoundedCornerShape(10.dp))
            }
        }
        // Круговой индикатор справа
        SkeletonCircle(diameter = 76.dp)
    }
}

// ═══════════════════════════════════════════════════════
// Скелет ParticipantRow
// Силуэт повторяет ParticipantRow:
//   Box surface, 12dp shape, 12dp padding
//   Row align center { Circle 48dp, Spacer 16, Column { name, role } }
// ═══════════════════════════════════════════════════════

@Composable
fun ParticipantRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonCircle(diameter = 48.dp)
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SkeletonText(fraction = 0.5f, height = 16.dp)
            SkeletonText(fraction = 0.3f, height = 12.dp)
        }
    }
}

// ═══════════════════════════════════════════════════════
// Скелет полного экрана события (EventPage)
// Силуэт повторяет EventContent:
//   1) EventInfo: surface card { квадрат 340 + title + description (3 строки) }
//   2) EventMoreInfo: surface card { header + 3 строки иконка+текст }
//   3) AddToCalendarButton — outlined 48dp
//   4) OpenInNavigatorButton — outlined 48dp
//   5) ParticipatorsCard — surface block
// ═══════════════════════════════════════════════════════

@Composable
fun EventDetailsSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1) EventInfo: главная картинка + заголовок + описание
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Большая картинка-плейсхолдер
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .shimmerSkeleton(RoundedCornerShape(20.dp))
            )
            // Заголовок (titleLarge)
            SkeletonText(fraction = 0.6f, height = 24.dp)
            // Описание — 3 строки
            SkeletonText(fraction = 0.95f, height = 14.dp)
            SkeletonText(fraction = 0.92f, height = 14.dp)
            SkeletonText(fraction = 0.7f, height = 14.dp)
        }

        // 2) EventMoreInfo: 3 строки (дата / время / место) + 2 кнопки
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FullInfoRowSkeleton()
            FullInfoRowSkeleton()
            FullInfoRowSkeleton()
            // Кнопка «Добавить в календарь»
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shimmerSkeleton(RoundedCornerShape(20.dp))
            )
            // Кнопка «Открыть в картах»
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shimmerSkeleton(RoundedCornerShape(20.dp))
            )
        }

        // 3) Карточка участников: header + 2-3 строки
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SkeletonText(fraction = 0.4f, height = 16.dp)
            ParticipantInlineRowSkeleton()
            ParticipantInlineRowSkeleton()
            ParticipantInlineRowSkeleton()
        }
    }
}

/** Строка «иконка + текст» внутри EventMoreInfo. */
@Composable
private fun InfoRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonBox(width = 24.dp, height = 24.dp, shape = RoundedCornerShape(6.dp))
        Spacer(Modifier.width(12.dp))
        SkeletonText(fraction = 0.7f, height = 16.dp)
    }
}

/** Строка «круглая иконка + значение». */
@Composable
private fun FullInfoRowSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonCircle(diameter = 40.dp)
        Spacer(Modifier.width(14.dp))
        SkeletonText(fraction = 0.65f, height = 16.dp)
    }
}

/** Компактная строка участника без своей карточки — для секций внутри EventDetailsSkeleton. */
@Composable
private fun ParticipantInlineRowSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonCircle(diameter = 36.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SkeletonText(fraction = 0.5f, height = 14.dp)
            SkeletonText(fraction = 0.25f, height = 10.dp)
        }
    }
}

/** Полноэкранный список одинаковых скелетов. */
@Composable
fun SkeletonList(
    itemCount: Int = 6,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    spacing: Dp = 12.dp,
    item: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        repeat(itemCount) { item() }
    }
}
