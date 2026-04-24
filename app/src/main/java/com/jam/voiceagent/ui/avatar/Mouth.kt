package com.jam.voiceagent.ui.avatar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun AvatarMouth(
    state: AvatarState,
    talkFactor: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width * 0.5f, size.height * 0.63f)
        val base = size.minDimension * 0.11f
        val stroke = size.minDimension * 0.018f

        when (state) {
            AvatarState.Idle,
            AvatarState.Thinking,
            AvatarState.Helpless -> {
                drawLine(
                    color = color,
                    start = Offset(center.x - base * 0.55f, center.y),
                    end = Offset(center.x + base * 0.55f, center.y),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }

            AvatarState.Listening -> {
                drawArc(
                    color = color,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(center.x - base * 0.8f, center.y - base * 0.35f),
                    size = Size(base * 1.6f, base * 1.0f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            AvatarState.Speaking -> {
                val width = base * (0.85f + talkFactor * 0.25f)
                val height = base * (0.95f + talkFactor * 0.45f)
                drawOval(
                    color = color,
                    topLeft = Offset(center.x - width / 2f, center.y - height / 2f),
                    size = Size(width, height)
                )
            }

            AvatarState.Happy -> {
                drawArc(
                    color = color,
                    startAngle = 12f,
                    sweepAngle = 156f,
                    useCenter = false,
                    topLeft = Offset(center.x - base, center.y - base * 0.25f),
                    size = Size(base * 2f, base * 1.35f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            AvatarState.Sad,
            AvatarState.Confused -> {
                drawArc(
                    color = color,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(center.x - base * 0.9f, center.y - base * 0.1f),
                    size = Size(base * 1.8f, base * 1.1f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            AvatarState.Surprised -> {
                drawCircle(color = color, radius = base * 0.42f, center = center)
            }
        }
    }
}
