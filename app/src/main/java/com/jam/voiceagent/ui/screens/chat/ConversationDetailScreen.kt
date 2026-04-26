package com.jam.voiceagent.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jam.voiceagent.data.model.ConversationDetail
import com.jam.voiceagent.data.repository.ConversationRepository
import com.jam.voiceagent.ui.components.TopRightQuickMenu
import com.jam.voiceagent.ui.util.formatServerTimestampToTaipei

@Composable
fun ConversationDetailScreen(
    isLoggedIn: Boolean,
    sessionId: String,
    conversationRepository: ConversationRepository,
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit,
    onUserClick: () -> Unit,
    onConfirmUseSession: (String) -> Unit,
    onRequireLogin: () -> Unit,
    onAuthSwitchedToGuest: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var detail by remember { mutableStateOf<ConversationDetail?>(null) }
    var reloadNonce by remember { mutableIntStateOf(0) }

    suspend fun loadDetail() {
        if (!isLoggedIn) {
            errorMessage = "登入後可以查看歷史對話。"
            isLoading = false
            onRequireLogin()
            return
        }
        isLoading = true
        errorMessage = null
        val result = conversationRepository.getConversationDetail(sessionId)
        if (result.switchedToGuest) {
            onAuthSwitchedToGuest()
        }
        if (result.requiresLogin) {
            errorMessage = result.errorMessage ?: "請先登入。"
            isLoading = false
            onRequireLogin()
            return
        }
        if (result.isSuccess) {
            detail = result.detail
        } else {
            errorMessage = result.errorMessage ?: "載入對話內容失敗，請稍後再試。"
        }
        isLoading = false
    }

    LaunchedEffect(sessionId, isLoggedIn, reloadNonce) {
        loadDetail()
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
                .padding(top = 78.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = detail?.title?.takeIf { it.isNotBlank() } ?: "歷史對話",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "載入失敗",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            isLoading = true
                            reloadNonce += 1
                        }) {
                            Text("重試")
                        }
                    }
                }

                detail == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "目前沒有可顯示的對話內容",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(detail?.messages.orEmpty()) { message ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (message.role == "assistant") "助理" else "你",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = message.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = formatServerTimestampToTaipei(
                                            rawTimestamp = message.createdAt,
                                            fallback = "時間未知"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val targetSessionId = detail?.sessionId
                            if (!targetSessionId.isNullOrBlank()) {
                                onConfirmUseSession(targetSessionId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !detail?.sessionId.isNullOrBlank()
                    ) {
                        Text("確定使用這段對話")
                    }
                }
            }
        }
    }
}
