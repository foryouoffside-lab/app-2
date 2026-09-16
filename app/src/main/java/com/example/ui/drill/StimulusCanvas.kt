package com.example.ui.drill

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.StudioStimulus
import com.example.model.specFor
import com.example.model.targetFunction
import com.example.model.targetProgress
import com.example.ui.theme.AppTheme
import com.example.util.TargetColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The user's target preferences, resolved into what the canvas actually needs.
 *
 * [speed] multiplies how far along its path a target travels per cycle. It never changes
 * the drill's length -- the dose is set by the evidence record, not by this.
 */
data class TargetStyle(val color: Color, val speed: Float) {
    companion object {
        fun from(color: TargetColor, speed: Float) =
            TargetStyle(Color(color.argb), speed)
    }
}

/**
 * Every drill's stimulus, drawn to fill whatever box it is given.
 *
 * Sizes are fractions of the shorter edge rather than pixels, so the same code serves a
 * 56dp list thumbnail and a full-screen landscape drill.
 *
 * Colour is deliberately split in two. A plain mark takes the user's chosen colour. A
 * stimulus whose colours ARE the exercise keeps its own: the red/cyan anaglyph pair only
 * separates the eyes because of those specific filters, and the Brock string's three beads
 * have to stay distinguishable from each other at three depths. Letting a setting recolour
 * those would break the drill rather than personalise it.
 */
@Composable
fun StimulusCanvas(
    stimulus: StudioStimulus,
    phase: Float,
    active: Boolean,
    style: TargetStyle = TargetStyle(AppTheme.colors.amber, 1f)
) {
    val palette = StimulusPalette(
        accent = style.color,
        support = AppTheme.colors.teal,
        text = AppTheme.colors.textHigh,
        bg = AppTheme.colors.bg
    )
    Canvas(Modifier.fillMaxSize().padding(6.dp)) {
        drawStimulus(stimulus, phase, active, style.speed, palette)
    }
}

/** The four colours every stimulus draws from, resolved from the theme by the caller. */
data class StimulusPalette(val accent: Color, val support: Color, val text: Color, val bg: Color)

/**
 * The drawing itself, with no composition around it.
 *
 * Split out so it can be rendered into a bitmap and checked: a stimulus that draws the
 * same frame all the way through its cycle is a drill the user watches as a still image,
 * and there is no other way to catch that automatically.
 */
