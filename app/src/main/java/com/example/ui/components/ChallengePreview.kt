package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.ChallengeKind
import com.example.ui.theme.AppTheme
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** The sheet colour a check is actually presented on, so the thumbnail is not a lie. */
private val Paper = Color(0xFFF2F2F2)
private val Ink = Color(0xFF141414)

/**
 * A miniature of what the challenge puts on screen.
 *
 * The library used to front every row with a Material icon, so a palette glyph stood for
 * both the colour plates and the hue game and told you nothing about either. Drawing the
 * real figure small is the same trick the Drill Studio row already uses for its stimulus:
 * you recognise the task before you read its name.
 *
 * Static by choice. Thirteen animating canvases in one scrolling list buys jitter, and a
 * check is a thing you sit still for.
 */
@Composable
fun ChallengePreview(kind: ChallengeKind, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Canvas(modifier) {
        when (kind) {
            ChallengeKind.CENTRAL_GRID -> grid(colors.rose)
            ChallengeKind.NEAR_CLARITY -> landoltC()
            ChallengeKind.READING_CLARITY -> textLines()
            ChallengeKind.ASTIGMATISM_FAN -> fan()
            ChallengeKind.ISHIHARA_STYLE_PLATES -> plate()
            ChallengeKind.RED_GREEN_BALANCE -> duochrome()
            ChallengeKind.COVER_ALIGNMENT -> coverEyes(colors.surfaceElevated, colors.iris, colors.textHigh)
            ChallengeKind.NEAR_POINT_CONVERGENCE -> ruler(colors.surfaceElevated, colors.textMuted, colors.amber)
            ChallengeKind.CONTRAST_SPOTTING -> contrastQuad(colors.surfaceElevated, colors.textHigh)
            ChallengeKind.COLOR_DISCRIMINATION -> hueQuad(colors.surfaceElevated)
            ChallengeKind.VISUAL_REACTION -> reactionDot(colors.surfaceElevated, colors.amber, colors.border)
            ChallengeKind.PERIPHERAL_AWARENESS -> peripheralCue(colors.surfaceElevated, colors.textHigh, colors.amber)
            ChallengeKind.AMBLYOPIA_PLAY -> symbolGrid(colors.surfaceElevated, colors.border, colors.amber)
        }
    }
}

private fun DrawScope.grid(centreDot: Color) {
    drawRect(Paper)
    val step = size.minDimension / 6f
    for (i in 0..6) {
        val p = i * step
        drawLine(Color(0xFF6B6B6B), Offset(p, 0f), Offset(p, size.height), 1f)
        drawLine(Color(0xFF6B6B6B), Offset(0f, p), Offset(size.width, p), 1f)
    }
    drawCircle(centreDot, radius = step * 0.22f, center = center)
}

private fun DrawScope.landoltC() {
    drawRect(Paper)
    val d = size.minDimension * 0.52f
    drawArc(
        Ink, 36f, 288f, false,
        topLeft = Offset(center.x - d / 2, center.y - d / 2),
        size = Size(d, d),
        style = Stroke(d / 5f, cap = StrokeCap.Butt)
    )
}

private fun DrawScope.textLines() {
    drawRect(Paper)
    val left = size.width * 0.18f
    var y = size.height * 0.26f
    listOf(0.64f to 4f, 0.56f to 3f, 0.46f to 2.2f, 0.34f to 1.4f).forEach { (widthFraction, thickness) ->
        drawLine(Ink, Offset(left, y), Offset(left + size.width * widthFraction, y), thickness, StrokeCap.Round)
        y += size.height * 0.16f
    }
}

private fun DrawScope.fan() {
    drawRect(Paper)
    val radius = size.minDimension * 0.40f
    repeat(6) { index ->
        val angle = Math.toRadians(index * 30.0)
        val offset = Offset(cos(angle).toFloat() * radius, sin(angle).toFloat() * radius)
        drawLine(Ink, center - offset, center + offset, 1.6f)
    }
    drawCircle(Paper, radius = size.minDimension * 0.06f, center = center)
    drawCircle(Ink, radius = size.minDimension * 0.025f, center = center)
}

private fun DrawScope.plate() {
    drawRect(Color(0xFFF2E7C9))
    val random = Random(7)
    val radius = size.minDimension / 2f
    // The digit is not legible at thumbnail size, so this shows the medium honestly: a
    // dot field in two confusable hues, not a number nobody could read here.
    repeat(90) {
        val x = random.nextFloat()
        val y = random.nextFloat()
        val dx = x - .5f
        val dy = y - .5f
        if (dx * dx + dy * dy > .23f) return@repeat
        val colour = if (random.nextFloat() < .38f) Color(0xFFE56F4A) else Color(0xFF74A85B)
        drawCircle(
            colour,
            radius = radius * (random.nextFloat() * .05f + .045f),
            center = Offset(x * size.width, y * size.height)
        )
    }
}

