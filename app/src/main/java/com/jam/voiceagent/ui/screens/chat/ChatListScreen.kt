package com.jam.voiceagent.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jam.voiceagent.data.model.ConversationSummary
import com.jam.voiceagent.data.repository.ConversationRepository
import com.jam.voiceagent.ui.components.TopRightQuickMenu
import com.jam.voiceagent.ui.util.formatServerTimestampToTaipei
import kotlin.math.roundToInt

@Composable
fun ChatListScreen(
    isLoggedIn: Boolean,
    conversationRepository: ConversationRepository,
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit,
    onUserClick: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onRequireLogin: () -> Unit,
    onAuthSwitchedToGuest: () -> Unit
) {
    val listState = rememberLazyListState()
    val conversations = remember { mutableStateListOf<ConversationSummary>() }
    val deletingSessionMap = remember { mutableStateMapOf<String, Boolean>() }
    var expandedSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasLoaded by remember { mutableStateOf(false) }

    suspend fun loadConversations() {
        if (!isLoggedIn) {
            conversations.clear()
            errorMessage = "登入後可以查看歷史對話。"
            hasLoaded = true
            return
        }
        isLoading = true
        errorMessage = null
        val result = conversationRepository.getConversations()
        if (result.switchedToGuest) {
            onAuthSwitchedToGuest()
        }
        if (result.requiresLogin) {
            conversations.clear()
            errorMessage = result.errorMessage ?: "請先登入。"
            isLoading = false
            hasLoaded = true
            onRequireLogin()
            return
        }
        if (result.isSuccess) {
            conversations.clear()
            conversations.addAll(result.conversations)
            deletingSessionMap.keys.toList().forEach { key ->
                if (conversations.none { it.sessionId == key }) {
                    deletingSessionMap.remove(key)
                }
            }
            if (expandedSessionId != null && conversations.none { it.sessionId == expandedSessionId }) {
                expandedSessionId = null
            }
        } else {
            errorMessage = result.errorMessage ?: "載入歷史對話失敗，請稍後再試。"
        }
        isLoading = false
        hasLoaded = true
    }

    LaunchedEffect(isLoggedIn) {
        hasLoaded = false
        loadConversations()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        TopRightQuickMenu(
            isLoggedIn = isLoggedIn,
            enabled = !isLoading,
            onHomeClick = onHomeClick,
            onChatClick = onChatClick,
            onUserClick = onUserClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 78.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "歷史對話",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            when {
                !isLoggedIn -> {
                    HintMessage("登入後可以查看歷史對話")
                }

                isLoading && !hasLoaded -> {
                    LoadingState()
                }

                errorMessage != null -> {
                    ErrorState(
                        message = errorMessage ?: "載入失敗",
                        onRetry = { if (!isLoading) {
                            hasLoaded = false
                        } }
                    )
                    LaunchedEffect(hasLoaded) {
                        if (!hasLoaded && !isLoading) {
                            loadConversations()
                        }
                    }
                }

                conversations.isEmpty() -> {
                    HintMessage("目前還沒有歷史對話")
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            items = conversations,
                            key = { it.sessionId }
                        ) { item ->
                            ConversationRow(
                                item = item,
                                isExpanded = expandedSessionId == item.sessionId,
                                isDeleting = deletingSessionMap[item.sessionId] == true,
                                onExpanded = { expandedSessionId = item.sessionId },
                                onCollapsed = {
                                    if (expandedSessionId == item.sessionId) {
                                        expandedSessionId = null
                                    }
                                },
                                onOpenDetail = { onOpenDetail(item.sessionId) },
                                onDelete = {
                                    if (deletingSessionMap[item.sessionId] == true) return@ConversationRow
                                    deletingSessionMap[item.sessionId] = true
                                    errorMessage = null
                                }
                            )

                            if (deletingSessionMap[item.sessionId] == true) {
                                LaunchedEffect(item.sessionId) {
                                    val result = conversationRepository.deleteConversation(item.sessionId)
                                    deletingSessionMap[item.sessionId] = false
                                    if (result.switchedToGuest) {
                                        onAuthSwitchedToGuest()
                                    }
                                    if (result.requiresLogin) {
                                        errorMessage = result.errorMessage ?: "請先登入。"
                                        onRequireLogin()
                                        return@LaunchedEffect
                                    }
                                    if (result.isSuccess) {
                                        conversations.removeAll { it.sessionId == item.sessionId }
                                        deletingSessionMap.remove(item.sessionId)
                                        if (expandedSessionId == item.sessionId) {
                                            expandedSessionId = null
                                        }
                                    } else {
                                        errorMessage = result.errorMessage ?: "刪除失敗，請稍後再試。"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun HintMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = "重試",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onRetry)
        )
    }
}

@Composable
private fun ConversationRow(
    item: ConversationSummary,
    isExpanded: Boolean,
    isDeleting: Boolean,
    onExpanded: () -> Unit,
    onCollapsed: () -> Unit,
    onOpenDetail: () -> Unit,
    onDelete: () -> Unit
) {
    val revealWidth = 98.dp
    val revealWidthPx = with(LocalDensity.current) { revealWidth.toPx() }
    var offsetX by remember(item.sessionId) { mutableFloatStateOf(0f) }
    val thresholdRatio = 0.42f
    val title = item.title?.takeIf { it.isNotBlank() } ?: "未命名對話"
    val timeText = formatServerTimestampToTaipei(
        rawTimestamp = item.updatedAt?.takeIf { it.isNotBlank() }
            ?: item.createdAt?.takeIf { it.isNotBlank() },
        fallback = "時間未知"
    )

    LaunchedEffect(isExpanded, revealWidthPx, isDeleting) {
        offsetX = if (isExpanded && !isDeleting) -revealWidthPx else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (offsetX < -0.5f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(revealWidth)
                    .height(76.dp)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable(enabled = !isDeleting, onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isDeleting) "刪除中…" else "刪除",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(isExpanded, revealWidthPx, isDeleting) {
                    if (!isDeleting) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val maxLeft = -revealWidthPx
                                offsetX = (offsetX + dragAmount).coerceIn(maxLeft, 0f)
                            },
                            onDragEnd = {
                                if (revealWidthPx <= 0f) return@detectHorizontalDragGestures
                                val shouldExpand = offsetX <= -(revealWidthPx * thresholdRatio)
                                if (shouldExpand) onExpanded() else onCollapsed()
                            }
                        )
                    }
                }
                .clickable(enabled = !isDeleting) {
                    if (isExpanded) {
                        onCollapsed()
                    } else {
                        onOpenDetail()
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