@Suppress("CyclomaticComplexMethod", "LongMethod")
fun DrawScope.drawStimulus(
    stimulus: StudioStimulus,
    phase: Float,
    active: Boolean,
    speed: Float,
    palette: StimulusPalette
) {
    val accent = palette.accent
    val support = palette.support
    val text = palette.text
    val bg = palette.bg
    val w = size.width
    val h = size.height
    val short = minOf(w, h)
    val center = Offset(w / 2, h / 2)

    val spec = specFor(stimulus.targetFunction)
    // Speed moves the target further along its path per cycle; it does not shorten
    // the drill.
    val t = if (active) ((phase * speed) % 1f + 1f) % 1f else 0f
    val p = targetProgress(spec, t)
    val r = short * spec.radius

    // A saccadic target must jump, not slide. Everything else follows its eased path.
    val step = if (t < .5f) 0f else 1f

    fun target(at: Offset, color: Color = accent, radius: Float = r) {
        drawCircle(color.copy(alpha = .18f), radius * 1.7f, at)
        drawCircle(color, radius, at)
        drawLine(Color.Black.copy(alpha = .5f), at - Offset(radius * .5f, 0f), at + Offset(radius * .5f, 0f), (radius * .12f).coerceAtLeast(1f))
        drawLine(Color.Black.copy(alpha = .5f), at - Offset(0f, radius * .5f), at + Offset(0f, radius * .5f), (radius * .12f).coerceAtLeast(1f))
    }

    fun stroke(width: Float) = Stroke((short * width).coerceAtLeast(1.5f))

    when (stimulus) {
        StudioStimulus.BLINK -> {
            val open = when {
                t < .33f -> 1f - t / .33f
                t < .66f -> .04f
                else -> (t - .66f) / .34f
            }
            val eye = Path().apply {
                moveTo(w * .18f, h * .5f)
                quadraticBezierTo(w * .5f, h * (.5f - .28f * open), w * .82f, h * .5f)
                quadraticBezierTo(w * .5f, h * (.5f + .16f * open), w * .18f, h * .5f)
                close()
            }
            drawPath(eye, text, style = stroke(.012f))
            if (open > .12f) target(center, accent, short * .085f)
        }

        StudioStimulus.BREAK_REMINDER -> {
            // A break is a length of time, so the drill has to show time passing. The
            // ring drains over the cycle and the hand sweeps it. The old version drew
            // a clock with its hands nailed on, which sat frozen for the whole break.
            val radius = short * .26f
            drawCircle(accent.copy(alpha = .09f), radius * 1.16f, center)
            drawCircle(text.copy(alpha = .18f), radius, center, style = stroke(.014f))
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * (1f - t),
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = stroke(.014f)
            )
            val hand = t * 2f * PI.toFloat() - PI.toFloat() / 2f
            drawLine(
                support, center,
                center + Offset(cos(hand) * radius * .70f, sin(hand) * radius * .70f),
                (short * .016f).coerceAtLeast(2f), StrokeCap.Round
            )
            drawCircle(text, (short * .020f).coerceAtLeast(2f), center)
        }

        // ---- vergence: the target travels in depth ----------------------------
        StudioStimulus.NEAR_TARGET -> target(Offset(w * (.18f + .64f * p), h * .5f))

        StudioStimulus.BROCK_STRING -> {
            // Red, green and yellow at three depths. Clinical colours, kept because
            // telling the beads apart is the exercise.
            drawLine(text.copy(alpha = .45f), Offset(w * .08f, h * .86f), Offset(w * .92f, h * .16f), (short * .010f).coerceAtLeast(1.5f))
            val beads = listOf(.26f to Color(0xFFE5484D), .5f to Color(0xFF30A46C), .74f to Color(0xFFFFC53D))
            beads.forEachIndexed { i, (f, color) ->
                val at = Offset(w * f, h * (.86f - .70f * ((f - .08f) / .84f)))
                // The bead being fixated grows; the others stay small.
                val active_ = (p * beads.size).toInt().coerceAtMost(beads.lastIndex) == i
                target(at, color, if (active_) r else r * .6f)
            }
        }

        StudioStimulus.VERGENCE_STEP -> {
            // Discrete vergence demands rather than a smooth glide.
            val levels = listOf(.06f, .14f, .24f)
            val spread = w * levels[(t * levels.size).toInt().coerceAtMost(levels.lastIndex)]
            target(center - Offset(spread, 0f), accent)
            target(center + Offset(spread, 0f), accent)
            drawCircle(text.copy(alpha = .22f), r * .6f, center, style = stroke(.006f))
        }

        StudioStimulus.FOCUS_SHIFT -> {
            val near = Offset(w * .28f, h * .64f)
            val far = Offset(w * .74f, h * .30f)
            drawLine(text.copy(alpha = .22f), near, far, (short * .008f).coerceAtLeast(1f))
            // Sized by the eased progress rather than a half-cycle test, so the
            // shift is visibly under way instead of teleporting between two states.
            target(near, accent, r * (1.5f - .8f * p))
            target(far, support, r * (.5f + .6f * p))
            drawCircle(accent.copy(alpha = .40f), r * .34f, near + (far - near) * p)
        }

        // ---- fusion: colours here are the mechanism --------------------------
        StudioStimulus.DISPARITY, StudioStimulus.DICHOPTIC -> {
            val spread = w * (.07f + .09f * p)
            target(center - Offset(spread, 0f), Color(0xFFE64B4B))
            target(center + Offset(spread, 0f), Color(0xFF31B7D9))
            drawCircle(text.copy(alpha = .25f), r * .8f, center, style = stroke(.006f))
        }

        StudioStimulus.STEREOGRAM -> {
            val stepSize = (short / 13f).coerceAtLeast(6f)
            var y = stepSize
            var row = 0
            while (y < h) {
                var x = stepSize
                var col = 0
                while (x < w) {
                    val shift = if ((row + col) % 5 == 0) w * .015f * p else 0f
                    drawCircle(
                        if ((row + col) % 2 == 0) accent else text.copy(alpha = .6f),
                        (stepSize * .15f).coerceAtLeast(1.2f), Offset(x + shift, y)
                    )
                    x += stepSize; col++
                }
                y += stepSize; row++
            }
        }

        StudioStimulus.APERTURE -> {
            // Two windows in a card, opening apart as the convergence demand steps up,
            // with the target behind each one moving to match and the single fused
            // image they are meant to make filling in between them.
            val gap = w * (.07f + .11f * p)
            val slotW = w * .13f
            val slotH = h * .56f
            val top = h * .22f
            listOf(-1f, 1f).forEach { side ->
                val left = center.x + side * gap - slotW / 2f
                drawRect(text.copy(alpha = .10f), Offset(left, top), Size(slotW, slotH))
                drawRect(text.copy(alpha = .38f), Offset(left, top), Size(slotW, slotH), style = stroke(.005f))
                target(Offset(left + slotW / 2f, center.y), accent, r * .70f)
            }
            drawCircle(support.copy(alpha = .16f + .30f * p), r * .95f, center)
            drawCircle(support.copy(alpha = .55f), r * .95f, center, style = stroke(.006f))
        }

        // ---- saccades: hold, then jump ---------------------------------------
        StudioStimulus.SACCADE ->
            target(Offset(w * (.16f + .68f * step), h * .5f))

        StudioStimulus.SACCADE_VERTICAL ->
            target(Offset(w * .5f, h * (.16f + .68f * step)))

        StudioStimulus.ANTI_SACCADE -> {
            // The mark appears on one side; the eye is meant to go to the other. The
            // hollow ring is where to look, and it has to be as visible as the target
            // or the drill teaches the reflex instead of suppressing it.
            val shown = Offset(w * (.16f + .68f * step), h * .5f)
            val away = Offset(w * (.84f - .68f * step), h * .5f)
            target(shown, accent)
            drawCircle(text.copy(alpha = .75f), r * 1.1f, away, style = stroke(.008f))
            drawCircle(text.copy(alpha = .30f), r * .35f, away)
        }

        // ---- pursuit: continuous, eased at every turn -------------------------
        StudioStimulus.PURSUIT -> target(Offset(w * (.12f + .76f * p), h * .5f))

        StudioStimulus.PURSUIT_CIRCULAR -> {
            val radius = short * .34f
            val angle = t * 2f * PI.toFloat()
            drawCircle(text.copy(alpha = .14f), radius, center, style = stroke(.005f))
            target(center + Offset(cos(angle) * radius, sin(angle) * radius))
        }

        StudioStimulus.PURSUIT_FIGURE_EIGHT -> {
            val a = w * .34f
            val b = h * .30f
            val angle = t * 2f * PI.toFloat()
            // Gerono lemniscate: one clean crossing of the midline per lap.
            val path = Path()
            var i = 0
            while (i <= 120) {
                val th = i / 120f * 2f * PI.toFloat()
                val px = center.x + cos(th) * a
                val py = center.y + sin(th) * cos(th) * b * 2f
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                i++
            }
            drawPath(path, text.copy(alpha = .14f), style = stroke(.005f))
            target(center + Offset(cos(angle) * a, sin(angle) * cos(angle) * b * 2f))
        }

        StudioStimulus.HEMIFIELD_PURSUIT -> {
            drawRect(support.copy(alpha = .07f), Offset.Zero, Size(w / 2, h))
            target(Offset(w * (.88f - .76f * p), h * .5f))
        }

        StudioStimulus.FIXATION -> {
            // The target holds still -- that is the drill -- so the rings are what show
            // the clock is still running. They breathe off t, because a fixation spec
            // deliberately pins targetProgress at zero and the ring sat frozen on it.
            val breathe = .5f - .5f * cos(t * 2f * PI.toFloat())
            target(center, accent, r)
            drawCircle(support.copy(alpha = .35f), short * (.13f + .06f * breathe), center, style = stroke(.006f))
            drawCircle(support.copy(alpha = .14f), short * (.21f + .06f * breathe), center, style = stroke(.004f))
        }

        // ---- relief: the eyes are shut, so this is a picture of the step ------
        StudioStimulus.PALMING -> {
            // One slow breath per cycle, carried by the warmth around the hands. The two
            // palms have to stay two palms: drawn as one shape they read as a coin.
            val breathe = .5f - .5f * cos(t * 2f * PI.toFloat())
            drawCircle(accent.copy(alpha = .05f + .06f * breathe), short * (.40f + .05f * breathe), center)
            drawCircle(accent.copy(alpha = .28f), short * (.40f + .05f * breathe), center, style = stroke(.004f))
            // The closed eye being covered, visible in the gap between the hands.
            val lid = Path().apply {
                moveTo(center.x - short * .10f, center.y)
                quadraticBezierTo(center.x, center.y + short * .07f, center.x + short * .10f, center.y)
            }
            drawPath(lid, text.copy(alpha = .40f), style = stroke(.008f))
            listOf(-1f, 1f).forEach { side ->
                val width = short * .30f
                val height = short * .40f
                val left = center.x + if (side < 0f) -width - short * .015f else short * .015f
                val top = center.y - height / 2f
                drawRoundRect(
                    accent.copy(alpha = .20f), Offset(left, top), Size(width, height),
                    CornerRadius(short * .13f, short * .13f)
                )
                drawRoundRect(
                    accent.copy(alpha = .80f), Offset(left, top), Size(width, height),
                    CornerRadius(short * .13f, short * .13f), style = stroke(.008f)
                )
                // Fingers, resting up on the forehead rather than on the eye.
                repeat(3) { finger ->
                    val x = left + width * (.28f + .22f * finger)
                    drawLine(
                        accent.copy(alpha = .45f),
                        Offset(x, top + height * .12f), Offset(x, top + height * .42f),
                        (short * .008f).coerceAtLeast(1f), StrokeCap.Round
                    )
                }
            }
        }

        StudioStimulus.WARM_COMPRESS -> {
            // Most of the cycle is heat on a closed lid; the last stretch is the sweep
            // that actually clears the gland, which is the part people skip.
            val warmth = Color(0xFFE8794A)
            val lid = Path().apply {
                moveTo(w * .20f, center.y + short * .02f)
                quadraticBezierTo(w * .50f, center.y + short * .18f, w * .80f, center.y + short * .02f)
            }
            drawPath(lid, text.copy(alpha = .70f), style = stroke(.011f))
            listOf(.32f, .50f, .68f).forEach { x ->
                drawLine(
                    text.copy(alpha = .35f),
                    Offset(w * x, center.y + short * (.09f + .05f * (1f - (x - .5f) * (x - .5f) * 16f))),
                    Offset(w * x, center.y + short * .19f),
                    (short * .008f).coerceAtLeast(1f), StrokeCap.Round
                )
            }
            if (t < .6f) {
                val pulse = .5f - .5f * cos(t / .6f * 4f * PI.toFloat())
                drawRoundRect(
                    warmth.copy(alpha = .20f + .20f * pulse),
                    Offset(w * .18f, center.y - short * .20f), Size(w * .64f, short * .20f),
                    CornerRadius(short * .06f, short * .06f)
                )
                listOf(.34f, .50f, .66f).forEachIndexed { i, x ->
                    val lift = short * (.06f + .05f * ((pulse + i * .33f) % 1f))
                    drawLine(
                        warmth.copy(alpha = .55f),
                        Offset(w * x, center.y - short * .22f),
                        Offset(w * x, center.y - short * .22f - lift),
                        (short * .012f).coerceAtLeast(1.5f), StrokeCap.Round
                    )
                }
            } else {
                val swept = (t - .6f) / .4f
                val from = w * .26f
                val at = Offset(from + w * .52f * swept, center.y - short * .06f)
                drawLine(
                    accent.copy(alpha = .30f), Offset(from, at.y), at,
                    (short * .05f).coerceAtLeast(3f), StrokeCap.Round
                )
                drawCircle(accent, short * .045f, at)
            }
        }

        StudioStimulus.ACUPRESSURE -> {
            // Four points around the bony rim, one at a time, pressed in small circles.
            val socket = Path().apply {
                moveTo(w * .22f, center.y)
                quadraticBezierTo(w * .50f, center.y - short * .20f, w * .78f, center.y)
                quadraticBezierTo(w * .50f, center.y + short * .20f, w * .22f, center.y)
                close()
            }
            drawPath(socket, text.copy(alpha = .35f), style = stroke(.008f))
            val points = listOf(.26f to .50f, .42f to .28f, .82f to .44f, .54f to .74f)
            val slot = t * points.size
            val current = slot.toInt().coerceAtMost(points.lastIndex)
            val within = slot - current
            val press = .5f - .5f * cos(within * 2f * PI.toFloat())
            points.forEachIndexed { i, (x, y) ->
                val at = Offset(w * x, center.y + (y - .5f) * short)
                if (i == current) {
                    drawCircle(accent.copy(alpha = .18f), r * (1.5f + .9f * press), at)
                    drawCircle(accent, r * .85f, at)
                    val circling = within * 2f * PI.toFloat()
                    drawCircle(
                        support, r * .34f,
                        at + Offset(cos(circling) * r * .6f, sin(circling) * r * .6f)
                    )
                } else {
                    drawCircle(support.copy(alpha = .35f), r * .42f, at)
                }
            }
        }

        StudioStimulus.EYE_ROM -> {
            // Eight stops round the clock. The hold at each extreme is the exercise, so
            // the target spends most of its slot parked and then moves on.
            val stops = 8
            val ring = short * .36f
            drawCircle(text.copy(alpha = .12f), ring, center, style = stroke(.005f))
            drawCircle(text.copy(alpha = .30f), r * .28f, center)
            fun stopAt(index: Float): Offset {
                val angle = index / stops * 2f * PI.toFloat() - PI.toFloat() / 2f
                return center + Offset(cos(angle) * ring, sin(angle) * ring)
            }
            repeat(stops) { k -> drawCircle(support.copy(alpha = .22f), r * .32f, stopAt(k.toFloat())) }
            val slot = t * stops
            val index = slot.toInt().coerceAtMost(stops - 1)
            val hold = .55f
            val moved = ((slot - index - hold) / (1f - hold)).coerceIn(0f, 1f)
            target(stopAt(index + moved * moved * (3f - 2f * moved)))
        }

        StudioStimulus.SCANNING -> {
            drawLine(support, Offset(w * .08f, h * .12f), Offset(w * .08f, h * .88f), (short * .016f).coerceAtLeast(2f))
            val points = listOf(.17f to .22f, .35f to .72f, .54f to .38f, .71f to .82f, .86f to .18f)
            val current = (t * points.size).toInt().coerceAtMost(points.lastIndex)
            points.forEachIndexed { i, (x, y) ->
                target(Offset(w * x, h * y), if (i == current) accent else support.copy(alpha = .55f), if (i == current) r else r * .7f)
            }
        }
    }
}

