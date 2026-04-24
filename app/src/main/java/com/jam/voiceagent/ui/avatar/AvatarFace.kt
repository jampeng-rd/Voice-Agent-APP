package com.jam.voiceagent.ui.avatar

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun AvatarFace(
    state: AvatarState,
    modifier: Modifier = Modifier,
    headColor: Color,
    featureColor: Color,
    glowColor: Color,
    sleepiness: Float = 0f,
    size: Dp = 190.dp
) {
    val transition = rememberInfiniteTransition(label = "avatar-animation")
    val floating = transition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    val blink = transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4200
                1f at 0
                1f at 3000
                0.12f at 3220
                1f at 3460
                1f at 4200
            }
        ),
        label = "blink"
    )
    val pulse = transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val talk = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 280),
            repeatMode = RepeatMode.Reverse
        ),
        label = "talk"
    )

    var idleSmileTarget by remember { mutableFloatStateOf(0f) }
    var idleSurpriseTarget by remember { mutableFloatStateOf(0f) }
    var idleTiredTarget by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(state) {
        idleSmileTarget = 0f
        idleSurpriseTarget = 0f
        idleTiredTarget = 0f
        if (state != AvatarState.Idle) {
            return@LaunchedEffect
        }

        while (true) {
            kotlinx.coroutines.delay(Random.nextLong(4000L, 10001L))
            when (Random.nextInt(100)) {
                in 0..46 -> {
                    idleSmileTarget = 0f
                    idleSurpriseTarget = 0f
                    idleTiredTarget = 0f
                }

                in 47..77 -> {
                    idleSmileTarget = Random.nextFloat() * 0.28f + 0.18f
                    idleSurpriseTarget = 0f
                    idleTiredTarget = Random.nextFloat() * 0.1f
                }

                in 78..91 -> {
                    idleSmileTarget = 0f
                    idleSurpriseTarget = Random.nextFloat() * 0.22f + 0.1f
                    idleTiredTarget = 0f
                }

                else -> {
                    idleSmileTarget = 0f
                    idleSurpriseTarget = 0f
                    idleTiredTarget = Random.nextFloat() * 0.32f + 0.16f
                }
            }
            kotlinx.coroutines.delay(Random.nextLong(1700L, 3200L))
            idleSmileTarget = 0f
            idleSurpriseTarget = 0f
            idleTiredTarget = 0f
        }
    }

    val idleSmile by animateFloatAsState(
        targetValue = if (state == AvatarState.Idle) idleSmileTarget else 0f,
        animationSpec = tween(durationMillis = 1600),
        label = "idle-smile"
    )
    val idleSurprise by animateFloatAsState(
        targetValue = if (state == AvatarState.Idle) idleSurpriseTarget else 0f,
        animationSpec = tween(durationMillis = 1400),
        label = "idle-surprise"
    )
    val idleTired by animateFloatAsState(
        targetValue = if (state == AvatarState.Idle) idleTiredTarget else 0f,
        animationSpec = tween(durationMillis = 1800),
        label = "idle-tired"
    )

    val sleepFactor = sleepiness.coerceIn(0f, 1f)
    val effectiveHeadColor = lerp(headColor, Color(0xFF8FB5A6), sleepFactor * 0.55f)
    val effectiveBlink = when (state) {
        AvatarState.Idle -> {
            (blink.value * (1f - idleTired * 0.25f - sleepFactor * 0.72f) + idleSurprise * 0.08f)
                .coerceIn(0.06f, 1.06f)
        }

        AvatarState.Helpless -> 0.2f
        else -> blink.value
    }

    Box(
        modifier = modifier
            .offset(y = floating.value.roundToInt().dp)
            .size(size),
        contentAlignment = Alignment.Center
    ) {
        if (state == AvatarState.Listening || state == AvatarState.Speaking) {
            Box(
                modifier = Modifier
                    .size(size * 1.12f)
                    .scale(pulse.value)
                    .alpha(0.42f)
                    .background(glowColor, CircleShape)
            )
        }

        Box(
            modifier = Modifier
                .size(size)
                .background(effectiveHeadColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AvatarEyes(
                state = state,
                blinkFactor = effectiveBlink,
                idleSurpriseFactor = if (state == AvatarState.Idle) idleSurprise else 0f,
                idleTiredFactor = if (state == AvatarState.Idle) idleTired else 0f,
                sleepiness = sleepFactor,
                color = featureColor,
                modifier = Modifier
                    .size(size)
                    .align(Alignment.Center)
            )
            AvatarMouth(
                state = state,
                talkFactor = if (state == AvatarState.Speaking) talk.value else 0f,
                idleSmileFactor = if (state == AvatarState.Idle) idleSmile else 0f,
                idleSurpriseFactor = if (state == AvatarState.Idle) idleSurprise else 0f,
                idleTiredFactor = if (state == AvatarState.Idle) idleTired else 0f,
                sleepiness = sleepFactor,
                color = featureColor,
                modifier = Modifier
                    .size(size)
                    .align(Alignment.Center)
            )
        }
    }
}
