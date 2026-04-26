package com.jam.voiceagent.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jam.voiceagent.ui.avatar.AvatarFace
import com.jam.voiceagent.ui.avatar.AvatarState
import com.jam.voiceagent.ui.avatar.interaction.ShakeInteractionState
import com.jam.voiceagent.ui.avatar.interaction.ShakeSensorController
import com.jam.voiceagent.ui.avatar.interaction.rememberTouchAffectionHandler
import com.jam.voiceagent.data.repository.ChatRepository
import com.jam.voiceagent.data.audio.VoicePlayer
import com.jam.voiceagent.data.audio.VoiceRecorder
import com.jam.voiceagent.data.repository.VoiceRepository
import com.jam.voiceagent.ui.components.ChatInputBar
import com.jam.voiceagent.ui.components.EmotionButtons
import com.jam.voiceagent.ui.components.TopRightQuickMenu
import com.jam.voiceagent.ui.voice.ListeningIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.random.Random

@Composable
fun AssistantHomeScreen(
    isLoggedIn: Boolean,
    onNavigateHome: () -> Unit,
    onNavigateChat: () -> Unit,
    onUserAction: () -> Unit,
    onStartNewConversation: () -> Unit,
    chatRepository: ChatRepository,
    voiceRepository: VoiceRepository,
    isChatBusy: Boolean,
    latestAssistantReply: String,
    startupErrorMessage: String,
    onChatBusyChange: (Boolean) -> Unit,
    onAssistantReplyChange: (String) -> Unit,
    onAuthSwitchedToGuest: () -> Unit,
    onStartupErrorConsumed: () -> Unit
) {
    var state by rememberSaveable { mutableStateOf(AvatarState.Idle) }
    var isTextInputMode by rememberSaveable { mutableStateOf(false) }
    var isMicPressed by rememberSaveable { mutableStateOf(false) }
    var showDebugPanel by rememberSaveable { mutableStateOf(false) }
    var inputText by rememberSaveable { mutableStateOf("") }
    var lastInteractionMs by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var voiceUiPhase by rememberSaveable { mutableStateOf(VoiceUiPhase.Idle) }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val touchAffectionHandler = rememberTouchAffectionHandler()
    val scope = rememberCoroutineScope()
    val voiceRecorder = remember(context) { VoiceRecorder(context.cacheDir) }
    val voicePlayer = remember { VoicePlayer() }
    var activeRecordedFile by remember { mutableStateOf<java.io.File?>(null) }
    var activeReplyAudioFile by remember { mutableStateOf<java.io.File?>(null) }

    fun showVoiceError(message: String) {
        onAssistantReplyChange(message)
        state = AvatarState.Confused
        scope.launch {
            delay(900)
            if (state == AvatarState.Confused) {
                state = AvatarState.Idle
            }
        }
    }

    fun startVoiceRecordingFlow() {
        if (isChatBusy || isTextInputMode) return
        lastInteractionMs = System.currentTimeMillis()
        onChatBusyChange(true)
        val startResult = voiceRecorder.startRecording()
        if (startResult.isFailure) {
            onChatBusyChange(false)
            isMicPressed = false
            voiceUiPhase = VoiceUiPhase.Idle
            showVoiceError("錄音時發生問題，請再試一次。")
            return
        }
        activeRecordedFile = startResult.getOrNull()
        isMicPressed = true
        voiceUiPhase = VoiceUiPhase.Recording
        state = AvatarState.Listening
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startVoiceRecordingFlow()
        } else {
            isMicPressed = false
            showVoiceError("需要麥克風權限才能使用語音對話。")
        }
    }

    var shakeState by remember {
        mutableStateOf(
            ShakeInteractionState(
                sensorAvailable = true,
                tiltX = 0f,
                tiltY = 0f,
                shakeStrength = 0f,
                isStrongShake = false
            )
        )
    }

    val sensorController = remember {
        ShakeSensorController(context) { latest ->
            shakeState = latest
        }
    }

    DisposableEffect(lifecycleOwner, sensorController) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                sensorController.start()
            }

            override fun onStop(owner: LifecycleOwner) {
                sensorController.stop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sensorController.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceRecorder.cancelAndDeleteCurrent()
            voicePlayer.stopAndRelease()
            voiceRepository.deleteTempAudio(activeRecordedFile)
            voiceRepository.deleteTempAudio(activeReplyAudioFile)
        }
    }

    val states = rememberAvatarStates()
    val resetIdleTimer = { lastInteractionMs = System.currentTimeMillis() }

    var inStrongShakeSession by remember { mutableStateOf(false) }
    var bounceStartedInSession by remember { mutableStateOf(false) }
    var lastStrongShakeSeenMs by remember { mutableLongStateOf(0L) }
    var shakeCooldownUntilMs by remember { mutableLongStateOf(0L) }
    var shakeCooldownRemainingMs by remember { mutableLongStateOf(0L) }
    var dizzyNonce by remember { mutableLongStateOf(0L) }
    var isDizzy by remember { mutableStateOf(false) }
    val bounceOffsetX = remember { Animatable(0f) }
    val bounceOffsetY = remember { Animatable(0f) }
    val shakeEndDebounceMs = 480L
    val shakeCooldownMs = 4_800L

    val bounceRangeX = ((configuration.screenWidthDp - 220f) * 0.48f).coerceIn(42f, 140f)
    val bounceRangeY = ((configuration.screenHeightDp - 420f) * 0.36f).coerceIn(56f, 180f)

    val cooldownActive = shakeCooldownRemainingMs > 0L
    val strongShakeActive = shakeState.isStrongShake && !cooldownActive && !isDizzy

    LaunchedEffect(strongShakeActive) {
        if (strongShakeActive) {
            resetIdleTimer()
            if (!inStrongShakeSession) {
                bounceStartedInSession = false
            }
            inStrongShakeSession = true
            lastStrongShakeSeenMs = System.currentTimeMillis()
            return@LaunchedEffect
        }

        if (!inStrongShakeSession || isDizzy) return@LaunchedEffect

        delay(shakeEndDebounceMs)
        val now = System.currentTimeMillis()
        if (inStrongShakeSession && !strongShakeActive && now - lastStrongShakeSeenMs >= shakeEndDebounceMs) {
            inStrongShakeSession = false
            shakeCooldownUntilMs = now + shakeCooldownMs
            if (bounceStartedInSession) {
                dizzyNonce = now
            } else {
                bounceOffsetX.animateTo(0f, animationSpec = tween(220))
                bounceOffsetY.animateTo(0f, animationSpec = tween(220))
            }
        }
    }

    LaunchedEffect(inStrongShakeSession, strongShakeActive, bounceRangeX, bounceRangeY) {
        if (inStrongShakeSession && strongShakeActive) {
            while (inStrongShakeSession && strongShakeActive) {
                bounceStartedInSession = true
                val targetX = Random.nextFloat() * (bounceRangeX * 2f) - bounceRangeX
                val targetY = Random.nextFloat() * (bounceRangeY * 2f) - bounceRangeY
                bounceOffsetX.animateTo(targetX, animationSpec = tween(105))
                bounceOffsetY.animateTo(targetY, animationSpec = tween(105))
            }
        } else {
            bounceOffsetX.animateTo(0f, animationSpec = tween(240))
            bounceOffsetY.animateTo(0f, animationSpec = tween(240))
        }
    }

    LaunchedEffect(dizzyNonce) {
        if (dizzyNonce <= 0L) return@LaunchedEffect
        isDizzy = true
        state = AvatarState.Idle
        bounceOffsetX.snapTo(0f)
        bounceOffsetY.snapTo(0f)
        delay(5000)
        isDizzy = false
        inStrongShakeSession = false
        bounceStartedInSession = false
        state = AvatarState.Idle
        bounceOffsetX.animateTo(0f, animationSpec = tween(200))
        bounceOffsetY.animateTo(0f, animationSpec = tween(200))
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(250)
            val now = System.currentTimeMillis()
            nowMs = now
            shakeCooldownRemainingMs = (shakeCooldownUntilMs - now).coerceAtLeast(0L)
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
    val isSleeping = state == AvatarState.Idle && idleSleepiness > 0.18f && !isDizzy

    val displayText = when {
        isDizzy -> "暈頭中…"
        isSleeping -> "z.. Z.. z..."
        state == AvatarState.Listening -> AvatarState.Listening.statusText
        state == AvatarState.Thinking -> AvatarState.Thinking.statusText
        state == AvatarState.Speaking -> AvatarState.Speaking.statusText
        startupErrorMessage.isNotBlank() && latestAssistantReply.isBlank() -> startupErrorMessage
        state == AvatarState.Idle && touchAffectionHandler.affectionLevel > 0.75f -> "好舒服呀～"
        latestAssistantReply.isNotBlank() -> latestAssistantReply
        else -> state.statusText
    }

    fun sendTextMessage() {
        if (isChatBusy) return
        val requestText = inputText.trim()
        if (requestText.isBlank()) return

        resetIdleTimer()
        inputText = ""
        onChatBusyChange(true)
        state = AvatarState.Thinking

        scope.launch {
            try {
                val result = chatRepository.sendText(requestText)
                if (result.switchedToGuest) {
                    onAuthSwitchedToGuest()
                    onAssistantReplyChange(result.errorMessage ?: "登入已過期，已切換為訪客模式。")
                    state = AvatarState.Helpless
                    delay(1200)
                    if (state == AvatarState.Helpless) {
                        state = AvatarState.Idle
                    }
                    return@launch
                }
                if (result.isSuccess) {
                    onAssistantReplyChange(result.aiReply.orEmpty())
                    state = AvatarState.Speaking
                    delay(900)
                    if (state == AvatarState.Speaking) {
                        state = AvatarState.Idle
                    }
                } else {
                    onAssistantReplyChange(result.errorMessage ?: "目前連線有點問題，請稍後再試。")
                    state = AvatarState.Confused
                    delay(900)
                    if (state == AvatarState.Confused) {
                        state = AvatarState.Idle
                    }
                }
            } finally {
                onChatBusyChange(false)
            }
        }
    }

    fun stopAndSendVoiceMessage() {
        if (!isMicPressed || voiceUiPhase != VoiceUiPhase.Recording) return

        isMicPressed = false
        voiceUiPhase = VoiceUiPhase.SendingVoice
        state = AvatarState.Thinking

        val stopResult = voiceRecorder.stopRecording()
        val recordedFile = stopResult.getOrNull()
        if (stopResult.isFailure || recordedFile == null) {
            onChatBusyChange(false)
            voiceUiPhase = VoiceUiPhase.Idle
            activeRecordedFile = null
            showVoiceError("錄音時發生問題，請再試一次。")
            return
        }

        activeRecordedFile = recordedFile
        scope.launch {
            try {
                val voiceResult = voiceRepository.sendVoiceRound(recordedFile)
                if (voiceResult.switchedToGuest) {
                    onAuthSwitchedToGuest()
                    onAssistantReplyChange(voiceResult.errorMessage ?: "登入已過期，已切換為訪客模式。")
                    state = AvatarState.Helpless
                    delay(1200)
                    if (state == AvatarState.Helpless) {
                        state = AvatarState.Idle
                    }
                    return@launch
                }
                if (!voiceResult.aiReply.isNullOrBlank()) {
                    onAssistantReplyChange(voiceResult.aiReply)
                }

                if (!voiceResult.isSuccess) {
                    val errorText = voiceResult.errorMessage ?: "目前連線有點問題，請稍後再試。"
                    if (voiceResult.isSttEmptyResult) {
                        onAssistantReplyChange(errorText)
                        state = AvatarState.Surprised
                        delay(900)
                        if (state == AvatarState.Surprised) {
                            state = AvatarState.Idle
                        }
                    } else {
                        showVoiceError(errorText)
                    }
                    return@launch
                }

                val replyFile = voiceResult.replyAudioFile
                activeReplyAudioFile = replyFile

                if (replyFile == null) {
                    state = AvatarState.Idle
                    val noAudioMessage = "目前只有文字回覆，沒有可播放的語音。"
                    val textOnlyReply = voiceResult.aiReply?.takeIf { it.isNotBlank() }
                    onAssistantReplyChange(
                        if (textOnlyReply == null) noAudioMessage else "$textOnlyReply\n$noAudioMessage"
                    )
                    return@launch
                }

                state = AvatarState.Speaking
                voiceUiPhase = VoiceUiPhase.PlayingResponse
                val playback = runCatching { voicePlayer.playAndAwait(replyFile) }
                if (playback.isFailure) {
                    showVoiceError("播放回覆時發生問題，請稍後再試。")
                } else {
                    state = AvatarState.Idle
                }
            } finally {
                voiceRepository.deleteTempAudio(activeRecordedFile)
                voiceRepository.deleteTempAudio(activeReplyAudioFile)
                activeRecordedFile = null
                activeReplyAudioFile = null
                onChatBusyChange(false)
                voiceUiPhase = VoiceUiPhase.Idle
                if (state == AvatarState.Thinking) {
                    state = AvatarState.Idle
                }
            }
        }
    }

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
                isLoggedIn = isLoggedIn,
                enabled = !isChatBusy,
                onHomeClick = {
                    resetIdleTimer()
                    onNavigateHome()
                },
                onChatClick = {
                    resetIdleTimer()
                    onStartupErrorConsumed()
                    onNavigateChat()
                },
                onUserClick = {
                    resetIdleTimer()
                    onStartupErrorConsumed()
                    onUserAction()
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
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .alpha(0.8f)
                        .padding(bottom = 24.dp),
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    resetIdleTimer()
                                    touchAffectionHandler.beginTouch()
                                },
                                onDragCancel = { touchAffectionHandler.endTouch() },
                                onDragEnd = { touchAffectionHandler.endTouch() }
                            ) { change, dragAmount ->
                                change.consume()
                                resetIdleTimer()
                                val distance = hypot(dragAmount.x, dragAmount.y)
                                touchAffectionHandler.addStroke(distance)
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    resetIdleTimer()
                                    touchAffectionHandler.beginTouch()
                                    touchAffectionHandler.nudge()
                                    try {
                                        tryAwaitRelease()
                                    } finally {
                                        touchAffectionHandler.endTouch()
                                    }
                                }
                            )
                        }
                ) {
                    AvatarFace(
                        state = state,
                        headColor = MaterialTheme.colorScheme.primaryContainer,
                        featureColor = MaterialTheme.colorScheme.primary,
                        glowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        affectionLevel = touchAffectionHandler.affectionLevel,
                        tiltX = shakeState.tiltX,
                        tiltY = shakeState.tiltY,
                        bounceOffsetX = bounceOffsetX.value,
                        bounceOffsetY = bounceOffsetY.value,
                        isDizzy = isDizzy,
                        sleepiness = idleSleepiness
                    )
                }

                Box(
                    modifier = Modifier
                        .height(58.dp)
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (state == AvatarState.Speaking) {
                        ListeningIndicator(
                            isActive = true,
                            color = MaterialTheme.colorScheme.primary,
                            softColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .width(110.dp)
                                .height(24.dp)
                        )
                    }
                }
            }

            if (isTextInputMode) {
                ChatInputBar(
                    text = inputText,
                    onTextChange = { inputText = it },
                    onSendClick = ::sendTextMessage,
                    enabled = !isChatBusy,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 98.dp),
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            BottomInputControls(
                showNewConversationAction = isLoggedIn,
                isTextInputMode = isTextInputMode,
                isMicPressed = isMicPressed,
                isRecordingVoice = voiceUiPhase == VoiceUiPhase.Recording,
                onStartNewConversation = {
                    if (!isChatBusy) {
                        resetIdleTimer()
                        onStartNewConversation()
                    }
                },
                onMicPressState = { pressed ->
                    if (!isTextInputMode) {
                        if (pressed) {
                            if (voiceUiPhase == VoiceUiPhase.Idle && !isChatBusy) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    startVoiceRecordingFlow()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        } else if (voiceUiPhase == VoiceUiPhase.Recording && isMicPressed) {
                            stopAndSendVoiceMessage()
                        } else {
                            isMicPressed = false
                            if (state == AvatarState.Listening) state = AvatarState.Idle
                        }
                    }
                },
                onSwitchToTextMode = {
                    if (!isChatBusy) {
                        resetIdleTimer()
                        isTextInputMode = true
                        isMicPressed = false
                        if (state == AvatarState.Listening) state = AvatarState.Idle
                    }
                },
                onSwitchToVoiceMode = {
                    if (!isChatBusy) {
                        resetIdleTimer()
                        isTextInputMode = false
                        isMicPressed = false
                        state = AvatarState.Idle
                    }
                },
                onTextSend = ::sendTextMessage,
                isTextSendEnabled = inputText.isNotBlank() && !isChatBusy,
                isChatBusy = isChatBusy,
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
                                    if (!isChatBusy) {
                                        resetIdleTimer()
                                        showDebugPanel = !showDebugPanel
                                    }
                                }
                                .alpha(0.72f)
                        )
                    }
                    if (showDebugPanel) {
                        Text(
                            text =
                                "A:${"%.2f".format(touchAffectionHandler.affectionLevel)} " +
                                    "S:${"%.2f".format(shakeState.shakeStrength)} " +
                                    "M:${"%.1f".format(shakeState.shakeMagnitude)} " +
                                    "J:${"%.2f".format(shakeState.jerkStrength)} " +
                                    "C:${shakeState.strongShakeCount} " +
                                    "TH:${"%.1f".format(shakeState.strongMagnitudeThreshold)}/${"%.0f".format(shakeState.strongJerkThreshold)}/${shakeState.strongRequiredHits} " +
                                    "CD:${"%.1f".format(shakeCooldownRemainingMs / 1000f)}s " +
                                    "D:${if (isDizzy) "Y" else "N"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.alpha(0.66f)
                        )
                    }
                    AnimatedVisibility(visible = showDebugPanel) {
                        EmotionButtons(
                            selected = state,
                            states = states,
                            onSelect = {
                                if (!isChatBusy) {
                                    resetIdleTimer()
                                    state = it
                                }
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
private fun BottomInputControls(
    showNewConversationAction: Boolean,
    isTextInputMode: Boolean,
    isMicPressed: Boolean,
    isRecordingVoice: Boolean,
    onStartNewConversation: () -> Unit,
    onMicPressState: (Boolean) -> Unit,
    onSwitchToTextMode: () -> Unit,
    onSwitchToVoiceMode: () -> Unit,
    onTextSend: () -> Unit,
    isTextSendEnabled: Boolean,
    isChatBusy: Boolean,
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
            .padding(bottom = if (isTextInputMode) 12.dp else 14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomStart
            ) {
                if (showNewConversationAction) {
                    IconButton(
                        onClick = onStartNewConversation,
                        enabled = !isChatBusy,
                        modifier = Modifier
                            .padding(start = 2.dp, bottom = 8.dp)
                            .size(44.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "開始新對話",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (isTextInputMode) {
                    IconButton(
                        onClick = onTextSend,
                        enabled = isTextSendEnabled && !isChatBusy,
                        modifier = Modifier
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
                } else {
                    Box(
                        modifier = Modifier.padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val canStartOrStopMic = !isChatBusy || isRecordingVoice
                        val currentCanStartOrStopMic by rememberUpdatedState(canStartOrStopMic)
                        val currentOnMicPressState by rememberUpdatedState(onMicPressState)
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
                                .alpha(if (canStartOrStopMic) 1f else 0.55f)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            if (currentCanStartOrStopMic) {
                                                currentOnMicPressState(true)
                                                try {
                                                    tryAwaitRelease()
                                                } finally {
                                                    currentOnMicPressState(false)
                                                }
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
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomEnd
            ) {
                IconButton(
                    onClick = if (isTextInputMode) onSwitchToVoiceMode else onSwitchToTextMode,
                    enabled = !isChatBusy,
                    modifier = Modifier
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
                            imageVector = if (isTextInputMode) Icons.Filled.Mic else Icons.Filled.Keyboard,
                            contentDescription = if (isTextInputMode) "切回語音模式" else "切換文字模式",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(if (isTextInputMode) 19.dp else 18.dp)
                        )
                    }
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

private enum class VoiceUiPhase {
    Idle,
    Recording,
    SendingVoice,
    PlayingResponse
}
