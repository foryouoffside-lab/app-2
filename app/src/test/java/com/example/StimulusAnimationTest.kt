package com.example

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.model.StudioStimulus
import com.example.ui.drill.StimulusPalette
import com.example.ui.drill.drawStimulus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Every stimulus has to actually move.
 *
 * This is the check for a class of bug rather than for one drill. The 20-20-20 break drew
 * a clock with its hands nailed on, the aperture drew a card that never opened, and the
 * fixation ring breathed off a progress value that a fixation target pins at zero: three
 * drills that ran their full length showing a still image, with nothing to fail. Two
 * frames from the same cycle, compared pixel for pixel, fail on all three.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class StimulusAnimationTest {

    private val palette = StimulusPalette(
        accent = Color(0xFFFFC53D),
        support = Color(0xFF3DBFB0),
        text = Color(0xFFF2F2F2),
        bg = Color(0xFF111111)
    )

    private fun frameAt(stimulus: StudioStimulus, phase: Float, active: Boolean = true): IntArray {
        val side = 220
        val bitmap = ImageBitmap(side, side)
        CanvasDrawScope().draw(
            Density(1f), LayoutDirection.Ltr, Canvas(bitmap),
            Size(side.toFloat(), side.toFloat())
        ) {
            drawStimulus(stimulus, phase, active, speed = 1f, palette = palette)
        }
        return bitmap.toPixelMap().buffer
    }

    @Test
    fun `every stimulus draws something different later in its cycle`() {
        StudioStimulus.entries.forEach { stimulus ->
            // Early against late. Every stimulus changes between these two: a saccade has
            // jumped, a lid has reopened, the compress has moved from warming to the sweep.
            assertFalse(
                "$stimulus draws the same frame all cycle -- the drill runs as a still image",
                frameAt(stimulus, .05f).contentEquals(frameAt(stimulus, .75f))
            )
        }
    }

    @Test
    fun `a paused drill holds one frame`() {
        // The other half of the contract: a stimulus draws from the phase it is handed and
        // nothing else, so a paused drill stops rather than drifting on the wall clock.
        StudioStimulus.entries.forEach { stimulus ->
            assertTrue(
                "$stimulus keeps moving while the drill is paused",
                frameAt(stimulus, .1f, active = false)
                    .contentEquals(frameAt(stimulus, .9f, active = false))
            )
        }
    }

}
