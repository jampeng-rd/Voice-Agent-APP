package com.jam.voiceagent.ui.avatar

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun AvatarFace(
    state: AvatarState,
    modifier: Modifier = Modifier,
    headColor: Color,
    featureColor: Color,
    glowColor: Color,
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
                1f at 3100
                0.12f at 3260
                1f at 3420
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
    val idleSmile = transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 6400
                0f at 0
                0f at 2600
                0.35f at 3300
                0.12f at 4100
                0f at 5100
                0f at 6400
            }
        ),
        label = "idle-smile"
    )

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
                .background(headColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AvatarEyes(
                state = state,
                blinkFactor = if (state == AvatarState.Helpless) 0.2f else blink.value,
                color = featureColor,
                modifier = Modifier
                    .size(size)
                    .align(Alignment.Center)
            )
            AvatarMouth(
                state = state,
                talkFactor = if (state == AvatarState.Speaking) talk.value else 0f,
                idleSmileFactor = if (state == AvatarState.Idle) idleSmile.value else 0f,
                color = featureColor,
                modifier = Modifier
                    .size(size)
                    .align(Alignment.Center)
            )
        }
    }
}
