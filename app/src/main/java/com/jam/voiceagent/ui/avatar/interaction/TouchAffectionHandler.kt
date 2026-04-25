package com.jam.voiceagent.ui.avatar.interaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

class TouchAffectionHandler {
    var affectionLevel by mutableFloatStateOf(0f)
        private set

    var isTouchingAvatar by mutableStateOf(false)
        private set

    fun beginTouch() {
        isTouchingAvatar = true
    }

    fun addStroke(distancePx: Float) {
        val delta = (distancePx / 2400f).coerceIn(0f, 0.025f)
        affectionLevel = (affectionLevel + delta).coerceIn(0f, 1f)
    }

    fun nudge() {
        affectionLevel = (affectionLevel + 0.03f).coerceIn(0f, 1f)
    }

    fun endTouch() {
        isTouchingAvatar = false
    }

    fun decay() {
        affectionLevel = (affectionLevel - 0.004f).coerceAtLeast(0f)
    }
}

@Composable
fun rememberTouchAffectionHandler(): TouchAffectionHandler {
    val handler = remember { TouchAffectionHandler() }

    LaunchedEffect(handler) {
        snapshotFlow { handler.isTouchingAvatar }.collect {
            while (!handler.isTouchingAvatar) {
                if (handler.affectionLevel <= 0f) break
                handler.decay()
                delay(120)
            }
        }
    }

    return handler
}
