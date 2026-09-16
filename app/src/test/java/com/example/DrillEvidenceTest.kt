package com.example

import com.example.model.EvidenceGrade
import com.example.model.ProtocolsRepository
import com.example.model.StudioDrillRepository
import com.example.model.toProtocol
import com.example.ui.drill.BLINK_CYCLE_SECONDS
import com.example.ui.drill.repRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every drill in Train is now playable on its own, so two things can silently rot: the
 * evidence record a drill points at, and the dose it actually runs. Both are checked here.
 */
class DrillEvidenceTest {

    @Test
    fun `every protocol evidence id resolves to a studio record`() {
        ProtocolsRepository.forAge(30).forEach { protocol ->
            protocol.evidenceId?.let {
                assertNotNull("${protocol.id} points at a missing record: $it", StudioDrillRepository.byId(it))
            }
        }
    }

    @Test
    fun `blink reset runs the dose its evidence is graded for`() {
        // Wolffsohn et al. 2025 (PMID 40467388): 15 close-squeeze-open cycles, each of the
        // three steps held 2s, 3x/day for 2 weeks. The shipped drill must be that cycle.
        val blink = ProtocolsRepository.forAge(30).single { it.id == "blink_reset" }
        assertEquals("complete_blink_squeeze", blink.evidenceId)
        assertEquals(6, BLINK_CYCLE_SECONDS)

        val dose = StudioDrillRepository.byId("complete_blink_squeeze")!!.dose
        assertTrue("the blink dose is the one place a trial actually set one", dose.fromEvidence)
        assertEquals(6, dose.cycleSeconds)
        assertEquals(15, dose.reps)
        assertEquals(dose.totalSeconds, blink.totalSeconds)
    }

    @Test
    fun `the blink record carries its sources and states what it does not prove`() {
        val drill = StudioDrillRepository.byId("complete_blink_squeeze")!!
        assertEquals(EvidenceGrade.B, drill.evidenceGrade)
        // A grade with no paper behind it is a claim, not evidence.
        assertTrue(drill.sources.isNotEmpty())
        drill.sources.forEach { assertTrue(it.citation, "PMID" in it.citation) }
        assertTrue(drill.unprovenClaims.isNotEmpty())
        // The dose-finding trial's gains washed out two weeks after stopping; the record
        // must not let that be read as a lasting benefit.
        assertTrue(drill.evidenceLimitation.contains("returned to baseline"))
    }

    @Test
    fun `every drill is playable at a dose the player can actually run`() {
        StudioDrillRepository.drills.forEach { drill ->
            val dose = drill.dose
            assertTrue("${drill.id}: cycle must be positive", dose.cycleSeconds > 0)
            assertTrue("${drill.id}: reps must be positive", dose.reps > 0)

            val protocol = drill.toProtocol()
            assertEquals("${drill.id}: protocol length must match the dose", dose.totalSeconds, protocol.totalSeconds)
            assertEquals("${drill.id}: must point back at its own record", drill.id, protocol.evidenceId)
            assertEquals(1, protocol.phases.size)

            // What the drill advertises has to be what it starts at. The player seeds its
            // stepper from duration/cycle and clamps it, so a dose outside the stepper's
            // range would quietly run a different length than the row and the sheet claim.
            val seeded = (protocol.phases[0].durationSeconds / dose.cycleSeconds)
                .coerceIn(repRange(dose.reps))
            assertEquals("${drill.id}: the player would not start at the stated dose", dose.reps, seeded)
        }
    }

    @Test
    fun `a dose is only called evidence when a trial actually set it`() {
        StudioDrillRepository.drills.forEach { drill ->
            if (!drill.dose.fromEvidence) {
                // Anything else is the app's own number and has to admit it, because the
                // evidence grade sitting next to it invites the opposite reading.
                assertTrue(
                    "${drill.id}: an app-chosen dose must say so",
                    drill.dose.basis.startsWith("No trial sets a per-rep dose")
                )
            } else {
                // A trial-set dose has to name the trial it came from.
                assertTrue("${drill.id}: a trial dose must cite one", Regex("\\d{4}").containsMatchIn(drill.dose.basis))
            }
        }
    }

    @Test
    fun `drill ids are unique so the list and the lookup cannot disagree`() {
        val ids = StudioDrillRepository.drills.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
