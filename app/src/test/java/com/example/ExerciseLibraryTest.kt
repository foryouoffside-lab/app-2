package com.example

import com.example.model.DigitalReproducibility
import com.example.model.EyeIssue
import com.example.model.EvidenceGrade
import com.example.model.GeneralUserStatus
import com.example.model.Practice
import com.example.model.StudioDrillRepository
import com.example.model.toProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The library is split by the complaint each exercise is offered for, and split again by
 * whether there is anything to run. Both splits decide what Train draws, so both have to
 * hold for every entry: a drill with no section never appears, and a habit that reaches
 * the player would start a timer over an exercise that has no cycle to time.
 */
class ExerciseLibraryTest {

    private val drills = StudioDrillRepository.drills

    @Test
    fun `every exercise lands in exactly one section`() {
        val grouped = drills.groupBy { it.issue }
        assertEquals("every drill must be listed once", drills.size, grouped.values.sumOf { it.size })
    }

    /**
     * A complaint may end up with nothing to offer -- amblyopia needs eye-separation
     * filters for every one of its drills -- and Train skips the heading when that
     * happens. What must never happen is a section emptying for any other reason.
     */
    @Test
    fun `a section is only ever empty because every drill in it needs equipment`() {
        val offered = drills.groupBy { it.issue }
        EyeIssue.entries.filter { offered[it].isNullOrEmpty() }.forEach { issue ->
            val all = StudioDrillRepository.allDrills.filter { it.issue == issue }
            assertTrue("$issue has no drills at all", all.isNotEmpty())
            assertTrue(
                "$issue lost its drills for a reason other than equipment",
                all.all { it.digitalReproducibility == DigitalReproducibility.EQUIPMENT_REQUIRED }
            )
        }
    }

    /** The point of the whole cut: nothing you cannot do reaches the library. */
    @Test
    fun `nothing needing equipment is offered`() {
        drills.forEach { drill ->
            assertTrue(
                "${drill.id} needs ${drill.equipment}, so it cannot be an option",
                drill.digitalReproducibility != DigitalReproducibility.EQUIPMENT_REQUIRED
            )
        }
    }

    /**
     * Train gives every drill name one line, shrinking it to a 12sp floor rather than
     * wrapping or ellipsising. Past about 40 characters even that floor overflows and the
     * name clips mid-word, which is how "Accommodation Demo" once shipped as
     * "Accommodation De". A long clinical name belongs in aliases, not on the card.
     */
    @Test
    fun `every drill name fits one row`() {
        drills.forEach { drill ->
            assertTrue(
                "${drill.id} has a ${drill.name.length}-character name and would clip: ${drill.name}",
                drill.name.length <= 40
            )
        }
    }

    @Test
    fun `ids are unique, because the list and the player both key on them`() {
        assertEquals(drills.size, drills.map { it.id }.toSet().size)
    }

    @Test
    fun `an exercise with nothing to run says what to do instead`() {
        val habits = drills.filter { it.practice == Practice.HABIT }
        assertTrue("the library has no habits at all", habits.isNotEmpty())
        habits.forEach { drill ->
            assertTrue(
                "${drill.id} is a habit, so its steps are the whole exercise and cannot be empty",
                drill.howTo.isNotEmpty()
            )
        }
    }

    @Test
    fun `every guided drill has a cycle the player can actually count`() {
        drills.filter { it.practice == Practice.GUIDED }.forEach { drill ->
            assertTrue("${drill.id} has no cycle length", drill.dose.cycleSeconds > 0)
            assertTrue("${drill.id} has no reps", drill.dose.reps > 0)
            // The player divides the planned length by the cycle to number the reps.
            val protocol = drill.toProtocol()
            assertEquals(
                "${drill.id} would show the wrong rep count",
                drill.dose.reps,
                protocol.totalSeconds / drill.dose.cycleSeconds
            )
        }
    }

    /**
     * The orthoptic instruments stay as records -- their evidence and safety text is the
     * honest answer to "can I do a barrel card at home" -- but they are not offered,
     * because a user cannot obtain one.
     */
    @Test
    fun `professional orthoptic tools are kept as records but never offered`() {
        val professionalIds = setOf("dot_card", "free_space_stereogram", "barrel_card")
        val professional = StudioDrillRepository.allDrills.filter { it.id in professionalIds }

        assertEquals(professionalIds, professional.map { it.id }.toSet())
        assertTrue(
            "an instrument nobody can buy must not reach the library",
            drills.none { it.id in professionalIds }
        )
        professional.forEach { drill ->
            assertEquals(Practice.GUIDED, drill.practice)
            assertEquals(GeneralUserStatus.CLINICAL_EQUIPMENT_REQUIRED, drill.generalUserStatus)
            assertEquals(EvidenceGrade.D, drill.evidenceGrade)
            assertTrue("${drill.id} needs real setup instructions", drill.howTo.isNotEmpty())
            assertTrue("${drill.id} needs traceable evidence", drill.sources.isNotEmpty())
            assertTrue("${drill.id} must say why a phone is not equivalent", drill.digitalValidity.isNotBlank())
        }
    }

    /**
     * The three consumer articles this library was reviewed against name the same small
     * set of practices in different words -- "zooming", "refocusing" and "focus change"
     * are one near/far task, not three exercises. Each article term must therefore resolve
     * to a drill we already have, so nobody is tempted to add a fourth card for it.
     */
    @Test
    fun `every exercise named in the source articles resolves to one drill`() {
        articleTerms.forEach { term ->
            val matches = StudioDrillRepository.allDrills.filter { drill ->
                (listOf(drill.name) + drill.aliases).any { normalise(it).contains(normalise(term)) }
            }
            assertTrue("\"$term\" is in the articles but maps to no drill", matches.isNotEmpty())
        }
    }

    /**
     * The anti-duplication rule itself. An alias naming two different drills means the
     * library is claiming one exercise is two, which is how a list of 24 turns into a
     * list of 30 that repeats itself.
     */
    @Test
    fun `no alias is claimed by two drills`() {
        val owners = mutableMapOf<String, MutableList<String>>()
        StudioDrillRepository.allDrills.forEach { drill ->
            drill.aliases.forEach { alias ->
                owners.getOrPut(normalise(alias)) { mutableListOf() }.add(drill.id)
            }
        }
        owners.filter { it.value.size > 1 }.forEach { (alias, ids) ->
            throw AssertionError("\"$alias\" is an alias of ${ids.joinToString(" and ")}")
        }
    }

    private fun normalise(text: String) =
        text.lowercase().replace('-', ' ').replace('/', ' ').replace("&", "and")
            .replace(Regex("""\s+"""), " ").trim()

    /** Every exercise named across the three reviewed articles. */
    private val articleTerms = listOf(
        // Dr Agarwal
        "Near & Far Focusing", "Figure of Eight", "Palming", "Blinking", "20-20-20 rule",
        "Zooming", "Refocusing", "Pencil Push-Ups", "Around the World", "Roll your eyes",
        // Kraff
        "Brock String", "Barrel card", "Near and far focus", "Figure eight",
        // Healthline
        "Focus change"
    )
}
