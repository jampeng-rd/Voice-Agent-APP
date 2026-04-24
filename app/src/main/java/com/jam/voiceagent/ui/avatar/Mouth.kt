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
    idleSmileFactor: Float,
    idleSurpriseFactor: Float,
    idleTiredFactor: Float,
    sleepiness: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width * 0.5f, size.height * 0.63f)
        val base = size.minDimension * 0.11f
        val stroke = size.minDimension * 0.018f

        when (state) {
            AvatarState.Idle -> {
                val sleepy = (idleTiredFactor + sleepiness).coerceIn(0f, 1f)
                val surprise = idleSurpriseFactor.coerceIn(0f, 1f)
                val smile = idleSmileFactor.coerceIn(0f, 1f)

                when {
                    sleepy > 0.74f -> {
                        drawLine(
                            color = color,
                            start = Offset(center.x - base * 0.45f, center.y + base * 0.04f),
                            end = Offset(center.x + base * 0.45f, center.y + base * 0.04f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }

                    surprise > 0.2f -> {
                        val radius = base * (0.25f + surprise * 0.12f)
                        drawCircle(color = color, radius = radius, center = center)
                    }

                    smile > 0.06f -> {
                        drawArc(
                            color = color,
                            startAngle = 16f,
                            sweepAngle = 148f,
                            useCenter = false,
                            topLeft = Offset(
                                center.x - base * (0.72f + smile * 0.1f),
                                center.y - base * (0.19f + smile * 0.2f)
                            ),
                            size = Size(
                                base * (1.44f + smile * 0.2f),
                                base * (0.32f + smile * 0.3f)
                            ),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    else -> {
                        drawLine(
                            color = color,
                            start = Offset(center.x - base * 0.55f, center.y),
                            end = Offset(center.x + base * 0.55f, center.y),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

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
