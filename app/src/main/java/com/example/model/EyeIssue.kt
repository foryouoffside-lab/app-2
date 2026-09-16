package com.example.model

/**
 * The problem a drill is meant to help with, named the way the person with the problem
 * would name it.
 *
 * This replaces the old free-text category. The two were never independent -- "Convergence
 * / Fusional Vergence" was already an answer to "what is this for" -- and a string could
 * not be grouped on without hoping every entry spelled it the same way. Sections in Train
 * are these, in this order, so the list reads from the everyday complaint down to the
 * clinical one.
 */
enum class EyeIssue(val label: String, val blurb: String) {
    DRY_AND_TIRED(
        "Dry, gritty, tired eyes",
        "Blink quality, tear film and lid care"
    ),
    SCREEN_STRAIN(
        "Screen strain",
        "Breaking up near work before it starts to ache"
    ),
    TENSION(
        "Tension and recovery",
        "Resting eyes that ache from holding one distance too long"
    ),
    FOCUS(
        "Focus won't switch",
        "Blur when your eyes move between near and far"
    ),
    TEAMING(
        "Double vision and eye teaming",
        "Getting both eyes onto the same target and keeping them there"
    ),
    TRACKING(
        "Tracking and reading",
        "Moving your eyes accurately across a page or a scene"
    ),
    LAZY_EYE(
        "Lazy eye and eye turn",
        "Amblyopia and strabismus work, done under an eye-care professional"
    ),
    NEURO(
        "After a brain injury",
        "Field loss, neglect and concussion recovery, done under a clinician"
    )
}

/**
 * How an exercise is actually practised.
 *
 * Not everything that helps eyes is a thing to watch on a phone. Cleaning your lids,
 * moving the monitor back and getting a child outdoors are exercises in the only sense
 * that matters -- something to do, repeatedly -- but running a timer over them would be
 * theatre. Those carry their instructions and nothing else.
 */
enum class Practice {
    /** Timed and cued in the player. */
    GUIDED,

    /** Nothing to run: the exercise is knowing what to do, and doing it away from the app. */
    HABIT
}