/** The instruction shown and spoken for [stimulus] at this point in its cycle. */
fun stimulusCue(stimulus: StudioStimulus, phase: Float): String = when (stimulus) {
    StudioStimulus.BLINK ->
        if (phase < .33f) "Close gently" else if (phase < .66f) "Gentle squeeze" else "Open and relax"
    StudioStimulus.BREAK_REMINDER -> "Look away from the screen"
    StudioStimulus.SACCADE, StudioStimulus.SACCADE_VERTICAL -> "Flick to the target, do not chase it"
    StudioStimulus.ANTI_SACCADE -> "Look to the OPPOSITE side"
    StudioStimulus.PURSUIT, StudioStimulus.HEMIFIELD_PURSUIT,
    StudioStimulus.PURSUIT_CIRCULAR, StudioStimulus.PURSUIT_FIGURE_EIGHT -> "Follow continuously"
    StudioStimulus.SCANNING -> "Anchor, then scan systematically"
    StudioStimulus.FIXATION -> "Hold steady on the centre"
    StudioStimulus.FOCUS_SHIFT -> if (phase < .5f) "Focus near" else "Focus far"
    StudioStimulus.NEAR_TARGET, StudioStimulus.VERGENCE_STEP, StudioStimulus.BROCK_STRING ->
        "Keep it single and clear"
    StudioStimulus.PALMING -> if (phase < .5f) "Breathe in, slowly" else "Breathe out"
    // Three steps, not two: the reheat is what the routine actually fails on, so it gets
    // called out rather than left in the instructions nobody re-reads mid-drill.
    StudioStimulus.WARM_COMPRESS -> when {
        phase < .5f -> "Hold it on your closed lids"
        phase < .65f -> "Still warm? Reheat it now"
        else -> "Sweep to the lashes: top down, bottom up"
    }
    StudioStimulus.ACUPRESSURE -> when ((phase * 4f).toInt().coerceAtMost(3)) {
        0 -> "Inner corner, small circles"
        1 -> "Along the brow"
        2 -> "Temple"
        else -> "Under the eye"
    }
    StudioStimulus.EYE_ROM -> "Reach the edge, then hold"
    else -> "Observe the target relationship"
}
