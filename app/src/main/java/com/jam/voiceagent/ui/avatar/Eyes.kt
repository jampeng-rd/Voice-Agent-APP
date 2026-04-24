package com.jam.voiceagent.ui.avatar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.max

@Composable
fun AvatarEyes(
    state: AvatarState,
    blinkFactor: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val eyeY = size.height * 0.43f
        val leftX = size.width * 0.38f
        val rightX = size.width * 0.62f
        val r = size.minDimension * 0.048f
        val stroke = size.minDimension * 0.018f

        when (state) {
            AvatarState.Happy -> {
                drawArc(
                    color = color,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(leftX - r * 1.4f, eyeY - r * 0.5f),
                    size = androidx.compose.ui.geometry.Size(r * 2.8f, r * 1.8f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(rightX - r * 1.4f, eyeY - r * 0.5f),
                    size = androidx.compose.ui.geometry.Size(r * 2.8f, r * 1.8f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            AvatarState.Helpless -> {
                drawLine(
                    color = color,
                    start = Offset(leftX - r, eyeY),
                    end = Offset(leftX + r, eyeY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(rightX - r, eyeY),
                    end = Offset(rightX + r, eyeY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }

            AvatarState.Sad -> {
                drawCircle(color = color, radius = r * 0.9f, center = Offset(leftX, eyeY + r * 0.12f))
                drawCircle(color = color, radius = r * 0.9f, center = Offset(rightX, eyeY + r * 0.12f))
                drawLine(
                    color = color,
                    start = Offset(leftX - r * 1.4f, eyeY - r * 1.3f),
                    end = Offset(leftX - r * 0.2f, eyeY - r * 1.7f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(rightX + r * 0.2f, eyeY - r * 1.7f),
                    end = Offset(rightX + r * 1.4f, eyeY - r * 1.3f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }

            AvatarState.Confused -> {
                drawCircle(color = color, radius = r * 0.9f, center = Offset(leftX, eyeY))
                drawCircle(color = color, radius = r * 0.7f, center = Offset(rightX, eyeY + r * 0.12f))
                drawLine(
                    color = color,
                    start = Offset(leftX - r * 1.2f, eyeY - r * 1.5f),
                    end = Offset(leftX + r * 0.5f, eyeY - r * 1.9f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(rightX - r * 0.6f, eyeY - r * 1.4f),
                    end = Offset(rightX + r * 1.2f, eyeY - r * 1.1f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }

            AvatarState.Surprised -> {
                drawCircle(color = color, radius = r * 1.2f, center = Offset(leftX, eyeY))
                drawCircle(color = color, radius = r * 1.2f, center = Offset(rightX, eyeY))
            }

            else -> {
                val openHeight = max(r * 0.35f, r * blinkFactor)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(leftX - r, eyeY - openHeight),
                    size = androidx.compose.ui.geometry.Size(r * 2f, openHeight * 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
                )
                drawRoundRect(
                    color = color,
                    topLeft = Offset(rightX - r, eyeY - openHeight),
                    size = androidx.compose.ui.geometry.Size(r * 2f, openHeight * 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
                )
            }
        }
    }
}
