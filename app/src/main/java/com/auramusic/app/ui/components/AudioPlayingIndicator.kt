package com.auramusic.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.auramusic.app.ui.theme.AuraNeonLime

/**
 * Six-bar animated equalizer indicator displayed on track cover images while playing.
 * Faithfully transcribed from SimpMusic's PlaybackIndicators.kt.
 *
 * Mathematical properties:
 * - 6 bars grown from the vertical centre (centred at y=75 in a 200x150 comp).
 * - Positioned on a 12-unit pitch spanning x 70.8..130.9.
 * - 1.44s loop cycle (1,440ms) smoothly eased with cubic smoothstep (lerpSmooth).
 * - Default color: Aura's signature Lime (#D4F644 / AuraNeonLime).
 */
@Composable
fun AudioPlayingIndicator(
    modifier: Modifier = Modifier,
    color: Color = AudioIndicatorLime,
) {
    val transition = rememberInfiniteTransition(label = "audioPlaying")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = AUDIO_CYCLE_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Canvas(modifier = modifier) {
        val scale = minOf(size.width / AUDIO_COMP_WIDTH, size.height / AUDIO_COMP_HEIGHT)
        val originX = (size.width - AUDIO_COMP_WIDTH * scale) / 2f
        val originY = (size.height - AUDIO_COMP_HEIGHT * scale) / 2f
        val centreY = originY + AUDIO_COMP_CENTRE_Y * scale
        val strokeWidth = AUDIO_BAR_WIDTH * scale
        val capRadius = strokeWidth / 2f

        AUDIO_BAR_X.forEachIndexed { bar, barX ->
            val height = audioBarHeight(bar, phase) * scale
            val half = (height / 2f - capRadius).coerceAtLeast(0f)
            val x = originX + barX * scale
            drawLine(
                color = color,
                start = Offset(x, centreY - half),
                end = Offset(x, centreY + half),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun audioBarHeight(
    bar: Int,
    phase: Float,
): Float {
    val heights = AUDIO_BAR_HEIGHTS[bar]
    val steps = heights.size - 1
    val position = phase.coerceIn(0f, 1f) * steps
    val index = position.toInt().coerceAtMost(steps - 1)
    return lerpSmooth(position - index, heights[index], heights[index + 1])
}

private fun lerpSmooth(
    t: Float,
    from: Float,
    to: Float,
): Float = from + (to - from) * t * t * (3f - 2f * t)

val AudioIndicatorLime = AuraNeonLime
val AudioIndicatorCyan = Color(0xFF05EBFF)

private const val AUDIO_COMP_WIDTH = 200f
private const val AUDIO_COMP_HEIGHT = 150f
private const val AUDIO_COMP_CENTRE_Y = 75f
private const val AUDIO_BAR_WIDTH = 6f

private val AUDIO_BAR_X = floatArrayOf(70.808f, 82.933f, 94.933f, 106.808f, 118.933f, 130.933f)

private val AUDIO_BAR_HEIGHTS = arrayOf(
    floatArrayOf(40f, 15.2f, 40f, 25.7f, 16.7f, 15.2f, 40f),
    floatArrayOf(26.7f, 15.2f, 22.3f, 70.2f, 40f, 40f, 15.2f),
    floatArrayOf(56.5f, 70.2f, 40f, 15.2f, 13.8f, 15.2f, 68.2f),
    floatArrayOf(24.7f, 40f, 15.2f, 51.8f, 40f, 57.2f, 15.2f),
    floatArrayOf(40f, 15.2f, 40f, 70.2f, 40f, 15.2f, 40f),
    floatArrayOf(70.3f, 15.2f, 62.8f, 29f, 71.2f, 40f, 15.2f),
)

private const val AUDIO_CYCLE_MS = 1_440
