package com.jam.voiceagent.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jam.voiceagent.ui.components.TopRightQuickMenu
import kotlin.math.roundToInt

@Composable
fun ChatPlaceholderScreen(
    isLoggedIn: Boolean,
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val fakeHistory = remember {
        mutableStateListOf(
            FakeChatItem(1, "今天下午三點提醒我開會，並在會前十分鐘再提醒一次。"),
            FakeChatItem(2, "剛剛那段英文幫我翻成自然一點的繁體中文口語說法。"),
            FakeChatItem(3, "幫我整理今天的待辦清單，先列出最重要的三件事情。"),
            FakeChatItem(4, "明天早上如果下雨，請提醒我帶傘和筆電防水套。"),
            FakeChatItem(5, "我想聽輕鬆一點的回覆語氣，回答不要太正式太生硬。"),
            FakeChatItem(6, "把剛剛的回答濃縮成一句話，方便我直接貼給同事。"),
            FakeChatItem(7, "請用條列式整理上週會議重點，控制在五點以內。"),
            FakeChatItem(8, "今天晚上九點前提醒我去超商領包裹，別忘記身分證。"),
            FakeChatItem(9, "把剛剛那段說明改成給國中生也看得懂的版本，字數控制在一百字內。"),
            FakeChatItem(10, "幫我把這封信調成比較有禮貌但不失效率的語氣，我要寄給合作夥伴。"),
            FakeChatItem(11, "請幫我想三種不同開場白，明天晨會我要報告新專案進度。"),
            FakeChatItem(12, "把今天的行程整理成早上、下午、晚上三段，並加上優先順序。"),
            FakeChatItem(13, "如果週末天氣變冷，提醒我帶外套，並且前一天晚上再提醒一次。"),
            FakeChatItem(14, "把這段口語內容轉成簡短會議紀錄格式，先列重點再列待辦。"),
            FakeChatItem(15, "我等等要面試，請幫我快速複習三個自我介紹版本。"),
            FakeChatItem(16, "幫我想五個 IG 貼文標題，主題是春天踏青與城市散步。"),
            FakeChatItem(17, "剛剛那個回覆太長了，請縮短成可以放在通知欄的一句話。"),
            FakeChatItem(18, "幫我把產品介紹改得更像真人對話，不要太像官方文宣。"),
            FakeChatItem(19, "明天早上七點叫我起床，另外七點十分再提醒我準備出門。"),
            FakeChatItem(20, "請把今天討論的功能點整理成開發 ticket 清單，我要貼到看板。"),
            FakeChatItem(21, "幫我把回覆改成比較溫柔、陪伴感更強一點的語氣。"),
            FakeChatItem(22, "把這句話翻成日文，再附上一個自然口語版和一個正式版。")
        )
    }
    var expandedItemId by rememberSaveable { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        TopRightQuickMenu(
            isLoggedIn = isLoggedIn,
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
                text = "歷史聊天（假資料）",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "本頁僅用於 NAV-01.2 列表與刪除互動原型，尚未串接 API。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = fakeHistory,
                    key = { it.id }
                ) { item ->
                    ChatHistoryRow(
                        id = item.id,
                        text = item.text,
                        isExpanded = expandedItemId == item.id,
                        onExpanded = { expandedItemId = item.id },
                        onCollapsed = {
                            if (expandedItemId == item.id) {
                                expandedItemId = null
                            }
                        },
                        onDelete = {
                            fakeHistory.remove(item)
                            if (expandedItemId == item.id) {
                                expandedItemId = null
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatHistoryRow(
    id: Int,
    text: String,
    isExpanded: Boolean,
    onExpanded: () -> Unit,
    onCollapsed: () -> Unit,
    onDelete: () -> Unit
) {
    val revealWidth = 98.dp
    val revealWidthPx = with(LocalDensity.current) { revealWidth.toPx() }
    var offsetX by remember(id) { mutableFloatStateOf(0f) }
    val thresholdRatio = 0.42f

    LaunchedEffect(isExpanded, revealWidthPx) {
        offsetX = if (isExpanded) -revealWidthPx else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (offsetX < -0.5f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(revealWidth)
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(42.dp)
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Text(
                        text = "刪除",
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(isExpanded, revealWidthPx) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val maxLeft = -revealWidthPx
                            offsetX = (offsetX + dragAmount).coerceIn(maxLeft, 0f)
                        },
                        onDragEnd = {
                            if (revealWidthPx <= 0f) return@detectHorizontalDragGestures
                            val shouldExpand = offsetX <= -(revealWidthPx * thresholdRatio)
                            if (shouldExpand) {
                                onExpanded()
                            } else {
                                onCollapsed()
                            }
                        }
                    )
                }
                .clickable {
                    if (isExpanded) {
                        onCollapsed()
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
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class FakeChatItem(
    val id: Int,
    val text: String
)
