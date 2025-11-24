package com.example.shabasher.Screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shabasher.components.ShabasherPrimaryButton
import com.example.shabasher.components.ShabasherSecondaryButton
import com.example.shabasher.Model.Routes
import com.example.shabasher.R
import com.example.shabasher.ViewModels.ThemeViewModel
import com.example.shabasher.ui.theme.ShabasherTheme

@Composable
fun EventCreatedPage(
    //navController: NavController,
    //themeViewModel: ThemeViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                )

        ) {
            Text(
                text = "Событие создано!"
            )
            Image(
                painter = painterResource(R.drawable.cat),
                contentDescription = "QR код"
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Text(
                    text = "shabasher.app/join/ABC123"
                )
                IconButton(
                    onClick = { },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.content_copy),
                        contentDescription = "Копировать"
                    )
                }
            }
        }
        ShabasherSecondaryButton(
            text = "Продолжить",
            onClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventCreatedPreview() {
    ShabasherTheme { EventCreatedPage() }
}