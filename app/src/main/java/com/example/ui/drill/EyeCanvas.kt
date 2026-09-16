package com.example.ui.drill

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import kotlin.math.abs

/**
 * An eye drawn from two quadratic curves, with the iris clipped to the lid opening so
 * the lid genuinely sweeps over it rather than the whole eye just scaling down.
 *
 * [openness] 0 is shut, 1 is wide. [squeeze] adds the extra screwed-shut compression
 * the blink protocol asks for, which is what distinguishes a squeeze from a plain close.
 */
@Composable
fun BlinkingEye(
    openness: Float,
    squeeze: Float,
    irisColor: Color,
    lidColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp
) {
    Canvas(modifier = modifier.then(Modifier.size(size))) {
        drawEye(
            openness = openness.coerceIn(0f, 1f),
            squeeze = squeeze.coerceIn(0f, 1f),
            irisColor = irisColor,
            lidColor = lidColor
        )
    }
}

private fun DrawScope.drawEye(
    openness: Float,
    squeeze: Float,
    irisColor: Color,
    lidColor: Color
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val halfWidth = size.minDimension * 0.36f

    // A quadratic peaks at half its control offset, so double the height we want.
    val upperRise = halfWidth * 0.60f * openness
    val lowerDrop = halfWidth * 0.32f * openness

    val left = Offset(cx - halfWidth, cy)
    val right = Offset(cx + halfWidth, cy)

    val eye = Path().apply {
        moveTo(left.x, left.y)
        quadraticBezierTo(cx, cy - upperRise * 2f, right.x, right.y)
        quadraticBezierTo(cx, cy + lowerDrop * 2f, left.x, left.y)
        close()
    }

    // Soft ambient glow so the shape reads against a dark ground.
    //
    // A radial fade, not a flat-alpha circle. Flat alpha leaves a hard disc edge, which
    // went unnoticed while the iris was teal on navy and became an obvious brown plate
    // once it turned amber on a warm ground.
    val glowRadius = halfWidth * 1.25f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                irisColor.copy(alpha = 0.14f + openness * 0.10f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = glowRadius
        ),
        radius = glowRadius,
        center = Offset(cx, cy)
    )

    if (openness > 0.04f) {
        clipPath(eye) {
            // Sclera
            drawRect(color = lidColor.copy(alpha = 0.10f))

            val irisRadius = halfWidth * 0.44f
            drawCircle(color = irisColor.copy(alpha = 0.85f), radius = irisRadius, center = Offset(cx, cy))
            drawCircle(
                color = irisColor,
                radius = irisRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )
            // Pupil
            drawCircle(color = Color.Black.copy(alpha = 0.55f), radius = irisRadius * 0.44f, center = Offset(cx, cy))
            // Catchlight, offset so the eye reads as lit from above-left.
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = irisRadius * 0.16f,
                center = Offset(cx - irisRadius * 0.34f, cy - irisRadius * 0.34f)
            )
        }
    }

    // Lid outline. At full close this collapses to the lash line.
    drawPath(
        path = eye,
        color = lidColor,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    if (openness <= 0.30f) {
        // Closed lash line, drawn a touch wider than the lid so the shut eye still has
        // a definite edge instead of fading to nothing.
        val lashSpread = halfWidth * (1f + squeeze * 0.06f)
        drawLine(
            color = lidColor,
            start = Offset(cx - lashSpread, cy),
            end = Offset(cx + lashSpread, cy),
            strokeWidth = (3f + squeeze * 2f).dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    if (squeeze > 0.01f) {
        // Crease marks at the corners: the visual cue that this is a squeeze, not a blink.
        val creaseLength = halfWidth * 0.26f * squeeze
        listOf(-1f, 1f).forEach { side ->
            listOf(-1f, 1f).forEach { vertical ->
                val originX = cx + side * halfWidth
                drawLine(
                    color = lidColor.copy(alpha = 0.55f * squeeze),
                    start = Offset(originX, cy),
                    end = Offset(
                        originX + side * creaseLength,
                        cy + vertical * creaseLength * 0.55f
                    ),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/** The three stages of one protocol cycle, in order. */
enum class BlinkStage(val label: String) { CLOSE("Close"), SQUEEZE("Squeeze"), OPEN("Open") }

/**
 * The stage a cycle is in.
 *
 * Derived from the same thirds as [blinkCycleState] rather than from openness: openness
 * alone cannot tell closing from opening, which had the drill telling the user to close
 * while the eye was opening.
 */
fun blinkStageFor(cycleProgress: Float): BlinkStage {
    val t = ((cycleProgress % 1f) + 1f) % 1f
    return when ((t * 3f).toInt().coerceIn(0, 2)) {
        0 -> BlinkStage.CLOSE
        1 -> BlinkStage.SQUEEZE
        else -> BlinkStage.OPEN
    }
}

/**
 * Maps elapsed progress through one close-squeeze-open cycle onto lid state.
 *
 * The trial protocol is close 2s, squeeze 2s, open 2s, so each stage is a third of the
 * cycle. Returned as a pair of openness to squeeze.
 */
fun blinkCycleState(cycleProgress: Float): Pair<Float, Float> {
    val t = ((cycleProgress % 1f) + 1f) % 1f
    val stage = (t * 3f).toInt().coerceIn(0, 2)
    val within = (t * 3f) - stage
    return when (stage) {
        0 -> (1f - within) to 0f                      // closing
        1 -> 0f to (1f - abs(within - 0.5f) * 2f)     // squeeze peaks mid-stage
        else -> within to 0f                          // opening
    }
}
