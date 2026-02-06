package com.example.shabasher.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.widget.Toast
import androidx.compose.ui.graphics.painter.Painter
import com.example.shabasher.Model.Routes

@Composable
fun ServiceCard(navController: NavController) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            "Сервисы",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ServiceButton(
                title = "Предложения",
                iconVector = Icons.Default.Lightbulb,
                onClick = {
                    navController.navigate(Routes.SUGGESTIONS)
                }
            )
            ServiceButton(
                title = "Сбор средств",
                painter = painterResource(id = com.example.shabasher.R.drawable.ruble),
                onClick = {
                    navController.navigate(Routes.DONATION)
                }
            )
        }
    }
}

@Composable
fun GamesCard() {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            "Игры",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ServiceButton(
                title = "Правда или\nдействие",
                iconVector = Icons.Default.CheckCircle,
                onClick = {
                    Toast.makeText(context, "Экран в разработке", Toast.LENGTH_SHORT).show()
                }
            )
            ServiceButton(
                title = "Мафия",
                iconVector = Icons.Default.Bedtime,
                onClick = {
                    Toast.makeText(context, "Экран в разработке", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// Основная функция с поддержкой и ImageVector, и Painter
@Composable
fun ServiceButton(
    title: String,
    iconVector: ImageVector? = null,
    painter: Painter? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp, horizontal = 16.dp)
                .width(115.dp)
                .height(115.dp),
            contentAlignment = Alignment.Center
        ) {
            if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (painter != null) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Альтернативная версия с только ImageVector для обратной совместимости
@Composable
fun ServiceButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ServiceButton(
        title = title,
        iconVector = icon,
        onClick = onClick
    )
}
