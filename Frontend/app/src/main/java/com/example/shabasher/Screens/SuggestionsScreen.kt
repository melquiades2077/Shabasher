package com.example.shabasher.Screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shabasher.Model.SafeNavigation
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Модели данных (без изменений)
data class Suggestion(
    val id: Int,
    val userId: String,
    val userName: String,
    val avatar: String,
    val text: String,
    var likes: Int,
    var dislikes: Int,
    val timestamp: Date,
    var liked: Boolean = false,
    var disliked: Boolean = false
)

data class User(
    val id: String,
    val name: String,
    val avatar: String
)

// UI компоненты
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    navController: NavController,
    onNavigateToProfile: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser = remember { User("57dd70f6-bca8-442c-855d-9488d4b59371", "pakapaka", "ПК") }
    var suggestions by remember { mutableStateOf<List<Suggestion>>(getSampleSuggestions()) }
    var newSuggestionText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Ключевое добавление: состояние для скролла LazyColumn
    val listState = rememberLazyListState()

    // Эффект для скролла вверх при изменении списка предложений
    LaunchedEffect(suggestions) {
        if (suggestions.isNotEmpty()) {
            // Ждем немного, чтобы композиция обновилась
            kotlinx.coroutines.delay(100)
            // Скроллим к первому элементу
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Предложения") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SafeNavigation.navigate { navController.popBackStack() }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = 16.dp,
                        end = 16.dp
                    )
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                        focusManager.clearFocus()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding() // ← Весь контент сдвигается при клавиатуре
                ) {
                    // Список сжимается при открытии клавиатуры
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        state = listState, // ← Подключаем состояние скролла
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                        reverseLayout = false // Убедитесь, что reverseLayout = false
                    ) {
                        if (suggestions.isEmpty()) {
                            item {
                                EmptyState(Modifier.fillParentMaxSize())
                            }
                        } else {
                            items(suggestions, key = { it.id }) { suggestion ->
                                SuggestionCard(
                                    suggestion = suggestion,
                                    onVote = { id, action ->
                                        handleVote(suggestions, id, action) { updated ->
                                            suggestions = updated
                                        }
                                    },
                                    onUserClick = onNavigateToProfile,
                                    modifier = Modifier.fillParentMaxWidth()
                                )
                            }
                        }
                    }

                    // Ключевое изменение: добавляем navigationBarsPadding()
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding().padding(bottom = 2.dp), // ← Защита от системных кнопок
                        color = MaterialTheme.colorScheme.background,
                        shadowElevation = 2.dp
                    ) {
                        SuggestionInputArea(
                            text = newSuggestionText,
                            onTextChange = { newSuggestionText = it },
                            onSend = {
                                if (newSuggestionText.trim().isNotEmpty()) {
                                    val newSuggestion = createNewSuggestion(currentUser, newSuggestionText.trim())
                                    suggestions = listOf(newSuggestion) + suggestions
                                    newSuggestionText = ""
                                    focusManager.clearFocus()
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}

// Вспомогательный модификатор — устанавливает минимальную высоту = высоте клавиатуры
@Composable
fun Modifier.windowInsetsBottomHeight(windowInsets: WindowInsets): Modifier {
    val density = LocalDensity.current
    val bottom = with(density) { windowInsets.getBottom(density).toDp() }
    return this.then(
        Modifier.heightIn(min = bottom.coerceAtLeast(56.dp)) // 56.dp = высота поля ввода
    )
}

@Composable
fun SuggestionCard(
    suggestion: Suggestion,
    onVote: (Int, String) -> Unit,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* Handle card click */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Информация о пользователе
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(
                    avatarText = suggestion.avatar,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = suggestion.userName + if (suggestion.userId == "57dd70f6-bca8-442c-855d-9488d4b59371") " (вы)" else "",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = formatTimeAgo(suggestion.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Текст предложения
            Text(
                text = suggestion.text,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimaryColor,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Кнопки голосования
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VoteButton(
                    icon = "👍",
                    count = suggestion.likes,
                    isActive = suggestion.liked,
                    onClick = { onVote(suggestion.id, "like") }
                )

                VoteButton(
                    icon = "👎",
                    count = suggestion.dislikes,
                    isActive = suggestion.disliked,
                    onClick = { onVote(suggestion.id, "dislike") }
                )
            }
        }
    }
}


@Composable
fun Avatar(
    avatarText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(AvatarGradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = avatarText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun VoteButton(
    icon: String,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (buttonColor, textColor) = when {
        isActive && icon == "👍" -> SuccessButtonColor to SuccessTextColor
        isActive && icon == "👎" -> ErrorButtonColor to ErrorTextColor
        else -> VoteButtonColor to TextSecondaryColor
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = buttonColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .height(36.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun SuggestionInputArea(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Добавляем флаг для отслеживания фокуса
    val focusState = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            // Добавляем минимальную высоту для надежности
            .heightIn(min = 56.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .onFocusChanged { focusState.value = it.isFocused },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (text.trim().isNotEmpty()) onSend() }
                ),
                maxLines = 3,
                decorationBox = { innerTextField ->
                    Box {
                        if (text.isEmpty()) {
                            Text(
                                text = "Напишите предложение...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        FloatingActionButton(
            onClick = { if (text.trim().isNotEmpty()) onSend() },
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Отправить",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Пока нет предложений",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondaryColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Будьте первым, кто поделится идеей",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

// Вспомогательные функции (без изменений)
fun getSampleSuggestions(): List<Suggestion> {
    val now = Date()
    return listOf(
    )
}
fun handleVote(
    suggestions: List<Suggestion>,
    suggestionId: Int,
    action: String,
    onUpdate: (List<Suggestion>) -> Unit
) {
    val updatedSuggestions = suggestions.map { suggestion ->
        if (suggestion.id == suggestionId) {
            when (action) {
                "like" -> {
                    if (suggestion.liked) {
                        suggestion.copy(
                            likes = suggestion.likes - 1,
                            liked = false
                        )
                    } else {
                        suggestion.copy(
                            likes = suggestion.likes + 1,
                            liked = true,
                            disliked = false,
                            dislikes = if (suggestion.disliked) suggestion.dislikes - 1 else suggestion.dislikes
                        )
                    }
                }
                "dislike" -> {
                    if (suggestion.disliked) {
                        suggestion.copy(
                            dislikes = suggestion.dislikes - 1,
                            disliked = false
                        )
                    } else {
                        suggestion.copy(
                            dislikes = suggestion.dislikes + 1,
                            disliked = true,
                            liked = false,
                            likes = if (suggestion.liked) suggestion.likes - 1 else suggestion.likes
                        )
                    }
                }
                else -> suggestion
            }
        } else {
            suggestion
        }
    }

    onUpdate(updatedSuggestions)
}

fun createNewSuggestion(currentUser: User, text: String): Suggestion {
    return Suggestion(
        id = (1..1000).random(),
        userId = currentUser.id,
        userName = currentUser.name,
        avatar = currentUser.avatar,
        text = text,
        likes = 0,
        dislikes = 0,
        timestamp = Date(),
        liked = false,
        disliked = false
    )
}

fun formatTimeAgo(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    val minutes = (diff / 60000).toInt()
    val hours = (diff / 3600000).toInt()
    val days = (diff / 86400000).toInt()

    return when {
        minutes < 1 -> "только что"
        minutes < 60 -> "$minutes мин назад"
        hours < 24 -> "$hours ч назад"
        else -> "$days дн. назад"
    }
}


// Цветовые константы (без изменений)
val BackgroundColor = Color(0xFF121212)
val CardBackgroundColor = Color(0xFF1E1E1E)
val TextPrimaryColor = Color(0xFFFFFFFF)
val TextSecondaryColor = Color(0xFFB3B3B3)
val AvatarGradientBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFBB86FC), Color(0xFF03DAC6))
)
val VoteButtonColor = Color(0xFF2D2D2D)
val SuccessButtonColor = Color(0x1A4CAF50)
val ErrorButtonColor = Color(0x1AF44336)
val SuccessTextColor = Color(0xFF4CAF50)
val ErrorTextColor = Color(0xFFF44336)