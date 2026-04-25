package com.jam.voiceagent.ui.avatar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.max

private fun smoothStep(edge0: Float, edge1: Float, x: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

@Composable
fun AvatarEyes(
    state: AvatarState,
    blinkFactor: Float,
    idleSurpriseFactor: Float,
    idleTiredFactor: Float,
    affectionLevel: Float,
    sleepiness: Float,
    isDizzy: Boolean,
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
                    size = Size(r * 2.8f, r * 1.8f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(rightX - r * 1.4f, eyeY - r * 0.5f),
                    size = Size(r * 2.8f, r * 1.8f),
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
                if (isDizzy) {
                    val swirlStroke = stroke * 0.85f
                    val swirlSize = Size(r * 2.3f, r * 2.3f)
                    drawArc(
                        color = color,
                        startAngle = 20f,
                        sweepAngle = 290f,
                        useCenter = false,
                        topLeft = Offset(leftX - swirlSize.width / 2f, eyeY - swirlSize.height / 2f),
                        size = swirlSize,
                        style = Stroke(width = swirlStroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = color,
                        startAngle = 50f,
                        sweepAngle = 290f,
                        useCenter = false,
                        topLeft = Offset(rightX - swirlSize.width / 2f, eyeY - swirlSize.height / 2f),
                        size = swirlSize,
                        style = Stroke(width = swirlStroke, cap = StrokeCap.Round)
                    )
                    return@Canvas
                }

                val surpriseBoost = idleSurpriseFactor * r * 0.26f
                val tiredClamp = (idleTiredFactor * r * 0.24f) + (sleepiness * r * 0.52f)
                val openHeight = (max(r * 0.35f, r * blinkFactor) + surpriseBoost - tiredClamp)
                    .coerceIn(r * 0.1f, r * 1.08f)
                val eyeWidth = (r * (1f + idleSurpriseFactor * 0.08f - idleTiredFactor * 0.08f))
                    .coerceAtLeast(r * 0.82f)
                val joyMid = smoothStep(0.26f, 0.7f, affectionLevel)
                val joyHigh = smoothStep(0.62f, 0.98f, affectionLevel)
                val affectionSquint = (joyMid * 0.58f + joyHigh * 0.42f).coerceIn(0f, 1f)

                if (sleepiness > 0.8f && state == AvatarState.Idle) {
                    drawLine(
                        color = color,
                        start = Offset(leftX - eyeWidth, eyeY),
                        end = Offset(leftX + eyeWidth, eyeY),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = color,
                        start = Offset(rightX - eyeWidth, eyeY),
                        end = Offset(rightX + eyeWidth, eyeY),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                } else {
                    val affectionShrink = affectionSquint * 0.4f
                    val openEyeAlpha = (1f - joyHigh * 0.95f).coerceIn(0f, 1f)
                    if (openEyeAlpha > 0.02f) {
                        drawRoundRect(
                            color = color.copy(alpha = openEyeAlpha),
                            topLeft = Offset(leftX - eyeWidth, eyeY - openHeight),
                            size = Size(eyeWidth * 2f, openHeight * 2f * (1f - affectionShrink)),
                            cornerRadius = CornerRadius(r, r)
                        )
                        drawRoundRect(
                            color = color.copy(alpha = openEyeAlpha),
                            topLeft = Offset(rightX - eyeWidth, eyeY - openHeight),
                            size = Size(eyeWidth * 2f, openHeight * 2f * (1f - affectionShrink)),
                            cornerRadius = CornerRadius(r, r)
                        )
                    }

                    if (joyHigh > 0.01f) {
                        val sweep = 128f + joyHigh * 24f
                        val width = r * (2.38f + joyHigh * 0.55f)
                        val height = r * (1.5f + joyHigh * 0.35f)
                        val raise = r * (0.22f + affectionSquint * 0.26f)
                        val laughColor = color.copy(alpha = joyHigh)
                        drawArc(
                            color = laughColor,
                            startAngle = 205f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(leftX - width / 2f, eyeY - raise - height * 0.5f),
                            size = Size(width, height),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = laughColor,
                            startAngle = 205f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(rightX - width / 2f, eyeY - raise - height * 0.5f),
                            size = Size(width, height),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}
