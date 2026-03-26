package com.example.shabasher.Screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shabasher.Model.SafeNavigation
import com.example.shabasher.R
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shabasher.Model.Suggestion
import com.example.shabasher.ViewModels.SuggestionsViewModel
import com.example.shabasher.data.network.SuggestionsRepository
import com.example.shabasher.utils.DateTimeUtils


// ========== ЭКРАН ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    navController: NavController,
    viewModel: SuggestionsViewModel,
    eventId: String,
    isEventOrganizer: Boolean = false
) {
    val suggestions by viewModel.suggestions.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val currentUserId = viewModel.getCurrentUserId()

    var inputText by remember { mutableStateOf("") }
    var suggestionToDelete by remember { mutableStateOf<String?>(null) }

    // ✅ Диалог подтверждения удаления
    suggestionToDelete?.let { suggestionId ->
        AlertDialog(
            onDismissRequest = { suggestionToDelete = null },
            title = {
                Text(
                    "Удалить предложение?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Это действие нельзя отменить. Вы уверены?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSuggestion(suggestionId)
                        suggestionToDelete = null
                    }
                ) {
                    Text(
                        "Удалить",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { suggestionToDelete = null }) {
                    Text(
                        "Отмена",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                )
            }
        )
    }

    // ✅ Показ ошибок через Toast
    error?.let { msg ->
        LaunchedEffect(msg) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // ✅ Загрузка данных
    LaunchedEffect(eventId) {
        viewModel.load(eventId)
    }

    // 🎨 Scaffold с TopAppBar и BottomBar для инпута
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Идеи",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // 🔧 Можно добавить действия для организатора
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundColor,
                    titleContentColor = TextPrimaryColor,
                    navigationIconContentColor = TextPrimaryColor,
                    actionIconContentColor = TextSecondaryColor
                )
            )
        },
        // ✨ ПОЛЕ ВВОДА В BOTTOM BAR — клавиатура обрабатывается автоматически!
        bottomBar = {
            SuggestionInputArea(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.trim().isNotEmpty()) {
                        viewModel.create(eventId, inputText) {
                            inputText = ""
                        }
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .imePadding().navigationBarsPadding()
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                // ❌ Убрали .background() — фон уже задан в Scaffold.containerColor
                // ❌ Убрали .imePadding() — теперь это задача bottomBar
                .padding(paddingValues)
        ) {
            // 📋 Список предложений / загрузка / пустое состояние
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth() // 👈 height управляется через weight
            ) {
                if (loading && suggestions.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (suggestions.isEmpty()) {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                } else {
                    val listState = rememberLazyListState()

                    LaunchedEffect(suggestions.size) {
                        if (suggestions.size > 1) {
                            listState.animateScrollToItem(0)
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(suggestions, key = { it.id }) { suggestion ->
                            if (suggestion.id.startsWith("temp_") && error != null) return@items

                            SuggestionCard(
                                suggestion = suggestion,
                                onVote = viewModel::vote,
                                onUserClick = { /* TODO: навигация на профиль */ },
                                onDelete = { suggestionToDelete = it },
                                currentUserId = currentUserId,
                                isEventOrganizer = isEventOrganizer,
                                modifier = Modifier.animateItem( // 👈 Анимация удаления/добавления
                                    fadeInSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
                                    fadeOutSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========== КАРТОЧКА ПРЕДЛОЖЕНИЯ ==========
@Composable
fun SuggestionCard(
    suggestion: Suggestion,
    onVote: (String, String) -> Unit,
    onUserClick: (String) -> Unit,
    onDelete: (String) -> Unit, // ← Новый параметр
    modifier: Modifier = Modifier,
    currentUserId: String?,
    isEventOrganizer: Boolean = false // ← Новый параметр
) {
    // Проверяем, можно ли показать кнопку удаления
    val canDelete = currentUserId != null &&
            (suggestion.userId == currentUserId || isEventOrganizer)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* Handle card click */ },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // 👤 Шапка карточки: аватар + имя + кнопка удаления
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Аватар
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = buildString {
                                if (suggestion.userId == currentUserId) {
                                    append("Вы")
                                } else {
                                    append(suggestion.userName)
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val relativeTime = rememberRelativeTime(suggestion.timestamp)
                        Text(
                            text = relativeTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryColor
                        )
                    }
                }

                // 🗑️ Кнопка удаления (показывается только автору или организатору)
                if (canDelete) {
                    IconButton(
                        onClick = { onDelete(suggestion.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline, // или Delete
                            contentDescription = "Удалить предложение",
                            tint = TextSecondaryColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Текст предложения
            Text(
                text = suggestion.text,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimaryColor,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

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
fun rememberRelativeTime(timestamp: String): String {
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(timestamp) {
        while (true) {
            // Быстрее обновляем свежие предложения, реже — старые
            val ageMinutes = java.time.temporal.ChronoUnit.MINUTES.between(
                DateTimeUtils.parseIsoTimestamp(timestamp),
                java.time.Instant.now()
            )
            val interval = when {
                ageMinutes < 60 -> 30_000L      // Каждые 30 сек для < 1 часа
                ageMinutes < 1440 -> 300_000L    // Каждые 5 мин для < 24 часов
                else -> 1_800_000L               // Каждые 30 мин для старых
            }
            delay(interval)
            tick++
        }
    }

    return remember(timestamp, tick) {
        DateTimeUtils.formatRelativeTime(timestamp)
    }
}

// ========== КНОПКА ГОЛОСОВАНИЯ ==========
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
        shape = RoundedCornerShape(22.dp),
        color = buttonColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .height(36.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

// ========== ПОЛЕ ВВОДА ==========
@Composable
fun SuggestionInputArea(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInputNotEmpty = text.trim().isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
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
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (isInputNotEmpty) onSend() }
                ),
                maxLines = 3,
                decorationBox = { innerTextField ->
                    Box {
                        if (text.isEmpty()) {
                            Text(
                                text = "Поделитесь идеей...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        // FloatingActionButton без параметра enabled
        FloatingActionButton(
            onClick = { if (isInputNotEmpty) onSend() },
            modifier = Modifier.size(48.dp),
            // Меняем цвета в зависимости от состояния
            containerColor = if (isInputNotEmpty) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isInputNotEmpty) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Отправить",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ========== ПУСТОЕ СОСТОЯНИЕ ==========
@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()  // 👈 Обязательно: занимаем всё пространство
            .padding(16.dp), // 👈 Отступы по краям для красоты
        horizontalAlignment = Alignment.CenterHorizontally,  // 👈 Центр по горизонтали
        verticalArrangement = Arrangement.Center  // 👈 Центр по вертикали
    ) {
        Text(
            text = "Пока нет идей",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondaryColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Изображение манула
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = com.example.shabasher.R.drawable.manulsuggestions),
                contentDescription = "Манул ждёт идей",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                alpha = 0.9f
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Будьте первым, кто\nоставит предложение ✨",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp) // 👈 Чтобы текст не прилипал к краям на узких экранах
        )
    }
}

// ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========
fun formatTimeAgo(timestamp: String): String {
    return timestamp.takeIf { it.isNotBlank() } ?: "только что"
}

// ========== ЦВЕТОВЫЕ КОНСТАНТЫ ==========
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