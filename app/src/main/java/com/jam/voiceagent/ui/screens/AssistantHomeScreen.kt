package com.jam.voiceagent.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jam.voiceagent.ui.avatar.AvatarFace
import com.jam.voiceagent.ui.avatar.AvatarState
import com.jam.voiceagent.ui.components.ChatInputBar
import com.jam.voiceagent.ui.components.EmotionButtons
import com.jam.voiceagent.ui.voice.ListeningIndicator
import kotlinx.coroutines.delay

@Composable
fun AssistantHomeScreen() {
    var state by rememberSaveable { mutableStateOf(AvatarState.Idle) }
    var isTextInputMode by rememberSaveable { mutableStateOf(false) }
    var isMicPressed by rememberSaveable { mutableStateOf(false) }
    var showSettingsQuickMenu by rememberSaveable { mutableStateOf(false) }
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var showDebugPanel by rememberSaveable { mutableStateOf(false) }
    var lastInteractionMs by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val states = rememberAvatarStates()
    val resetIdleTimer = { lastInteractionMs = System.currentTimeMillis() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            nowMs = System.currentTimeMillis()
        }
    }

    val idleElapsedMs = if (state == AvatarState.Idle) nowMs - lastInteractionMs else 0L
    val sleepTarget = if (idleElapsedMs > 60_000L) {
        ((idleElapsedMs - 60_000L) / 20_000f).coerceIn(0f, 1f)
    } else {
        0f
    }
    val idleSleepiness by animateFloatAsState(
        targetValue = if (state == AvatarState.Idle) sleepTarget else 0f,
        animationSpec = tween(durationMillis = 1800),
        label = "idle-sleepiness"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            TopRightQuickMenu(
                showMenu = showSettingsQuickMenu,
                isLoggedIn = isLoggedIn,
                onToggleMenu = {
                    resetIdleTimer()
                    showSettingsQuickMenu = !showSettingsQuickMenu
                },
                onHomeClick = {
                    resetIdleTimer()
                    showSettingsQuickMenu = false
                },
                onAccountClick = {
                    resetIdleTimer()
                    isLoggedIn = !isLoggedIn
                    showSettingsQuickMenu = false
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 12.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 62.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .alpha(0.8f)
                        .padding(bottom = 24.dp),
                    fontWeight = FontWeight.Medium
                )

                AvatarFace(
                    state = state,
                    headColor = MaterialTheme.colorScheme.primaryContainer,
                    featureColor = MaterialTheme.colorScheme.primary,
                    glowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    sleepiness = idleSleepiness
                )

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (state == AvatarState.Speaking) {
                        ListeningIndicator(
                            isActive = true,
                            color = MaterialTheme.colorScheme.primary,
                            softColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .width(102.dp)
                                .height(22.dp)
                        )
                    }
                }
            }

            if (isTextInputMode) {
                ChatInputBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 106.dp),
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            BottomInputControls(
                isTextInputMode = isTextInputMode,
                isMicPressed = isMicPressed,
                onMicPressState = { pressed ->
                    if (!isTextInputMode) {
                        resetIdleTimer()
                        isMicPressed = pressed
                        state = if (pressed) AvatarState.Listening else AvatarState.Idle
                    }
                },
                onSwitchToTextMode = {
                    resetIdleTimer()
                    isTextInputMode = true
                    isMicPressed = false
                    if (state == AvatarState.Listening) state = AvatarState.Idle
                },
                onSwitchToVoiceMode = {
                    resetIdleTimer()
                    isTextInputMode = false
                    isMicPressed = false
                    state = AvatarState.Idle
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp)
                    .padding(bottom = if (isTextInputMode) 170.dp else 128.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.78f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Debug",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.alpha(0.6f)
                        )
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "切換 debug 表情",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    resetIdleTimer()
                                    showDebugPanel = !showDebugPanel
                                }
                                .alpha(0.72f)
                        )
                    }
                    AnimatedVisibility(visible = showDebugPanel) {
                        EmotionButtons(
                            selected = state,
                            states = states,
                            onSelect = {
                                resetIdleTimer()
                                state = it
                            },
                            selectedContainer = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabel = MaterialTheme.colorScheme.primary,
                            container = MaterialTheme.colorScheme.surface,
                            label = MaterialTheme.colorScheme.onSurfaceVariant,
                            compact = true,
                            modifier = Modifier.width(220.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopRightQuickMenu(
    showMenu: Boolean,
    isLoggedIn: Boolean,
    onToggleMenu: () -> Unit,
    onHomeClick: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn(tween(150)) + expandHorizontally(
                animationSpec = tween(180),
                expandFrom = Alignment.End
            ),
            exit = fadeOut(tween(100)) + shrinkHorizontally(
                animationSpec = tween(140),
                shrinkTowards = Alignment.End
            )
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.9f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Home 快捷",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onHomeClick)
                    )
                    Icon(
                        imageVector = if (isLoggedIn) Icons.AutoMirrored.Filled.Logout else Icons.Filled.Person,
                        contentDescription = if (isLoggedIn) "登出快捷" else "登入快捷",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onAccountClick)
                    )
                }
            }
        }

        IconButton(onClick = onToggleMenu) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "設定快捷選單",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomInputControls(
    isTextInputMode: Boolean,
    isMicPressed: Boolean,
    onMicPressState: (Boolean) -> Unit,
    onSwitchToTextMode: () -> Unit,
    onSwitchToVoiceMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "record-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(820),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic-pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 14.dp)
    ) {
        if (isTextInputMode) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .size(66.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.NorthEast,
                        contentDescription = "文字模式主操作",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            IconButton(
                onClick = onSwitchToVoiceMode,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 8.dp)
                    .size(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "切回語音模式",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isMicPressed) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(pulse)
                            .alpha(0.46f)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.26f), CircleShape)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onMicPressState(true)
                                    try {
                                        tryAwaitRelease()
                                    } finally {
                                        onMicPressState(false)
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "語音模式主麥克風",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            IconButton(
                onClick = onSwitchToTextMode,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 8.dp)
                    .size(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Keyboard,
                        contentDescription = "切換文字模式",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberAvatarStates(): List<AvatarState> = listOf(
    AvatarState.Idle,
    AvatarState.Listening,
    AvatarState.Thinking,
    AvatarState.Speaking,
    AvatarState.Happy,
    AvatarState.Sad,
    AvatarState.Confused,
    AvatarState.Surprised,
    AvatarState.Helpless
)
