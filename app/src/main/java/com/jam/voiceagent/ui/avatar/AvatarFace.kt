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
import androidx.compose.ui.graphics.graphicsLayer
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
    affectionLevel: Float = 0f,
    tiltX: Float = 0f,
    tiltY: Float = 0f,
    bounceOffsetX: Float = 0f,
    bounceOffsetY: Float = 0f,
    isDizzy: Boolean = false,
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
    val listeningPulseSoft = transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listening-pulse-soft"
    )
    val listeningPulseMid = transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listening-pulse-mid"
    )
    val listeningPulseOuter = transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listening-pulse-outer"
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
        if (state != AvatarState.Idle) return@LaunchedEffect

        while (true) {
            kotlinx.coroutines.delay(Random.nextLong(2600L, 3601L))
            when (Random.nextInt(100)) {
                in 0..40 -> {
                    idleSmileTarget = 0f
                    idleSurpriseTarget = 0f
                    idleTiredTarget = 0f
                }
                in 41..72 -> {
                    idleSmileTarget = Random.nextFloat() * 0.2f + 0.2f
                    idleSurpriseTarget = 0f
                    idleTiredTarget = Random.nextFloat() * 0.08f
                }
                in 73..87 -> {
                    idleSmileTarget = 0f
                    idleSurpriseTarget = Random.nextFloat() * 0.16f + 0.08f
                    idleTiredTarget = 0f
                }
                else -> {
                    idleSmileTarget = 0f
                    idleSurpriseTarget = 0f
                    idleTiredTarget = Random.nextFloat() * 0.2f + 0.11f
                }
            }
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
    val affectionWarmth by animateFloatAsState(
        targetValue = affectionLevel.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 420),
        label = "affection-warmth"
    )
    val warmPink = lerp(Color(0xFFF7B6B2), Color(0xFFF3A0A0), affectionWarmth * 0.78f)
    val affectionHeadColor = lerp(headColor, warmPink, affectionWarmth * 0.72f)
    val effectiveHeadColor = lerp(affectionHeadColor, Color(0xFF161D1A), sleepFactor * 0.9f)
    val effectiveFeatureColor = lerp(featureColor, Color(0xFFE6F1EE), sleepFactor * 0.88f)
    val effectiveBlink = when {
        isDizzy -> 0.16f
        state == AvatarState.Idle -> {
            (blink.value * (1f - idleTired * 0.25f - sleepFactor * 0.72f) + idleSurprise * 0.08f)
                .coerceIn(0.06f, 1.06f)
        }
        state == AvatarState.Helpless -> 0.2f
        else -> blink.value
    }

    val animatedTiltX by animateFloatAsState(tiltX.coerceIn(-1f, 1f), tween(300), label = "tilt-x")
    val animatedTiltY by animateFloatAsState(tiltY.coerceIn(-1f, 1f), tween(300), label = "tilt-y")
    val animatedBounceX by animateFloatAsState(bounceOffsetX, tween(120), label = "bounce-x")
    val animatedBounce by animateFloatAsState(bounceOffsetY, tween(180), label = "bounce-y")

    Box(
        modifier = modifier
            .offset(
                x = animatedBounceX.roundToInt().dp,
                y = (floating.value + animatedBounce).roundToInt().dp
            )
            .graphicsLayer {
                translationX = animatedTiltX * 56f
                translationY = animatedTiltY * 36f
                rotationZ = animatedTiltX * 18f
            }
            .size(size),
        contentAlignment = Alignment.Center
    ) {
        if (state == AvatarState.Listening) {
            Box(
                modifier = Modifier
                    .size(size * 1.18f)
                    .scale(listeningPulseSoft.value)
                    .alpha(0.4f)
                    .background(glowColor.copy(alpha = 0.54f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(size * 1.34f)
                    .scale(listeningPulseMid.value)
                    .alpha(0.3f)
                    .background(glowColor.copy(alpha = 0.38f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(size * 1.52f)
                    .scale(listeningPulseOuter.value)
                    .alpha(0.22f)
                    .background(glowColor.copy(alpha = 0.26f), CircleShape)
            )
        } else if (state == AvatarState.Speaking || affectionWarmth > 0.25f) {
            Box(
                modifier = Modifier
                    .size(size * 1.12f)
                    .scale(if (affectionWarmth > 0.25f) pulse.value else 1f)
                    .alpha(0.42f + affectionWarmth * 0.08f)
                    .background(
                        lerp(glowColor, Color(0xFFF5B6C4), affectionWarmth * 0.86f),
                        CircleShape
                    )
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
                affectionLevel = affectionWarmth,
                sleepiness = sleepFactor,
                isDizzy = isDizzy,
                color = effectiveFeatureColor,
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
                isDizzy = isDizzy,
                affectionLevel = affectionWarmth,
                color = effectiveFeatureColor,
                modifier = Modifier
                    .size(size)
                    .align(Alignment.Center)
            )
        }
    }
}
