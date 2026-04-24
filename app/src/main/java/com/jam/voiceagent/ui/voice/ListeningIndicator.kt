package com.jam.voiceagent.ui.voice

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun ListeningIndicator(
    isActive: Boolean,
    color: Color,
    softColor: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "speaking-wave")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-phase"
    )

    Canvas(
        modifier = modifier
            .width(102.dp)
            .height(22.dp)
    ) {
        val profile = listOf(0.16f, 0.24f, 0.35f, 0.5f, 0.65f, 0.82f, 1f, 0.82f, 0.65f, 0.5f, 0.35f, 0.24f, 0.16f)
        val count = profile.size
        val barWidth = size.width / 33f
        val gap = (size.width - barWidth * count) / (count - 1)
        val centerY = size.height / 2f

        repeat(count) { index ->
            val normalized = profile[index]
            val rhythm = 0.9f + (sin(phase.value + index * 0.5f).toFloat() * 0.1f)
            val amplitude = if (isActive) normalized * rhythm else normalized * 0.2f
            val minHalfHeight = size.height * 0.1f
            val halfHeight = (size.height * 0.46f * amplitude).coerceAtLeast(minHalfHeight)
            val x = index * (barWidth + gap)
            val fill = if (index in 4..8) color.copy(alpha = 0.68f) else softColor.copy(alpha = 0.56f)

            drawRoundRect(
                color = fill,
                topLeft = Offset(x, centerY - halfHeight),
                size = Size(barWidth, halfHeight * 2f),
                cornerRadius = CornerRadius(barWidth, barWidth)
            )
        }
    }
}
