package com.jam.voiceagent.ui.avatar.interaction

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class ShakeSensorController(
    context: Context,
    private val onStateChanged: (ShakeInteractionState) -> Unit
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 0f

    private var strongShakeCount = 0
    private var previousMagnitude = 0f
    private var previousTimestampNs = 0L

    private val baseThreshold = 2.0f
    private val strongMagnitudeThreshold = 19.5f
    private val strongJerkThreshold = 95f
    private val minConsecutiveStrongHits = 3

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val alpha = 0.84f
            gravityX = alpha * gravityX + (1f - alpha) * event.values[0]
            gravityY = alpha * gravityY + (1f - alpha) * event.values[1]
            gravityZ = alpha * gravityZ + (1f - alpha) * event.values[2]

            val linearX = event.values[0] - gravityX
            val linearY = event.values[1] - gravityY
            val linearZ = event.values[2] - gravityZ

            val shakeMagnitude = sqrt(linearX * linearX + linearY * linearY + linearZ * linearZ)

            val deltaTimeSec = if (previousTimestampNs == 0L) {
                0.016f
            } else {
                max((event.timestamp - previousTimestampNs) / 1_000_000_000f, 0.008f)
            }
            val jerk = abs(shakeMagnitude - previousMagnitude) / deltaTimeSec
            previousMagnitude = shakeMagnitude
            previousTimestampNs = event.timestamp

            val normalizedShake = ((shakeMagnitude - baseThreshold) / 18f).coerceIn(0f, 1.6f)
            val normalizedJerk = (jerk / 220f).coerceIn(0f, 1.8f)

            val tiltX = (-gravityX / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)
            val tiltY = (gravityY / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)

            val strongCandidate =
                shakeMagnitude >= strongMagnitudeThreshold && jerk >= strongJerkThreshold

            strongShakeCount = if (strongCandidate) {
                (strongShakeCount + 1).coerceAtMost(minConsecutiveStrongHits + 6)
            } else {
                (strongShakeCount - 1).coerceAtLeast(0)
            }

            val isStrongShake = strongShakeCount >= minConsecutiveStrongHits

            onStateChanged(
                ShakeInteractionState(
                    sensorAvailable = true,
                    tiltX = tiltX,
                    tiltY = tiltY,
                    shakeStrength = normalizedShake,
                    isStrongShake = isStrongShake,
                    strongShakeCount = strongShakeCount,
                    shakeMagnitude = shakeMagnitude,
                    jerkStrength = normalizedJerk,
                    cooldownRemainingMs = 0L
                )
            )
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun start() {
        if (accelerometer == null) {
            onStateChanged(
                ShakeInteractionState(
                    sensorAvailable = false,
                    tiltX = 0f,
                    tiltY = 0f,
                    shakeStrength = 0f,
                    isStrongShake = false
                )
            )
            return
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
    }
}
