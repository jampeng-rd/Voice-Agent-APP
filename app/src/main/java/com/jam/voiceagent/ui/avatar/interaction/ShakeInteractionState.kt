package com.jam.voiceagent.ui.avatar.interaction

data class ShakeInteractionState(
    val sensorAvailable: Boolean,
    val tiltX: Float,
    val tiltY: Float,
    val shakeStrength: Float,
    val isStrongShake: Boolean,
    val strongShakeCount: Int = 0,
    val shakeMagnitude: Float = 0f,
    val jerkStrength: Float = 0f,
    val cooldownRemainingMs: Long = 0L
)