private fun DrawScope.duochrome() {
    drawRect(Color(0xFFD94A4A), size = Size(size.width / 2f, size.height))
    drawRect(Color(0xFF48A868), topLeft = Offset(size.width / 2f, 0f), size = Size(size.width / 2f, size.height))
    val dot = size.minDimension * 0.09f
    listOf(0.26f, 0.74f).forEach { x ->
        listOf(0.36f, 0.64f).forEach { y ->
            drawCircle(Color(0xFF202020), dot, Offset(size.width * x, size.height * y))
        }
    }
}

private fun DrawScope.coverEyes(background: Color, iris: Color, cover: Color) {
    drawRect(background)
    val r = size.minDimension * 0.15f
    val y = size.height * 0.5f
    drawCircle(Color.White, r, Offset(size.width * 0.31f, y))
    drawCircle(iris, r * 0.52f, Offset(size.width * 0.31f, y))
    drawCircle(Color.White, r, Offset(size.width * 0.69f, y))
    drawCircle(iris, r * 0.52f, Offset(size.width * 0.69f, y))
    drawRoundRect(
        cover,
        topLeft = Offset(size.width * 0.52f, y - r * 1.25f),
        size = Size(size.width * 0.34f, r * 2.5f),
        cornerRadius = CornerRadius(r * 0.4f)
    )
}

private fun DrawScope.ruler(background: Color, tick: Color, target: Color) {
    drawRect(background)
    val y = size.height * 0.62f
    drawLine(tick, Offset(size.width * 0.12f, y), Offset(size.width * 0.88f, y), 2f, StrokeCap.Round)
    repeat(5) { index ->
        val x = size.width * (0.16f + index * 0.17f)
        drawLine(tick, Offset(x, y), Offset(x, y - size.height * 0.12f), 2f)
    }
    drawCircle(target, size.minDimension * 0.11f, Offset(size.width * 0.28f, size.height * 0.3f))
}

private fun DrawScope.contrastQuad(background: Color, mark: Color) {
    drawRect(background)
    val alphas = listOf(0.58f, 0.17f, 0.17f, 0.17f)
    quadCentres().forEachIndexed { index, centre ->
        drawCircle(mark.copy(alpha = alphas[index]), size.minDimension * 0.15f, centre)
    }
}

private fun DrawScope.hueQuad(background: Color) {
    drawRect(background)
    val base = Color.hsv(176f, .58f, .86f)
    val odd = Color.hsv(204f, .58f, .86f)
    quadCentres().forEachIndexed { index, centre ->
        drawCircle(if (index == 1) odd else base, size.minDimension * 0.15f, centre)
    }
}

private fun DrawScope.quadCentres(): List<Offset> = listOf(0.31f, 0.69f).flatMap { y ->
    listOf(0.31f, 0.69f).map { x -> Offset(size.width * x, size.height * y) }
}

private fun DrawScope.reactionDot(background: Color, target: Color, ring: Color) {
    drawRect(background)
    drawCircle(ring, size.minDimension * 0.30f, center, style = Stroke(1.5f))
    drawCircle(target, size.minDimension * 0.17f, Offset(size.width * 0.70f, size.height * 0.31f))
}

private fun DrawScope.peripheralCue(background: Color, cross: Color, cue: Color) {
    drawRect(background)
    val arm = size.minDimension * 0.10f
    drawLine(cross, Offset(center.x - arm, center.y), Offset(center.x + arm, center.y), 2f)
    drawLine(cross, Offset(center.x, center.y - arm), Offset(center.x, center.y + arm), 2f)
    drawCircle(cue, size.minDimension * 0.10f, Offset(size.width * 0.85f, center.y))
}

private fun DrawScope.symbolGrid(background: Color, tile: Color, found: Color) {
    drawRect(background)
    val cell = size.minDimension / 3.6f
    val gap = (size.width - cell * 3) / 4f
    repeat(3) { row ->
        repeat(3) { column ->
            val hit = row == 1 && column == 2
            drawRoundRect(
                if (hit) found else tile,
                topLeft = Offset(gap + column * (cell + gap), gap + row * (cell + gap)),
                size = Size(cell, cell),
                cornerRadius = CornerRadius(cell * 0.3f)
            )
        }
    }
}
