package com.example.model

import com.example.R

enum class EvidenceGrade { A, B, C, D, E }

enum class DigitalReproducibility { FULL, PARTIAL, EQUIPMENT_REQUIRED }

enum class GeneralUserStatus {
    GENERAL_TRAINING,
    GENERAL_DEMONSTRATION,
    CONDITION_SPECIFIC,
    CLINICIAN_SUPERVISED,
    CLINICAL_EQUIPMENT_REQUIRED,
    RESEARCH_ONLY,
    EXCLUDED
}

enum class ProfessionalReviewStatus {
    NOT_REVIEWED,
    RESEARCH_REVIEWED,
    PROFESSIONAL_REVIEW_REQUIRED,
    PROFESSIONALLY_REVIEWED,
    APPROVED_FOR_GENERAL_TRAINING,
    CLINICAL_ONLY
}

/**
 * Where the user's eyes are while the drill runs, which decides how it is guided.
 *
 * Derived from the stimulus rather than stored per drill: it is a property of what the
 * drill asks you to look at, so the two cannot drift apart.
 */
enum class DrillGuidance {
    /** Eyes on the display. The animation is the cue, and talking over it only distracts. */
    VISUAL,

    /** Eyes shut or off the screen. Nothing drawn can reach the user, so the cue has to
     *  be spoken, sounded and felt instead. */
    NON_VISUAL
}

enum class StudioStimulus {
    BLINK,
    BREAK_REMINDER,
    NEAR_TARGET,
    BROCK_STRING,
    DISPARITY,
    STEREOGRAM,
    APERTURE,
    FOCUS_SHIFT,
    SACCADE,
    SACCADE_VERTICAL,
    ANTI_SACCADE,
    PURSUIT,
    PURSUIT_CIRCULAR,
    PURSUIT_FIGURE_EIGHT,
    VERGENCE_STEP,
    FIXATION,
    SCANNING,
    HEMIFIELD_PURSUIT,
    DICHOPTIC,

    // Relief and lid care. Every one of these is done with the eyes closed or covered, so
    // what the canvas draws is a picture of the step rather than something to look at.
    PALMING,
    WARM_COMPRESS,
    ACUPRESSURE,

    /** Eyes open, working through the full range of gaze and holding at each extreme. */
    EYE_ROM
}

/**
 * How much of a drill to do in one sitting: one cycle of [cycleSeconds], repeated [reps] times.
 *
 * [fromEvidence] is the honest part. It is true only where a published protocol actually
 * set this cycle and this count. Everywhere else the numbers are a default the app picked
 * so the drill can run at all, and [basis] says so in the words the player shows. A grade
 * on the evidence is not a grade on the dose.
 */
data class DrillDose(
    val cycleSeconds: Int,
    val reps: Int,
    val basis: String,
    val fromEvidence: Boolean
) {
    val totalSeconds: Int get() = cycleSeconds * reps
}

/** A dose the app chose because no trial set one. Never presented as evidence. */
private fun illustrative(cycleSeconds: Int, reps: Int, note: String) =
    DrillDose(cycleSeconds, reps, "No trial sets a per-rep dose for this drill. " + note, false)

data class DrillSource(
    val title: String,
    val citation: String,
    val url: String
)

data class StudioDrill(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    /** The complaint this drill is offered for. Train groups the library by it. */
    val issue: EyeIssue,
    val targetSystem: String,
    val targetFunction: String,
    val clinicalPurpose: String,
    val clinicalUse: Boolean,
    val evidenceGrade: EvidenceGrade,
    val evidenceType: String,
    val studiedPopulation: String,
    val condition: String,
    val evidenceFor: String,
    val evidenceLimitation: String,
    val provenOutcomes: List<String>,
    val unprovenClaims: List<String>,
    val digitalReproducibility: DigitalReproducibility,
    val equipment: String,
    val digitalValidity: String,
    val whatYouDo: String,
    val whatItTrains: String,
    val contraindications: String,
    val safetyLevel: String,
    val generalUserStatus: GeneralUserStatus,
    val reviewStatus: ProfessionalReviewStatus,
    val stimulus: StudioStimulus,
    val dose: DrillDose,
    val sources: List<DrillSource>,
    /**
     * How this one is practised. A [Practice.HABIT] never reaches the player: it has no
     * cycle to run, so Train opens its instructions instead of starting a timer.
     */
    val practice: Practice = Practice.GUIDED,
    /**
     * The steps, in order, for an exercise the app cannot cue frame by frame.
     *
     * Required for a [Practice.HABIT], which is nothing but its steps, and allowed on a
     * guided drill whose set-up has to happen before the timer is any use.
     */
    val howTo: List<String> = emptyList(),
    /**
     * A demo image per [howTo] step, same index, null where none exists yet. Most drills
     * have no images until they are illustrated one at a time.
     */
    val howToImages: List<Int?> = emptyList(),
    /**
     * A looping demo clip per [howTo] step, same index as [howToImages]. Takes priority
     * over the image at that index when both exist.
     */
    val howToVideos: List<Int?> = emptyList()
)

private val citt = DrillSource(
    "Randomized clinical trial of treatments for symptomatic convergence insufficiency in children",
    "CITT Study Group. Arch Ophthalmol. 2008;126:1336-1349. PMID 18852411.",
    "https://pubmed.ncbi.nlm.nih.gov/18852411/"
)
private val cittArt = DrillSource(
    "Treatment of symptomatic convergence insufficiency in CITT-ART",
    "CITT-ART Investigator Group. Optom Vis Sci. 2019;96:825-835. PMID 31651593.",
    "https://pubmed.ncbi.nlm.nih.gov/31651593/"
)
private val aaoCi = DrillSource(
    "Home- and office-based vergence and accommodative therapies for convergence insufficiency",
    "American Academy of Ophthalmology report. Ophthalmology. 2021. PMID 34172337.",
    "https://pubmed.ncbi.nlm.nih.gov/34172337/"
)
private val ciMeta = DrillSource(
    "Interventions for convergence insufficiency: a network meta-analysis",
    "Cochrane Database Syst Rev. 2020. PMID 33263359.",
    "https://pubmed.ncbi.nlm.nih.gov/33263359/"
)
private val blinkRct = DrillSource(
    "Optimisation of blinking exercises for dry eye disease",
    "Wolffsohn et al. Cont Lens Anterior Eye. 2025;48(5):102453. PMID 40467388.",
    "https://pubmed.ncbi.nlm.nih.gov/40467388/"
)
private val blinkParameters = DrillSource(
    "Effects of blinking exercises on palpebral fissure height and tear film parameters",
    "Arita et al. Ocul Surf. 2025;36:237-243. PMID 39920919.",
    "https://pubmed.ncbi.nlm.nih.gov/39920919/"
)
private val blinkApp = DrillSource(
    "A smartphone-based blink training application for alleviating dry eye signs and symptoms",
    "Xu et al. npj Digit Med. 2025;8:703. PMID 41266739.",
    "https://pubmed.ncbi.nlm.nih.gov/41266739/"
)
// Sources added for the lid-care and habit entries. Where the exact PubMed id was not
// verifiable at the time of writing, the link is the search that finds the paper rather
// than a guessed identifier: a wrong PMID points confidently at the wrong study.
private val dews2 = DrillSource(
    "TFOS DEWS II Management and Therapy Report",
    "Jones L et al. Ocul Surf. 2017;15:575-628. PMID 28736343.",
    "https://pubmed.ncbi.nlm.nih.gov/28736343/"
)
private val outdoorTrial = DrillSource(
    "Effect of time spent outdoors at school on the development of myopia among children in China",
    "He M et al. JAMA. 2015;314:1142-1148. PMID 26372583.",
    "https://pubmed.ncbi.nlm.nih.gov/26372583/"
)
private val digitalEnvironment = DrillSource(
    "TFOS Lifestyle: Impact of the digital environment on the ocular surface",
    "Wolffsohn JS et al. Ocul Surf. 2023;28:213-252. PMID 37062428.",
    "https://pubmed.ncbi.nlm.nih.gov/37062428/"
)
private val opticalCorrectionReview = DrillSource(
    "Optical correction of refractive error for computer users",
    "Heus P et al. Cochrane Database Syst Rev. 2018. PMID 29633784.",
    "https://pubmed.ncbi.nlm.nih.gov/29633784/"
)
private val aaoScreens = DrillSource(
    "Computers, digital devices and eye strain",
    "American Academy of Ophthalmology, EyeSmart patient guidance.",
    "https://www.aao.org/eye-health/tips-prevention/computer-usage"
)
private val breakStudy = DrillSource(
    "The effects of breaks on digital eye strain, dry eye and binocular vision",
    "Talens-Estarelles et al. Contact Lens Anterior Eye. 2023. PMID 35963776.",
    "https://pubmed.ncbi.nlm.nih.gov/35963776/"
)
private val accommodativeTrial = DrillSource(
    "Treatment of accommodative dysfunction in children",
    "Scheiman et al. Optom Vis Sci. 2011;88:1343-1352. PMID 21873922.",
    "https://pubmed.ncbi.nlm.nih.gov/21873922/"
)
private val saccadeStudy = DrillSource(
    "Impact of task-specific training on saccadic eye movement performance",
    "Joiner et al. J Neurophysiol. 2019. PMID 31461366.",
    "https://pubmed.ncbi.nlm.nih.gov/31461366/"
)
private val antiSaccade = DrillSource(
    "Look away: the anti-saccade task and the voluntary control of eye movement",
    "Munoz DP, Everling S. Nat Rev Neurosci. 2004;5:218-228. PMID 14976521.",
    "https://pubmed.ncbi.nlm.nih.gov/14976521/"
)
private val strokeMeta = DrillSource(
    "Rehabilitative interventions for neglect and hemianopia poststroke",
    "Liu et al. Arch Phys Med Rehabil. 2019;100:956-979. PMID 31030733.",
    "https://pubmed.ncbi.nlm.nih.gov/31030733/"
)
private val searchTrial = DrillSource(
    "Scanning Eye Training as a Rehabilitation Choice for Hemianopia after stroke",
    "Rowe et al. Int J Stroke. 2025;20:968-976. PMID 40083185.",
    "https://pubmed.ncbi.nlm.nih.gov/40083185/"
)
private val vestibularCpg = DrillSource(
    "Vestibular Rehabilitation for Peripheral Vestibular Hypofunction: An Updated Clinical Practice Guideline",
    "Hall et al. J Neurol Phys Ther. 2022;46:118-177. PMID 34864777.",
    "https://pubmed.ncbi.nlm.nih.gov/34864777/"
)
private val pursuitReview = DrillSource(
    "Pursuit interventions in spatial neglect following stroke",
    "Spering et al. Neuropsychol Rev. 2015. PMID 26280103.",
    "https://pubmed.ncbi.nlm.nih.gov/26280103/"
)
private val amblyopiaReview = DrillSource(
    "Binocular treatment for amblyopia: a systematic review",
    "Tsani et al. Int Ophthalmol. 2024;44:362. PMID 39222269.",
    "https://pubmed.ncbi.nlm.nih.gov/39222269/"
)
private val amblyopiaRct = DrillSource(
    "Binocular iPad game vs patching for treatment of amblyopia",
    "Kelly et al. JAMA Ophthalmol. 2016;134:1402-1408. PMID 27832248.",
    "https://pubmed.ncbi.nlm.nih.gov/27832248/"
)
private val ixtTrial = DrillSource(
    "Office-based vergence and anti-suppression therapy in intermittent exotropia",
    "Randomised clinical trial. PMID 39513698.",
    "https://pubmed.ncbi.nlm.nih.gov/39513698/"
)
private val concussTrial = DrillSource(
    "CONCUSS randomized clinical trial",
    "Office-based vergence/accommodative therapy for concussion-related CI. PMID 41033748.",
    "https://pubmed.ncbi.nlm.nih.gov/41033748/"
)
private val obvatPilot = DrillSource(
    "Virtual reality-based vision therapy versus office-based vergence/accommodative therapy",
    "Li S et al. BMC Ophthalmol. 2022;22:182. PMID 35448970.",
    "https://pubmed.ncbi.nlm.nih.gov/35448970/"
)
private val nhsOrthopticExercises = DrillSource(
    "Orthoptic exercises: dot card and near/distance stereograms",
    "Frimley Health NHS Foundation Trust. Clinician-reviewed patient guidance; approved 2024.",
    "https://www.fhft.nhs.uk/patients-and-visitors/patient-information-library/orthoptic-exercises/submit/7561"
)
private val nhsDotCard = DrillSource(
    "How to carry out dot-card exercises",
    "United Lincolnshire Hospitals NHS Trust. Orthoptic patient guidance.",
    "https://www.ulh.nhs.uk/patients/patient-information-library/how-to-carry-out-your-dot-card-exercises/"
)

/**
 * Evidence-screened Studio inventory. A grade applies to the exact claim in [evidenceFor],
 * not to every possible use of the named procedure. Protocol components remain grade D
 * when the package is supported but the individual component has not been isolated.
 */
object StudioDrillRepository {
    /** Every researched record, including the ones nobody can do at home. Kept whole so
     *  the evidence and the safety text survive; [drills] is what the app offers. */
    val allDrills = listOf(
        StudioDrill(
            id = "complete_blink_squeeze", name = "Complete Blink + Gentle Squeeze",
            aliases = listOf("Blinking", "blink-squeeze-open"), issue = EyeIssue.DRY_AND_TIRED,
            targetSystem = "Ocular surface", targetFunction = "Blink completeness and tear-film distribution",
            clinicalPurpose = "Behavioral support for people with dry-eye symptoms and incomplete blinking.",
            clinicalUse = true, evidenceGrade = EvidenceGrade.B,
            evidenceType = "One randomised dose-finding trial plus two randomised controlled trials",
            studiedPopulation = "Adults with dry-eye disease (126 dose-finding, 100 randomised, 40 app-cued)",
            condition = "Dry eye / incomplete blink",
            evidenceFor = "At the optimised dose — 15 close-squeeze-open cycles, three times a day for two weeks — symptom severity and frequency, incomplete blinks and conjunctival staining all fell. Two separate randomised trials also measured longer tear break-up time.",
            evidenceLimitation = "In the dose-finding trial the gains returned to baseline two weeks after stopping, and tear break-up time, blink rate and tear meniscus height did not move at all. The two trials that did shift tear-film stability paired blinking with artificial tears or an all-day on-screen cue, so neither isolates the exercise. Not a substitute for assessment of persistent dry eye.",
            provenOutcomes = listOf("Blink completeness", "Dry-eye symptom scores", "Conjunctival staining", "Tear break-up time (paired-intervention trials only)"),
            unprovenClaims = listOf("Cures dry eye", "Prevents eye disease", "Benefit that lasts after you stop"),
            digitalReproducibility = DigitalReproducibility.FULL, equipment = "None",
            digitalValidity = "The screen can reproduce the timed behavioral cue; it cannot verify blink quality.",
            whatYouDo = "Follow the cue: close gently, add a brief gentle squeeze, then open and relax.",
            whatItTrains = "A complete voluntary blink pattern.",
            contraindications = "Stop for pain or new visual disturbance. Persistent symptoms need eye-care assessment.",
            safetyLevel = "SAFE GENERAL DEMONSTRATION", generalUserStatus = GeneralUserStatus.GENERAL_TRAINING,
            reviewStatus = ProfessionalReviewStatus.RESEARCH_REVIEWED, stimulus = StudioStimulus.BLINK,
            dose = DrillDose(6, 15, "The trial optimum: 15 close-squeeze-open cycles with each step held 2 seconds, three times a day for two weeks (Wolffsohn 2025).", true),
            sources = listOf(blinkRct, blinkParameters, blinkApp)
        ),
        StudioDrill(
            id = "screen_break_20_20_20", name = "20-20-20 Break Reminder",
            aliases = listOf("20-20-20 rule"),
            issue = EyeIssue.SCREEN_STRAIN, targetSystem = "Ocular surface and accommodative behavior",
            targetFunction = "Interrupt sustained near-screen viewing", clinicalPurpose = "Symptom-management reminder during screen use.",
            clinicalUse = true, evidenceGrade = EvidenceGrade.C, evidenceType = "Small prospective intervention study",
            studiedPopulation = "29 symptomatic computer users", condition = "Digital eye strain",
            evidenceFor = "Short-term reduction in reported digital-eye-strain and dry-eye symptoms.",
            evidenceLimitation = "No control group; most ocular-surface and binocular signs did not change; benefit did not persist after stopping.",
            provenOutcomes = listOf("Short-term symptom scores"), unprovenClaims = listOf("Improves eyesight", "Changes refractive error"),
            digitalReproducibility = DigitalReproducibility.FULL, equipment = "A distant real-world target",
            digitalValidity = "The app can time a break, but the user must look away from the screen.",
            whatYouDo = "At the reminder, look at a comfortably distant object and blink normally.",
            whatItTrains = "Break-taking behavior, not an ocular muscle treatment.", contraindications = "None specific; seek care for persistent or severe symptoms.",
            safetyLevel = "SAFE GENERAL DEMONSTRATION", generalUserStatus = GeneralUserStatus.GENERAL_TRAINING,
            reviewStatus = ProfessionalReviewStatus.RESEARCH_REVIEWED, stimulus = StudioStimulus.BREAK_REMINDER,
            dose = DrillDose(20, 1, "The rule as tested: a 20-second break every 20 minutes, looking about 20 feet away (Talens-Estarelles 2023). One break is the whole dose.", true),
            sources = listOf(breakStudy),
            howTo = listOf(
                "Pick your far target before you start: something about twenty feet away, six metres. Out of a window is ideal, or the far end of the room.",
                "Twenty feet is further than most rooms. If the far wall is closer than that, use the view outside rather than something across the desk.",
                "Put the phone down and look at it properly for the full twenty seconds. Glancing up and back does nothing.",
                "Blink normally while you look. Screen work halves your blink rate, and this is the moment to catch it up.",
                "Then set it going again in twenty minutes. The interval is the part that works."
            )
        ),
        ciComponent("pencil_push_up", "Pencil / Near Target Push-Up", StudioStimulus.NEAR_TARGET,
            "A physical near target", "Home push-ups were less effective than office therapy and evidence for home therapy remains insufficient.", EvidenceGrade.C,
            illustrative(10, 12, "CITT prescribed 15 minutes a day, 5 days a week for 12 weeks across a set of procedures, and never a count for this one. Home push-ups also underperformed office therapy."),
            howTo = listOf(
                "You need a real object, not the screen: a pencil with a letter or a small mark on the side works best. Anything with fine detail you can tell is sharp will do.",
                "Hold it upright at arm's length, level with the bridge of your nose, and look at that one mark.",
                "Move it slowly towards your nose, keeping the mark single and clear the whole way in. Slowly -- a couple of centimetres a second.",
                "The instant it doubles or blurs, stop and hold there. That point is the whole exercise; do not push through it.",
                "Move it back out until it is single and clear again, then start the next push-up. Stop for good if you get a headache or the doubling stops clearing."
            ),
            howToImages = listOf(
                R.drawable.howto_pencil_object,
                R.drawable.howto_pencil_hold,
                R.drawable.howto_pencil_eyes,
                R.drawable.howto_pencil_diplopia,
                R.drawable.howto_pencil_hold
            ),
            // Step 3 gets the real convergence clip; PrepSteps prefers video over image
            // when both exist, so howto_pencil_eyes.png is the fallback if it fails to load.
            howToVideos = listOf(null, null, R.raw.howto_pencil_convergence, null, null),
            aliases = listOf("Pencil push-ups", "Zooming", "Thumb tromboning", "Pen convergence")),
        ciComponent("brock_string", "Brock String", StudioStimulus.BROCK_STRING,
            "Physical string and fixation beads", "A clinically used component of multicomponent therapy; independent efficacy has not been established.", EvidenceGrade.D,
            illustrative(12, 10, "CITT prescribed 15 minutes a day, 5 days a week for 12 weeks across a set of procedures, and never a count for this one. Home push-ups also underperformed office therapy."),
            howTo = listOf(
                "You need about three feet of string and three beads on it. No beads? Tie three coloured knots, or thread on three buttons.",
                "Space them out: one close, about a hand's width from your nose, one at arm's length, one at the far end.",
                "Tie the far end to a door handle and hold the near end against the bridge of your nose, so the string runs straight out from between your eyes.",
                "Look at one bead at a time. Done right you see two strings crossing exactly at that bead, in an X.",
                "Move your gaze bead by bead and watch where the X sits. If it lands in front of or behind the bead, that is the thing being worked on -- not a mistake to force."
            ),
            howToImages = listOf(
                R.drawable.howto_brock_string_setup,
                R.drawable.howto_brock_string_setup,
                R.drawable.howto_brock_string_hold,
                R.drawable.howto_brock_string_near_x,
                R.drawable.howto_brock_string_mid_x
            ),
            // Step 5 gets the crossing-point-sliding clip; PrepSteps prefers video over
            // image when both exist, so howto_brock_string_mid_x.png is the fallback.
            howToVideos = listOf(null, null, null, null, R.raw.howto_brock_string_crossing)),
        ciComponent(
            "dot_card", "Dot-Card Convergence", StudioStimulus.VERGENCE_STEP,
            "A clinician-supplied dot card and your prescribed near correction",
            "Used by orthoptic services for diagnosed convergence weakness. The package and clinical practice are supported, but this card has not been isolated in the strongest trials.",
            EvidenceGrade.D,
            illustrative(5, 24, "NHS orthoptic guidance commonly uses no more than 2-3 minutes per session, several times daily, with the exact schedule written by the orthoptist."),
            equipmentRequired = true,
            howTo = listOf(
                "Use a real dot card supplied or approved by your orthoptist. The phone animation is only an explanation and cannot create the printed alignment cues.",
                "Wear the reading glasses or prism your orthoptist told you to use. Hold the near end of the card at the tip of your nose, angled slightly down.",
                "Start with the farthest dot. Keep that dot single and clear; the two printed lines should meet at the dot.",
                "Move one dot nearer at a time. Hold each selected dot single for about five seconds before moving on. The other dots and lines appearing double is part of the task.",
                "Work back toward the far end, then relax by looking into the distance. Never exceed the duration or frequency written by your orthoptist.",
                "Stop if double vision does not clear, your eyes remain crossed, or you develop marked headache, nausea, dizziness or pain."
            ),
            howToImages = listOf(
                R.drawable.howto_dot_card_setup,
                R.drawable.howto_dot_card_hold,
                R.drawable.howto_dot_card_far,
                R.drawable.howto_dot_card_near,
                R.drawable.howto_dot_card_hold
            ),
            // Step 6 is a pure safety warning with no new visual to teach, so it's left
            // without an image -- PrepSteps falls back to a numbered placeholder there.
            aliases = listOf("Convergence dot card", "Orthoptic dot card"),
            additionalSources = listOf(nhsOrthopticExercises, nhsDotCard)
        ),
        ciComponent(
            "free_space_stereogram", "Free-Space Fusion Stereogram", StudioStimulus.STEREOGRAM,
            "A clinician-supplied near/distance stereogram card and a fixation target",
            "Cat, bucket and related fusion cards are used by orthoptic services, but near and distance versions train opposite vergence directions and must be prescribed correctly.",
            EvidenceGrade.D,
            illustrative(10, 12, "Published NHS instructions leave dose to the orthoptist; examples use a few minutes at a time followed by equal relaxation. This two-minute run is an educational pace, not a prescription."),
            equipmentRequired = true,
            howTo = listOf(
                "Use the exact stereogram and near-or-distance method your orthoptist prescribed. Doing the opposite version can worsen symptoms.",
                "For a prescribed near stereogram, hold the card at arm's length and a pen midway between the card and your nose. Look at the pen while staying aware of the card.",
                "Adjust the pen until the two middle pictures join and you see three pictures. The middle picture should contain both identifying features and stay clear.",
                "For a prescribed distance stereogram, look through or over the card at a real distant target until the middle pictures join. Do not swap methods without instruction.",
                "Hold only for the time your orthoptist set, then look far away or close your eyes for an equal relaxation period.",
                "Stop if double vision persists after relaxing, your eyes remain crossed, or symptoms markedly worsen."
            ),
            howToImages = listOf(
                R.drawable.howto_stereogram_setup,
                R.drawable.howto_stereogram_near_hold,
                R.drawable.howto_stereogram_fusion,
                R.drawable.howto_stereogram_distance_hold,
                R.drawable.howto_stereogram_near_hold
            ),
            // Step 6 is a pure safety warning with no new visual to teach, so it's left
            // without an image -- PrepSteps falls back to a numbered placeholder there.
            aliases = listOf("Cat stereogram", "Bucket stereogram", "LifeSaver card", "Free-space fusion card"),
            additionalSources = listOf(nhsOrthopticExercises, obvatPilot)
        ),
        ciComponent(
            "barrel_card", "Barrel-Card Convergence", StudioStimulus.DISPARITY,
            "A correctly printed red-green barrel card and clinician instruction",
            "The barrel card appears in office-based vergence/accommodative programs, including a randomized pilot protocol, but its independent treatment effect has not been measured.",
            EvidenceGrade.D,
            illustrative(10, 9, "Clinical programs progress the card to the patient's fusion ability. No trial validates a universal number of holds; this is a short demonstration only."),
            equipmentRequired = true,
            howTo = listOf(
                "Use a correctly printed barrel card given or approved by your clinician. A phone screen cannot reproduce the viewing geometry reliably.",
                "Hold the card lengthwise against the bridge of your nose, with the largest matching barrels farthest away.",
                "Check one eye at a time so each sees its intended coloured row, then open both eyes.",
                "Look at the far pair and gently fuse them into one barrel. Keep it single and clear without holding your breath or forcing the eyes inward.",
                "Move to the middle and then nearest pair only when your clinician's progression allows it. Relax by looking into the distance after the set.",
                "Stop for persistent double vision, convergence spasm, significant headache, dizziness, nausea or pain."
            ),
            aliases = listOf("Three-barrel card", "Red-green barrel card"),
            additionalSources = listOf(obvatPilot, aaoCi, ciMeta)
        ),
        ciComponent("vectogram", "Vectogram Fusion", StudioStimulus.DISPARITY,
            "Calibrated vectogram and viewing system", "The phone demonstration does not reproduce calibrated disparity or prove the component's independent effect.", EvidenceGrade.D,
            illustrative(10, 10, "CITT prescribed 15 minutes a day, 5 days a week for 12 weeks across a set of procedures, and never a count for this one. Home push-ups also underperformed office therapy."), equipmentRequired = true),
        ciComponent("random_dot_stereogram", "Random-Dot Stereogram", StudioStimulus.STEREOGRAM,
            "Protocol-specific stereoscopic display or filters", "Clinical package evidence cannot be attributed to this component alone.", EvidenceGrade.D,
            illustrative(10, 10, "CITT prescribed 15 minutes a day, 5 days a week for 12 weeks across a set of procedures, and never a count for this one. Home push-ups also underperformed office therapy."), equipmentRequired = true),
        ciComponent("aperture_rule", "Aperture Rule", StudioStimulus.APERTURE,
            "Aperture-rule instrument and targets", "A visual explanation only; a flat phone cannot reproduce the optical geometry.", EvidenceGrade.D,
            illustrative(10, 10, "CITT prescribed 15 minutes a day, 5 days a week for 12 weeks across a set of procedures, and never a count for this one. Home push-ups also underperformed office therapy."), equipmentRequired = true),
        StudioDrill(
            id = "accommodative_rock", name = "Near / Far Accommodative Rock", issue = EyeIssue.FOCUS,
            aliases = listOf("Near and far focus", "Near and far focusing", "Refocusing", "Focus change", "Hart-chart rock"),
            targetSystem = "Accommodative system", targetFunction = "Alternating accommodative demand",
            clinicalPurpose = "A component of clinician-directed vergence/accommodative therapy.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.D, evidenceType = "Supported multicomponent protocol; component not isolated",
            studiedPopulation = "Children 9-17 with symptomatic CI and coexisting accommodative dysfunction",
            condition = "Accommodative dysfunction with convergence insufficiency",
            evidenceFor = "The multicomponent protocol improved measured accommodative amplitude and facility.",
            evidenceLimitation = "This drill was not isolated; a screen cannot create a true far accommodative demand.",
            provenOutcomes = listOf("Protocol-level accommodative amplitude and facility"),
            unprovenClaims = listOf("Reverses presbyopia", "Reverses myopia"), digitalReproducibility = DigitalReproducibility.PARTIAL,
            equipment = "Real near and distant targets", digitalValidity = "The screen only cues gaze shifts; use a real distant target.",
            whatYouDo = "Alternate attention between the on-screen near target and a clear real-world distant target.",
            whatItTrains = "Awareness of near/far focus changes.", contraindications = "Clinician assessment is appropriate for persistent blur or headache.",
            safetyLevel = "CONDITION-SPECIFIC", generalUserStatus = GeneralUserStatus.GENERAL_DEMONSTRATION,
            reviewStatus = ProfessionalReviewStatus.PROFESSIONAL_REVIEW_REQUIRED, stimulus = StudioStimulus.FOCUS_SHIFT,
            dose = illustrative(8, 15, "Clinically the measure is cycles per minute, set per patient. This is a comfortable default pace."),
            sources = listOf(accommodativeTrial, citt),
            howTo = listOf(
                "Set up two real targets. Near: hold something with fine print about 30cm away -- a pencil mark, a word on a label. Far: something across the room or out of a window with detail you can check is sharp.",
                "The screen is the metronome here, not the near target. It cannot make your eyes focus, so do not use it as one.",
                "On the near cue, look at the near target and wait until the detail actually sharpens before you move. Waiting for it to clear is the exercise.",
                "On the far cue, do the same at distance. Both eyes stay open throughout.",
                "Stop if it aches, blurs and will not clear, or gives you a headache. Blur that keeps refusing to clear is worth an eye test, not more reps."
            ),
            howToImages = listOf(
                R.drawable.howto_accommodative_rock_setup,
                null,
                R.drawable.howto_accommodative_rock_near,
                R.drawable.howto_accommodative_rock_far,
                null
            )
            // Steps 2 and 5 are pure explanation/safety text with no new visual to teach,
            // so they're left without images -- PrepSteps falls back to a numbered
            // placeholder there, same pattern as the other drills' non-visual steps.
        ),
        StudioDrill(
            id = "lens_flipper_facility", name = "Lens-Flipper Accommodative Facility", issue = EyeIssue.FOCUS,
            targetSystem = "Accommodative system", targetFunction = "Accommodation facility under lens-induced demand",
            clinicalPurpose = "Clinical measurement/training procedure using plus and minus lenses.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.D, evidenceType = "Clinical procedure within supported multicomponent protocols",
            studiedPopulation = "Condition-specific pediatric CI/accommodative-dysfunction samples", condition = "Accommodative dysfunction",
            evidenceFor = "Protocol-level improvement in facility; no independent component estimate.",
            evidenceLimitation = "Impossible to reproduce faithfully without prescribed lenses and calibrated targets.",
            provenOutcomes = listOf("Protocol-level accommodative facility"), unprovenClaims = listOf("A phone changes optical focus demand"),
            digitalReproducibility = DigitalReproducibility.EQUIPMENT_REQUIRED, equipment = "Lens flipper and calibrated near target",
            digitalValidity = "Educational animation only.", whatYouDo = "Observe how a clinician alternates lenses after the target clears.",
            whatItTrains = "Clinically, response speed to changing optical demand.", contraindications = "Do not self-select lens powers.",
            safetyLevel = "HARDWARE-DEPENDENT", generalUserStatus = GeneralUserStatus.CLINICAL_EQUIPMENT_REQUIRED,
            reviewStatus = ProfessionalReviewStatus.CLINICAL_ONLY, stimulus = StudioStimulus.FOCUS_SHIFT,
            dose = illustrative(5, 12, "Clinically scored as cycles per minute through prescribed flipper lenses. This is a viewing pace for the explanation, not a dose."),
            sources = listOf(accommodativeTrial)
        ),
        performanceDrill("horizontal_saccades", "Horizontal Saccadic Target Switching", StudioStimulus.SACCADE,
            "Saccadic initiation and target switching", "Task practice can improve trained saccadic performance; transfer to reading or health is unproven.", listOf(saccadeStudy),
            illustrative(4, 20, "The training study used laboratory blocks of hundreds of trials, not a timed home set.")),
        performanceDrill("smooth_pursuit", "Controlled Smooth Pursuit", StudioStimulus.PURSUIT,
            "Smooth tracking of a moving target", "A faithful motion demonstration; benefit for healthy users beyond task practice is unestablished.", emptyList(),
            illustrative(8, 12, "A comfortable tracking pace, chosen so the target stays followable."), EvidenceGrade.D),
        performanceDrill("fixation_stability", "Target Fixation", StudioStimulus.FIXATION,
            "Maintaining gaze on a stationary target", "A visual-skill demonstration; therapeutic benefit requires a diagnosed condition and measured protocol.", emptyList(),
            illustrative(10, 6, "Held long enough to notice drift, short enough not to strain."), EvidenceGrade.D),
        performanceDrill("vertical_saccades", "Vertical Saccadic Target Switching", StudioStimulus.SACCADE_VERTICAL,
            "Saccadic initiation and target switching on the vertical axis",
            "Task practice can improve trained saccadic performance; transfer to reading or health is unproven, and the vertical axis has been studied less than the horizontal.",
            listOf(saccadeStudy),
            illustrative(4, 20, "The training study used laboratory blocks of hundreds of trials, not a timed home set.")),
        performanceDrill("circular_pursuit", "Circular Smooth Pursuit", StudioStimulus.PURSUIT_CIRCULAR,
            "Continuous tracking through every direction of gaze",
            "A conventional clinical and sports-vision pattern. No outcome study isolates it, and benefit for healthy users beyond task practice is unestablished.",
            emptyList(),
            illustrative(10, 10, "A comfortable orbit rate, chosen so the target stays followable without catch-up flicks."),
            EvidenceGrade.D),
        performanceDrill("figure_eight_pursuit", "Figure-Eight Pursuit", StudioStimulus.PURSUIT_FIGURE_EIGHT,
            "Tracking across the midline in both directions",
            "A long-standing clinical pattern chosen because it crosses the midline repeatedly. Its specific advantage over simpler tracking has not been demonstrated.",
            emptyList(),
            illustrative(12, 8, "One full figure per rep at a rate the eye can hold."),
            grade = EvidenceGrade.D,
            aliases = listOf("Figure of eight", "Infinity tracing", "Lazy eight")),
        StudioDrill(
            id = "anti_saccade", name = "Anti-Saccade (Look Away)",
            issue = EyeIssue.TRACKING, targetSystem = "Oculomotor and inhibitory control",
            targetFunction = "Suppressing a reflexive look and moving the opposite way",
            clinicalPurpose = "A research paradigm measuring voluntary control over a reflexive eye movement.",
            clinicalUse = true, evidenceGrade = EvidenceGrade.C,
            evidenceType = "Extensively characterised laboratory paradigm",
            studiedPopulation = "Healthy adults, and clinical groups in which error rates differ",
            condition = "No general medical indication",
            evidenceFor = "The task reliably measures inhibitory control of gaze, and error rates separate several clinical groups from controls.",
            evidenceLimitation = "It is a measurement paradigm, not a treatment. Practising it improves the task itself; there is no evidence that doing so improves attention, reading or eye health.",
            provenOutcomes = listOf("Anti-saccade error rate", "Saccadic reaction time"),
            unprovenClaims = listOf("Improves attention", "Improves reading", "Treats ADHD", "Improves general eye health"),
            digitalReproducibility = DigitalReproducibility.PARTIAL, equipment = "None to run it; eye tracking to score it",
            digitalValidity = "The stimulus is faithful, but without eye tracking the app cannot tell whether you actually looked away.",
            whatYouDo = "When the target appears, look to the OPPOSITE side by the same distance. Do not look at it.",
            whatItTrains = "Voluntary suppression of a reflexive glance.",
            contraindications = "Stop for double vision, dizziness, nausea, pain, or severe headache.",
            safetyLevel = "SAFE GENERAL DEMONSTRATION", generalUserStatus = GeneralUserStatus.GENERAL_DEMONSTRATION,
            reviewStatus = ProfessionalReviewStatus.RESEARCH_REVIEWED, stimulus = StudioStimulus.ANTI_SACCADE,
            dose = illustrative(5, 16, "Laboratory blocks run 100+ trials with eye tracking. This is a short unscored sample."),
            sources = listOf(antiSaccade, saccadeStudy)
        ),
        ciComponent("step_vergence", "Step Vergence Jumps", StudioStimulus.VERGENCE_STEP,
            "Prism bars or a calibrated step target",
            "A clinically used component of multicomponent therapy; a flat screen cannot create a true vergence demand and its independent efficacy is unestablished.",
            EvidenceGrade.D,
            illustrative(8, 12, "CITT prescribed 15 minutes a day, 5 days a week for 12 weeks across a set of procedures, and never a count for this one."), equipmentRequired = true),
        StudioDrill(
            id = "hemianopia_scanning", name = "Structured Hemifield Scanning", issue = EyeIssue.NEURO,
            targetSystem = "Visual field compensation", targetFunction = "Systematic scanning toward a field loss",
            clinicalPurpose = "Compensatory rehabilitation after stroke-related homonymous hemianopia.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.C, evidenceType = "Systematic review plus multicenter sham-controlled RCT",
            studiedPopulation = "Adults with stable homonymous hemianopia after stroke", condition = "Post-stroke hemianopia",
            evidenceFor = "Earlier reviews found encouraging visual outcomes; a 2025 trial found no advantage over sham.",
            evidenceLimitation = "Conflicting evidence; it does not restore a damaged visual field.",
            provenOutcomes = listOf("Task-specific visual scanning in some studies"), unprovenClaims = listOf("Restores lost visual field"),
            digitalReproducibility = DigitalReproducibility.FULL, equipment = "Clinical field assessment before therapeutic use",
            digitalValidity = "The search task is reproducible, but screen extent is narrower than real-world space.",
            whatYouDo = "Search systematically from the anchor line across the display and select each target.",
            whatItTrains = "A compensatory scanning strategy.", contraindications = "Stroke rehabilitation should be selected by a qualified clinician.",
            safetyLevel = "CLINICIAN-SUPERVISED", generalUserStatus = GeneralUserStatus.CLINICIAN_SUPERVISED,
            reviewStatus = ProfessionalReviewStatus.CLINICAL_ONLY, stimulus = StudioStimulus.SCANNING,
            dose = illustrative(12, 10, "Scanning programmes run for hours across weeks and are prescribed against a measured field loss. This is a short sample of the search task."),
            sources = listOf(strokeMeta, searchTrial)
        ),
        StudioDrill(
            id = "gaze_stabilization_vor", name = "Gaze Stabilization (VORx1)", issue = EyeIssue.NEURO,
            aliases = listOf("VOR x1", "Gaze stability exercise", "Vestibulo-ocular reflex training"),
            targetSystem = "Vestibulo-ocular reflex", targetFunction = "Holding a target still while the head moves",
            clinicalPurpose = "Vestibular rehabilitation for diagnosed peripheral vestibular hypofunction.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.A,
            evidenceType = "Clinical practice guideline, strong recommendation, built on Level I randomised trials",
            studiedPopulation = "Adults whose vestibular loss was confirmed by caloric or rotational-chair testing",
            condition = "Unilateral or bilateral peripheral vestibular hypofunction",
            evidenceFor = "The 2022 APTA guideline rates gaze stability exercises as having strong evidence of effectiveness, against a strong recommendation not to substitute eye-only exercises. In a randomised trial in chronic unilateral loss, 12 of 13 people reached normal dynamic visual acuity while no placebo patient did.",
            evidenceLimitation = "Every trial enrolled people whose vestibular loss was confirmed by caloric or rotational-chair testing, which no app can do. Dizziness has many causes and some need urgent assessment. Recovered dynamic visual acuity also did not track with how much oscillopsia people still reported.",
            provenOutcomes = listOf("Dynamic visual acuity", "Dizziness and disequilibrium", "Postural stability"),
            unprovenClaims = listOf("Sharpens eyesight", "Treats dizziness of unknown cause", "Replaces assessment of new vertigo"),
            digitalReproducibility = DigitalReproducibility.FULL, equipment = "None - the phone is the fixation target",
            digitalValidity = "A stationary target held at a fixed distance is exactly what VORx1 asks for, so the screen reproduces the task. It cannot sense head speed, so it cannot tell you whether the target truly stayed in focus.",
            whatYouDo = "Hold the phone at arm's length, fix on the target, and turn your head while the target stays clear.",
            whatItTrains = "The reflex that holds your gaze steady while your head is moving.",
            contraindications = "For vestibular hypofunction diagnosed by a clinician. Sit down for it. Stop if dizziness worsens through the session, or for headache, nausea that does not settle, or new visual symptoms. Sudden vertigo, new hearing loss, double vision or neurological symptoms need assessment, not exercise.",
            safetyLevel = "CLINICIAN-SUPERVISED", generalUserStatus = GeneralUserStatus.CLINICIAN_SUPERVISED,
            reviewStatus = ProfessionalReviewStatus.CLINICAL_ONLY, stimulus = StudioStimulus.FIXATION,
            dose = illustrative(60, 3, "Trials ran one-minute bouts totalling 20-40 minutes a day, and the guideline sets a minimum of three sessions daily. Three one-minute bouts is one session's sample, not the daily dose."),
            howTo = listOf(
                "Sit down first. This exercise is meant to provoke mild dizziness, and doing it standing risks a fall.",
                "Hold the phone at arm's length at eye level and look at the centre target.",
                "Keep your eyes locked on the target and turn your head side to side, about 30 to 45 degrees each way. The target has to stay in focus - if it blurs, slow down.",
                "Move as fast as you can while the target stays clear. The speed is what drives the adaptation, so build it up as the blur allows.",
                "After a minute, rest until the dizziness settles. Do the next bouts nodding up and down instead of side to side.",
                "Mild dizziness during and just after is expected. Stop if it builds through the session, or for headache, nausea that does not settle, or any new visual symptom."
            ),
            howToImages = listOf(
                null,
                R.drawable.howto_vor_hold,
                R.drawable.howto_vor_horizontal,
                R.drawable.howto_vor_horizontal,
                R.drawable.howto_vor_vertical,
                null
            ),
            // Step 1 is a sit-down safety note and step 6 is a safety reminder, neither has
            // a new visual to teach. Step 4 (move faster) reuses the horizontal image --
            // same posture, just paced differently, so no new image is needed.
            sources = listOf(vestibularCpg)
        ),
        StudioDrill(
            id = "neglect_pursuit", name = "Contralesional Smooth-Pursuit Training", issue = EyeIssue.NEURO,
            targetSystem = "Visual attention / oculomotor", targetFunction = "Pursuit toward contralesional space",
            clinicalPurpose = "Condition-specific rehabilitation for post-stroke spatial neglect.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.C, evidenceType = "Small studies and systematic reviews",
            studiedPopulation = "Stroke survivors with unilateral spatial neglect", condition = "Post-stroke spatial neglect",
            evidenceFor = "Short-term neglect-test and some functional improvements in small studies.",
            evidenceLimitation = "Small heterogeneous samples and risk of bias; not generalizable to healthy users.",
            provenOutcomes = listOf("Neglect measures in selected stroke samples"), unprovenClaims = listOf("Treats all post-stroke vision loss"),
            digitalReproducibility = DigitalReproducibility.PARTIAL, equipment = "Clinician-selected spatial setup",
            digitalValidity = "The target motion is reproducible; therapeutic direction and dose are patient-specific.",
            whatYouDo = "In supervised care, follow the target continuously toward the affected side.", whatItTrains = "Directed visual exploration and pursuit.",
            contraindications = "Clinical-only; stop for neurological symptoms, nausea, or severe headache.", safetyLevel = "CLINICIAN-SUPERVISED",
            generalUserStatus = GeneralUserStatus.CLINICIAN_SUPERVISED, reviewStatus = ProfessionalReviewStatus.CLINICAL_ONLY,
            stimulus = StudioStimulus.HEMIFIELD_PURSUIT,
            dose = illustrative(10, 12, "Direction and dose are patient-specific and set by a clinician."),
            sources = listOf(pursuitReview, strokeMeta)
        ),
        StudioDrill(
            id = "dichoptic_amblyopia", name = "Contrast-Balanced Dichoptic Task", issue = EyeIssue.LAZY_EYE,
            targetSystem = "Binocular cortical processing", targetFunction = "Simultaneous binocular integration with contrast balancing",
            clinicalPurpose = "Investigational/adjunct digital treatment for diagnosed amblyopia.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.C, evidenceType = "Multiple RCTs and systematic reviews with heterogeneous results",
            studiedPopulation = "Children with unilateral anisometropic, strabismic, or mixed amblyopia", condition = "Diagnosed amblyopia",
            evidenceFor = "Visual-acuity gains can occur, but superiority to patching is not established and protocol matters.",
            evidenceLimitation = "Requires eye-specific images, calibrated contrast, prescribed correction, adherence, and clinical monitoring.",
            provenOutcomes = listOf("Visual acuity in selected protocols"), unprovenClaims = listOf("Cures lazy eye", "Works without diagnosis or optical correction"),
            digitalReproducibility = DigitalReproducibility.EQUIPMENT_REQUIRED, equipment = "Anaglyph/3D separation and calibrated contrast",
            digitalValidity = "This uncalibrated preview explains dichoptic separation; it is not treatment.",
            whatYouDo = "Observe that different target parts are assigned to each eye and must be combined.", whatItTrains = "Binocular combination in protocol-specific therapy.",
            contraindications = "Pediatric ophthalmology/orthoptic supervision required.", safetyLevel = "DO NOT DEPLOY TO GENERAL USERS",
            generalUserStatus = GeneralUserStatus.CLINICIAN_SUPERVISED, reviewStatus = ProfessionalReviewStatus.CLINICAL_ONLY,
            stimulus = StudioStimulus.DICHOPTIC,
            dose = illustrative(10, 12, "Real protocols run about an hour a day for weeks on calibrated hardware. This is a look at the principle."),
            sources = listOf(amblyopiaReview, amblyopiaRct)
        ),
        StudioDrill(
            id = "ixt_anti_suppression", name = "Intermittent Exotropia Anti-Suppression Demo", issue = EyeIssue.LAZY_EYE,
            targetSystem = "Binocular fusion", targetFunction = "Simultaneous perception and fusion maintenance",
            clinicalPurpose = "Component of office therapy studied in selected small-to-moderate angle intermittent exotropia.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.C, evidenceType = "Small single-center randomized clinical trial",
            studiedPopulation = "40 children aged 6 to <18 with untreated small-to-moderate angle IXT", condition = "Intermittent exotropia",
            evidenceFor = "Short-term improvement in clinical binocular and accommodative measures for a multicomponent program.",
            evidenceLimitation = "Small trial, short-term endpoint, and the anti-suppression component was not isolated.",
            provenOutcomes = listOf("Program-level binocular clinical measures"), unprovenClaims = listOf("Treats every exotropia", "Straightens eyes permanently"),
            digitalReproducibility = DigitalReproducibility.EQUIPMENT_REQUIRED, equipment = "Eye-separation filters and clinician calibration",
            digitalValidity = "Uncalibrated educational preview only.", whatYouDo = "Observe complementary monocular elements that form one binocular target.",
            whatItTrains = "Clinically, simultaneous binocular participation and fusion control.", contraindications = "Requires strabismus diagnosis and supervision.",
            safetyLevel = "DO NOT DEPLOY TO GENERAL USERS", generalUserStatus = GeneralUserStatus.CLINICIAN_SUPERVISED,
            reviewStatus = ProfessionalReviewStatus.CLINICAL_ONLY, stimulus = StudioStimulus.DICHOPTIC,
            dose = illustrative(10, 12, "The trial ran a supervised office programme, not a timed phone set."),
            sources = listOf(ixtTrial)
        ),
        StudioDrill(
            id = "concussion_vergence", name = "Post-Concussion Vergence Demo", issue = EyeIssue.NEURO,
            // Full clinical name kept as an alias: it does not fit a phone row, and the
            // how-to sheet is where the long form belongs.
            aliases = listOf("Post-Concussion Vergence / Accommodation Demo"),
            targetSystem = "Vergence and accommodation", targetFunction = "Near point of convergence and positive fusional vergence",
            clinicalPurpose = "Clinician-delivered therapy for diagnosed concussion-related convergence insufficiency.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.B, evidenceType = "Randomized delayed-treatment clinical trial",
            studiedPopulation = "People aged 11-25, 4-24 weeks after concussion, with diagnosed symptomatic CI", condition = "Concussion-related convergence insufficiency",
            evidenceFor = "The 2025 CONCUSS trial reported improved clinical signs and symptoms with immediate office therapy versus delay.",
            evidenceLimitation = "Evidence applies to a diagnosed subgroup and a supervised multicomponent program, not this phone demo.",
            provenOutcomes = listOf("Near point of convergence", "Positive fusional vergence", "CI symptom scores"),
            unprovenClaims = listOf("Treats concussion", "Safe for unscreened self-treatment"), digitalReproducibility = DigitalReproducibility.PARTIAL,
            equipment = "Clinical diagnosis, monitoring, and protocol materials", digitalValidity = "Concept demo only.",
            whatYouDo = "Observe the controlled near-target and disparity tasks used within supervised care.", whatItTrains = "Protocol-level vergence/accommodative function.",
            contraindications = "Clinical-only after concussion; stop for symptom exacerbation.", safetyLevel = "CLINICIAN-SUPERVISED",
            generalUserStatus = GeneralUserStatus.CLINICIAN_SUPERVISED, reviewStatus = ProfessionalReviewStatus.CLINICAL_ONLY,
            stimulus = StudioStimulus.NEAR_TARGET,
            dose = illustrative(10, 12, "The CONCUSS programme was weekly supervised office therapy with home reinforcement."),
            sources = listOf(concussTrial)
        ),

        // ---- relief: done with the eyes closed or covered -------------------------
        StudioDrill(
            id = "palming", name = "Palming Rest", issue = EyeIssue.TENSION,
            aliases = listOf("Eye palming", "Dark rest"),
            targetSystem = "Whole visual system", targetFunction = "Removing light and near-focus demand for a fixed interval",
            clinicalPurpose = "A timed rest. It is not a treatment for anything.", clinicalUse = false,
            evidenceGrade = EvidenceGrade.E, evidenceType = "No controlled outcome study identified",
            studiedPopulation = "None", condition = "Eye and brow tension during long near work",
            evidenceFor = "Nothing has been measured. Covering the eyes removes light and the demand to focus, which is rest rather than therapy.",
            evidenceLimitation = "Palming is best known from the Bates method, which claimed to correct refractive error and never demonstrated it. Rest is all this is.",
            provenOutcomes = emptyList(),
            unprovenClaims = listOf("Improves eyesight", "Reduces a glasses prescription", "Treats eye disease", "Relaxes the focusing muscle"),
            digitalReproducibility = DigitalReproducibility.FULL, equipment = "Your own hands",
            digitalValidity = "The app can pace and time the rest; the rest itself happens with the screen unwatched.",
            whatYouDo = "Cup your palms over closed eyes without pressing on them, and breathe slowly until the timer ends.",
            whatItTrains = "Nothing measurable. It is a paced break from light and near focus.",
            contraindications = "Never press on the eyeball. Wash your hands first, and skip it with an eye infection or after recent eye surgery. Eye pain, flashes or a shower of new floaters need an eye-care professional the same day, not a rest.",
            safetyLevel = "SAFE GENERAL DEMONSTRATION", generalUserStatus = GeneralUserStatus.GENERAL_TRAINING,
            reviewStatus = ProfessionalReviewStatus.NOT_REVIEWED, stimulus = StudioStimulus.PALMING,
            dose = illustrative(10, 12, "Two minutes, paced to a slow ten-second breath: long enough to be a break, short enough to actually do."),
            sources = emptyList(),
            howTo = listOf(
                "Sit with your elbows on a desk or on a cushion in your lap, so your neck carries none of the weight.",
                "Rub your palms together for a few seconds until they feel warm.",
                "Close your eyes, then cup a palm over each socket, heel of the hand on the cheekbone, fingers on the forehead. No weight on the eyeball itself.",
                "Let the darkness be complete but comfortable. Breathe in for about five seconds, out for about five.",
                "Come out slowly: drop the hands first, open the eyes into the low light, then look at something bright."
            )
        ),
        StudioDrill(
            id = "eye_range_of_motion", name = "Full Range-of-Motion Rotations", issue = EyeIssue.TENSION,
            aliases = listOf("Around the world", "Eye rolls", "Roll your eyes", "Directional gaze"),
            targetSystem = "Extraocular muscles", targetFunction = "Taking gaze to each extreme and holding it briefly",
            clinicalPurpose = "A comfort and mobility routine after a long spell at one fixed distance.", clinicalUse = false,
            evidenceGrade = EvidenceGrade.E, evidenceType = "Conventional practice; no controlled outcome study identified",
            studiedPopulation = "None", condition = "Stiffness and ache after prolonged fixed gaze",
            evidenceFor = "Nothing measured. The eyes do move through their full range, which is the whole of the claim.",
            evidenceLimitation = "Widely taught and widely oversold. Eye muscles are not weak the way a limb muscle can be, and moving them further does not sharpen sight.",
            provenOutcomes = emptyList(),
            unprovenClaims = listOf("Strengthens the eye muscles", "Improves eyesight", "Slows or reverses myopia", "Removes the need for glasses"),
            digitalReproducibility = DigitalReproducibility.FULL, equipment = "None",
            digitalValidity = "The target reaches each extreme of gaze on a schedule, which is exactly what the exercise asks for.",
            whatYouDo = "Keep your head still and follow the target to each edge in turn, holding briefly at each without straining.",
            whatItTrains = "Comfortable movement through the full range of gaze.",
            contraindications = "Move only as far as is comfortable. Stop for pain, dizziness, nausea or double vision. Skip it after recent eye surgery or a retinal problem unless your eye-care professional has cleared it.",
            safetyLevel = "SAFE GENERAL DEMONSTRATION", generalUserStatus = GeneralUserStatus.GENERAL_TRAINING,
            reviewStatus = ProfessionalReviewStatus.NOT_REVIEWED, stimulus = StudioStimulus.EYE_ROM,
            dose = illustrative(8, 8, "Eight positions, one second at each, eight times round: slow enough to reach each extreme rather than flick past it."),
            sources = emptyList()
        ),
        StudioDrill(
            id = "orbital_acupressure", name = "Orbital Rim Massage", issue = EyeIssue.TENSION,
            targetSystem = "Periorbital tissue", targetFunction = "Gentle pressure around the bony rim of the eye socket",
            clinicalPurpose = "A relief routine for the ache around the eyes after long near work.", clinicalUse = false,
            evidenceGrade = EvidenceGrade.D,
            evidenceType = "Studied as the Chinese school eye exercises; mixed results, and mostly against the wrong outcome for a comfort routine",
            studiedPopulation = "Chinese schoolchildren, in studies of acupoint eye exercises", condition = "Periocular tension and ache",
            evidenceFor = "Some studies of the schools programme report short-term easing of tired-eye symptoms.",
            evidenceLimitation = "Those trials tested a whole school routine, not this sequence, and looked mainly at whether it slowed myopia. It does not. Anything here is comfort while you do it.",
            provenOutcomes = listOf("Short-term self-reported comfort"),
            unprovenClaims = listOf("Slows myopia", "Improves eyesight", "Treats dry eye", "Drains the eye"),
            digitalReproducibility = DigitalReproducibility.FULL, equipment = "Clean hands",
            digitalValidity = "The app can pace the sequence and name each point; the pressure is yours to judge.",
            whatYouDo = "Press gently on the bone around the eye socket at each point in turn, in small circles, never on the eyeball.",
            whatItTrains = "Nothing measurable. It is paced, gentle pressure on the tissue around the eye.",
            contraindications = "Bone only, never the eyeball. Clean hands. Skip it over broken skin, a stye, an eye infection, recent eye or facial surgery, or any injury around the eye.",
            safetyLevel = "SAFE GENERAL DEMONSTRATION", generalUserStatus = GeneralUserStatus.GENERAL_TRAINING,
            reviewStatus = ProfessionalReviewStatus.NOT_REVIEWED, stimulus = StudioStimulus.ACUPRESSURE,
            dose = illustrative(16, 6, "Four points, four seconds of small circles at each, six times round."),
            sources = emptyList(),
            howTo = listOf(
                "Wash your hands and close your eyes.",
                "Inner corner: the small notch beside the bridge of the nose, pressed with your index fingers.",
                "Brow: out along the bony ridge under the eyebrow, to the middle of the brow.",
                "Temple: the hollow at the outer corner, a finger's width out from the eye.",
                "Under-eye: the bony ridge below the pupil, about a finger's width under the lower lid.",
                "Small circles, light pressure, four seconds each. If anything is sharp or sore, stop."
            ),
            howToImages = listOf(
                null,
                R.drawable.howto_orbital_acupressure_inner,
                R.drawable.howto_orbital_acupressure_brow,
                R.drawable.howto_orbital_acupressure_temple,
                R.drawable.howto_orbital_acupressure_undereye,
                null
            )
            // Steps 1 and 6 are prep/technique reminders with no new location to show, so
            // they're left without images -- PrepSteps falls back to a numbered placeholder.
        ),

        // ---- lid care: the part of dry eye that is not blinking --------------------
        StudioDrill(
            id = "warm_compress", name = "Warm Compress + Lid Massage", issue = EyeIssue.DRY_AND_TIRED,
            targetSystem = "Meibomian glands and lid margin", targetFunction = "Warming the lid oils, then expressing them along the lid",
            clinicalPurpose = "First-line self-care for meibomian gland dysfunction and evaporative dry eye.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.B, evidenceType = "Consensus review of controlled trials of lid warming and expression",
            studiedPopulation = "Adults with meibomian gland dysfunction and evaporative dry eye",
            condition = "Meibomian gland dysfunction / evaporative dry eye",
            evidenceFor = "Sustained lid warming with expression improves gland secretion, tear break-up time and dry-eye symptoms, and is a first-line recommendation in the TFOS DEWS II therapy report.",
            evidenceLimitation = "The benefit depends on the lid reaching and holding treatment temperature, which a folded flannel loses within a couple of minutes; the regimens that worked reheated the compress or used a purpose-made one. The app times the routine and cannot tell how warm your lids are.",
            provenOutcomes = listOf("Meibomian gland secretion", "Tear break-up time", "Dry-eye symptom scores"),
            unprovenClaims = listOf("Cures dry eye", "Replaces prescribed treatment", "Works without reheating"),
            digitalReproducibility = DigitalReproducibility.PARTIAL, equipment = "A clean warm compress or a microwaveable eye mask",
            digitalValidity = "Timing and pacing are reproduced faithfully; the heat is not, and the temperature is the active part.",
            whatYouDo = "Hold a warm compress over closed lids, reheating as it cools, then sweep gently along each lid towards the lashes.",
            whatItTrains = "Melting and clearing the oil layer that keeps tears from evaporating.",
            contraindications = "Warm, never hot: test it on your inner wrist first. Stop for pain, and do not do this over an active infection, a hot swollen lid, or after recent eye surgery without your eye-care professional's say-so.",
            safetyLevel = "SAFE GENERAL DEMONSTRATION", generalUserStatus = GeneralUserStatus.GENERAL_TRAINING,
            reviewStatus = ProfessionalReviewStatus.RESEARCH_REVIEWED, stimulus = StudioStimulus.WARM_COMPRESS,
            dose = illustrative(20, 12, "Four minutes of sustained lid warming with expression is the shape of the routine DEWS II describes; the protocols behind it vary in the exact minutes, so this total is a reasonable middle."),
            sources = listOf(dews2),
            howTo = listOf(
                "What you need: a microwaveable eye mask, or a clean flannel. A mask holds its heat for the whole routine. A flannel goes cold in about two minutes, so you will be reheating it.",
                "To heat a flannel: hold it under the hot tap, wring it out well, then fold it in half. To heat a mask: microwave it for exactly as long as its label says, and no longer.",
                "Nothing to hand? Use your hands. Rub your palms together hard for ten seconds and cup them over your closed lids. The heat is weaker, so re-warm them every time it fades.",
                "Test it on the inside of your wrist before it goes near your eyes. It should feel warm and comfortable, never hot. Lid skin is the thinnest on your body and scalds easily.",
                "Sit or lie back with your head supported. Close both eyes and lay the compress across both lids. Let it rest there under its own weight -- no pressing on the eye.",
                "Reheat the moment it stops feeling warm. The heat is the part that works: a cooled cloth sitting on your face is doing nothing.",
                "When I call the massage: use the flat pad of your index finger. Upper lid, sweep downwards. Lower lid, sweep upwards. Always towards the lashes, never sideways across the eye.",
                "Firm enough to move the skin, gentle enough to stay comfortable. Two or three sweeps a lid, then the warmth goes back on.",
                "Finish by wiping along the lash line with a fresh lid wipe or a clean damp cloth, to clear away what you have just worked loose."
            )
        ),
        StudioDrill(
            id = "lid_hygiene", name = "Daily Lid-Margin Cleaning", issue = EyeIssue.DRY_AND_TIRED,
            targetSystem = "Lid margin", targetFunction = "Clearing crust, debris and biofilm from the lash line",
            clinicalPurpose = "First-line self-care for blepharitis and lid-margin disease.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.C,
            evidenceType = "Consensus first-line recommendation; individual cleaning regimens are poorly separated in trials",
            studiedPopulation = "Adults with blepharitis and lid-margin disease", condition = "Blepharitis / lid-margin disease",
            evidenceFor = "Recommended as first-line management of lid-margin disease in the TFOS DEWS II therapy report, with symptom improvement reported across regimens.",
            evidenceLimitation = "Which cleanser, how hard and how often are not settled, and crusting that keeps returning needs a diagnosis rather than more scrubbing.",
            provenOutcomes = listOf("Lid-margin debris", "Symptom scores"),
            unprovenClaims = listOf("Cures blepharitis", "Prevents dry eye", "Replaces prescribed treatment"),
            digitalReproducibility = DigitalReproducibility.PARTIAL, equipment = "Lid wipes or a lid-safe cleanser, and a clean cloth",
            digitalValidity = "Nothing to run. The app can only tell you how it is done.",
            whatYouDo = "Once a day, clean along the lash line of each closed lid with a lid wipe or a cloth and a lid-safe cleanser.",
            whatItTrains = "Nothing. It keeps the lid margin clear so the glands behind it can drain.",
            contraindications = "Stop for pain, bleeding, or a red swollen lid that is spreading -- that needs care the same week. Do not put cleanser in the eye itself.",
            safetyLevel = "SAFE GENERAL DEMONSTRATION", generalUserStatus = GeneralUserStatus.GENERAL_TRAINING,
            reviewStatus = ProfessionalReviewStatus.RESEARCH_REVIEWED, stimulus = StudioStimulus.WARM_COMPRESS,
            dose = illustrative(60, 1, "About a minute a day, both eyes, as part of washing. Best straight after a warm compress, while the oils are still soft."),
            sources = listOf(dews2),
            practice = Practice.HABIT,
            howTo = listOf(
                "Do it after a warm compress if you use one: the debris comes away far more easily.",
                "Wash your hands. Take a fresh lid wipe, or a clean cloth with a lid-safe cleanser.",
                "Close one eye and pull the lid gently taut. Wipe along the lash line itself, not the surface of the lid, in short strokes.",
                "Repeat on the lower lid, then the other eye with a fresh surface each time. Never reuse a wipe between eyes.",
                "Rinse with clean water and pat dry. Once a day is plenty; twice makes it sore."
            )
        ),
        StudioDrill(
            id = "blink_awareness", name = "Blink Awareness at the Screen", issue = EyeIssue.DRY_AND_TIRED,
            targetSystem = "Ocular surface", targetFunction = "Restoring blink rate and completeness during screen work",
            clinicalPurpose = "The habit behind the blink drill: doing it where the problem actually happens.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.C,
            evidenceType = "Randomised trial of an all-day on-screen blink cue; the unprompted habit itself is untested",
            studiedPopulation = "Adults with dry-eye symptoms who use screens", condition = "Dry eye / incomplete blinking during screen use",
            evidenceFor = "A smartphone cue prompting a forceful blink through the working day improved tear break-up time and symptom scores over 30 days.",
            evidenceLimitation = "That trial supplied the cue. Nobody has shown that deciding to blink better, unprompted, does the same -- and blink rate drops again the moment attention returns to the task.",
            provenOutcomes = listOf("Tear break-up time (cued)", "Dry-eye symptom scores (cued)"),
            unprovenClaims = listOf("Cures dry eye", "Works without a reminder", "Improves eyesight"),
            digitalReproducibility = DigitalReproducibility.FULL, equipment = "None",
            digitalValidity = "The timed drill in this app reproduces the cue. This entry is the same thing carried into the rest of the day.",
            whatYouDo = "Through screen work, catch the blink you are not taking and make it a complete one: lids fully closed, with a brief squeeze.",
            whatItTrains = "A complete blink at the moment attention would otherwise suppress it.",
            contraindications = "None specific. Dryness that persists despite this needs an eye-care assessment rather than more effort.",
            safetyLevel = "SAFE GENERAL DEMONSTRATION", generalUserStatus = GeneralUserStatus.GENERAL_TRAINING,
            reviewStatus = ProfessionalReviewStatus.RESEARCH_REVIEWED, stimulus = StudioStimulus.BLINK,
            dose = illustrative(5, 12, "The trial that worked cued a forceful blink roughly every five seconds, all day. Carried by hand, aim for one deliberate complete blink at the end of every paragraph, email or scroll."),
            sources = listOf(blinkApp, blinkRct),
            practice = Practice.HABIT,
            howTo = listOf(
                "Watch for the moment reading gets intent. That is when blinking stops -- not when the eyes start to hurt.",
                "Take one deliberate blink: lids all the way shut, a brief gentle squeeze, then open. A half-blink spreads nothing.",
                "Hang it on something that already repeats: the end of a paragraph, sending an email, a scroll.",
                "Set the screen slightly below eye level. A lower gaze leaves less eye surface exposed to the air.",
                "If the habit will not stick, run the Complete Blink drill here instead: a supplied cue is what the trial actually tested."
            )
        ),

        // ---- habits: nothing to run, everything to do -----------------------------
        StudioDrill(
            id = "screen_setup", name = "Screen Setup and Lighting", issue = EyeIssue.SCREEN_STRAIN,
            targetSystem = "Viewing conditions", targetFunction = "Reducing the demand the screen makes in the first place",
            clinicalPurpose = "The setup half of digital eye strain, which no exercise substitutes for.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.C, evidenceType = "Professional consensus guidance; individual factors rarely isolated in trials",
            studiedPopulation = "Adult screen users", condition = "Digital eye strain",
            evidenceFor = "Distance, screen height, glare control and text size are the standard first recommendations for screen-related symptoms, and symptomatic users commonly improve once they are corrected.",
            evidenceLimitation = "Almost never tested one factor at a time, and none of it changes your eyes -- it changes what you ask of them. Symptoms that persist with a good setup are a reason to have your eyes examined.",
            provenOutcomes = listOf("Symptom scores in mixed ergonomic interventions"),
            unprovenClaims = listOf("Blue light from screens damages the eye", "Blue-blocking lenses relieve eye strain", "Screens cause permanent damage"),
            digitalReproducibility = DigitalReproducibility.FULL, equipment = "Your desk",
            digitalValidity = "Nothing to run. The app can only tell you what to change.",
            whatYouDo = "Set the screen an arm's length away and slightly below eye level, kill the glare, and make the text big enough to read without leaning in.",
            whatItTrains = "Nothing. It lowers the load the exercises are otherwise trying to offset.",
            contraindications = "Leaning in to read despite a good setup, or new blur, headache or double vision, calls for an eye examination rather than another adjustment.",
            safetyLevel = "SAFE GENERAL DEMONSTRATION", generalUserStatus = GeneralUserStatus.GENERAL_TRAINING,
            reviewStatus = ProfessionalReviewStatus.RESEARCH_REVIEWED, stimulus = StudioStimulus.BREAK_REMINDER,
            dose = illustrative(120, 1, "A couple of minutes, once, and again whenever the desk changes. This one is not a repeating dose."),
            sources = listOf(aaoScreens, breakStudy),
            practice = Practice.HABIT,
            howTo = listOf(
                "Distance: about an arm's length from eye to screen, further for a large monitor.",
                "Height: the top of the screen at or just below eye level, so your gaze runs slightly downwards and less eye surface is exposed.",
                "Glare: no window or bright lamp directly behind or in front of the screen. Tilt the screen away from whatever is reflecting in it.",
                "Brightness: match the screen to the room. A screen that glows in a dark room, or looks grey in a bright one, is set wrong.",
                "Text: enlarge it until you can read sitting back. Leaning in is the signal, not the fix.",
                "Then keep taking breaks anyway. Setup and breaks do different jobs."
            )
        ),
        StudioDrill(
            id = "working_distance_correction", name = "Check Your Screen Prescription", issue = EyeIssue.SCREEN_STRAIN,
            targetSystem = "Optical correction", targetFunction = "Clear vision at the actual screen working distance",
            clinicalPurpose = "A prompt to use current, task-appropriate correction before adding exercises.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.C, evidenceType = "Evidence-based review and professional consensus report",
            studiedPopulation = "Adults using computers, including people with presbyopia", condition = "Digital eye strain with refractive or working-distance demand",
            evidenceFor = "Uncorrected or partly corrected refractive error can contribute to digital eye strain. Expert guidance recommends full correction appropriate to the viewing distance.",
            evidenceLimitation = "Trials comparing computer-specific lenses are small and heterogeneous. This app cannot measure a prescription, working distance or binocular status.",
            provenOutcomes = listOf("Task-specific symptom scores in some optical-correction studies"),
            unprovenClaims = listOf("Glasses weaken the eyes", "One lens design prevents all screen strain", "Eye exercises replace a prescription"),
            digitalReproducibility = DigitalReproducibility.PARTIAL, equipment = "Your usual correction and an eye examination when due",
            digitalValidity = "Nothing to run. The app can identify a reason to check correction, but cannot prescribe lenses.",
            whatYouDo = "Use the correction prescribed for your screen distance. If you lean in, squint, or still blur, arrange an eye examination instead of adding more drills.",
            whatItTrains = "Nothing. Clear correction removes avoidable optical demand.",
            contraindications = "Sudden blur, new double vision, eye pain, flashes, a curtain or sudden vision loss needs prompt medical assessment, not a lens experiment.",
            safetyLevel = "SAFE GENERAL GUIDANCE", generalUserStatus = GeneralUserStatus.GENERAL_TRAINING,
            reviewStatus = ProfessionalReviewStatus.RESEARCH_REVIEWED, stimulus = StudioStimulus.BREAK_REMINDER,
            dose = illustrative(60, 1, "Check once when setting up a workstation and again when the prescription, symptoms or working distance changes."),
            sources = listOf(digitalEnvironment, opticalCorrectionReview),
            practice = Practice.HABIT,
            howTo = listOf(
                "Sit where you normally work and measure the distance from your eyes to the screen. Tell the optometrist that distance; a desktop, laptop and phone make different demands.",
                "Use the glasses or contacts prescribed for that task. Do not remove or weaken correction because an exercise claims it will strengthen the eye.",
                "Make text larger before leaning closer. Persistent squinting, blur, headache or closing one eye is a reason for an examination.",
                "If you use progressive lenses, keep the screen where you can see it without tipping your chin up. Neck strain can be a lens-zone and monitor-height problem, not an eye-muscle problem."
            )
        ),
        StudioDrill(
            id = "outdoor_daylight", name = "Daily Time Outdoors", issue = EyeIssue.SCREEN_STRAIN,
            targetSystem = "Eye growth and refractive development", targetFunction = "Daylight exposure and long viewing distances",
            clinicalPurpose = "The one habit here with trial evidence for delaying the onset of short-sightedness in children.", clinicalUse = true,
            evidenceGrade = EvidenceGrade.B, evidenceType = "Cluster-randomised school trial, with supporting cohort evidence",
            studiedPopulation = "Children aged about 6-7, followed for three years", condition = "Myopia onset in childhood",
            evidenceFor = "Adding 40 minutes of outdoor time to the school day reduced three-year myopia incidence compared with usual activity.",
            evidenceLimitation = "The effect is on how many children become short-sighted, not on how strong an existing prescription becomes, and it was measured in children. Nothing here restores adult eyesight or removes a prescription.",
            provenOutcomes = listOf("Myopia incidence in children"),
            unprovenClaims = listOf("Reverses myopia", "Improves adult eyesight", "Replaces a prescription", "Works through a window"),
            digitalReproducibility = DigitalReproducibility.FULL, equipment = "Outdoors",
            digitalValidity = "Nothing to run. The app cannot take you outside.",
            whatYouDo = "Get outdoors in daylight for a couple of hours across the day -- children most of all.",
            whatItTrains = "Nothing trainable. Daylight and distance are the exposure the trials changed.",
            contraindications = "Never look at the sun. Shade, a hat and sunglasses in strong sun; UV exposure carries its own risk.",
            safetyLevel = "SAFE GENERAL DEMONSTRATION", generalUserStatus = GeneralUserStatus.GENERAL_TRAINING,
            reviewStatus = ProfessionalReviewStatus.RESEARCH_REVIEWED, stimulus = StudioStimulus.BREAK_REMINDER,
            dose = DrillDose(3600, 2, "About two hours a day outdoors is where the cohort evidence points; the 2015 school trial itself added only 40 minutes to the day and still shifted myopia onset.", true),
            sources = listOf(outdoorTrial),
            practice = Practice.HABIT,
            howTo = listOf(
                "Aim for roughly two hours of daylight a day, in whatever pieces fit. It does not have to be exercise.",
                "Outside is the point: daylight is tens of times brighter than a lit room, and a window blocks most of what matters.",
                "Move things you would do anyway: a walk at lunch, homework at a garden table, the school run on foot.",
                "It matters most for children, and most of all before short-sightedness has started.",
                "Never look at the sun. In strong sun use shade, a hat and sunglasses.",
                "This does not replace an eye test. A child squinting, or sitting close to the television, needs one."
            )
        )
    )

    /**
     * What the app offers: everything a person can actually do with a phone and what is
     * already in the house.
     *
     * A drill needing an instrument you cannot buy or improvise -- a vectogram, prism
     * bars, an aperture rule, a clinician's printed card -- is not a choice on a list,
     * it is a dead row you can only read. Household substitutes do not count as
     * equipment: a pencil, a warm cloth, knots tied in a string all pass.
     */
    val drills = allDrills.filter { it.digitalReproducibility != DigitalReproducibility.EQUIPMENT_REQUIRED }

    /** Resolves against every record, so an id in a saved plan never dangles. */
    fun byId(id: String): StudioDrill? = allDrills.firstOrNull { it.id == id }
}

private fun ciComponent(
    id: String,
    name: String,
    stimulus: StudioStimulus,
    equipment: String,
    limitation: String,
    grade: EvidenceGrade,
    dose: DrillDose,
    equipmentRequired: Boolean = false,
    howTo: List<String> = emptyList(),
    howToImages: List<Int?> = emptyList(),
    howToVideos: List<Int?> = emptyList(),
    aliases: List<String> = emptyList(),
    additionalSources: List<DrillSource> = emptyList()
) = StudioDrill(
    id = id, name = name, aliases = aliases, issue = EyeIssue.TEAMING, targetSystem = "Vergence and binocular fusion",
    targetFunction = "Convergence awareness and positive fusional vergence",
    clinicalPurpose = "A component used in therapy for diagnosed symptomatic convergence insufficiency.", clinicalUse = true,
    evidenceGrade = grade, evidenceType = "Clinical component within RCT-tested multicomponent programs",
    studiedPopulation = "Primarily children and adolescents with symptomatic convergence insufficiency",
    condition = "Convergence insufficiency", evidenceFor = "Office-based multicomponent therapy improves near point of convergence and positive fusional vergence.",
    evidenceLimitation = limitation, provenOutcomes = listOf("Package-level near point of convergence", "Package-level positive fusional vergence"),
    unprovenClaims = listOf("Independently proven", "Improves healthy eyesight", "Treats every strabismus"),
    digitalReproducibility = if (equipmentRequired) DigitalReproducibility.EQUIPMENT_REQUIRED else DigitalReproducibility.PARTIAL,
    equipment = equipment, digitalValidity = "Digital demonstration of a clinically used procedure; it is not optically equivalent.",
    whatYouDo = "Observe the target relationship and the single/clear binocular goal; therapeutic use requires clinician instruction.",
    whatItTrains = "Clinically, convergence awareness and fusional vergence within a structured program.",
    contraindications = "Stop for persistent double vision, significant dizziness, nausea, pain, or severe headache.",
    safetyLevel = if (equipmentRequired) "HARDWARE-DEPENDENT" else "CONDITION-SPECIFIC",
    generalUserStatus = if (equipmentRequired) GeneralUserStatus.CLINICAL_EQUIPMENT_REQUIRED else GeneralUserStatus.CONDITION_SPECIFIC,
    reviewStatus = ProfessionalReviewStatus.PROFESSIONAL_REVIEW_REQUIRED, stimulus = stimulus, dose = dose,
    sources = (listOf(citt, cittArt, aaoCi, ciMeta) + additionalSources).distinctBy { it.url }, howTo = howTo,
    howToImages = howToImages, howToVideos = howToVideos
)

private fun performanceDrill(
    id: String,
    name: String,
    stimulus: StudioStimulus,
    target: String,
    limitation: String,
    sources: List<DrillSource>,
    dose: DrillDose,
    grade: EvidenceGrade = EvidenceGrade.C,
    aliases: List<String> = emptyList()
) = StudioDrill(
    id = id, name = name, aliases = aliases, issue = EyeIssue.TRACKING, targetSystem = "Extraocular motor system",
    targetFunction = target, clinicalPurpose = "Isolated visual-skill demonstration; not medical treatment.", clinicalUse = true,
    evidenceGrade = grade, evidenceType = if (sources.isEmpty()) "Clinical use / mechanistic task evidence" else "Controlled task-training study",
    studiedPopulation = "Healthy adult laboratory samples; clinical populations vary", condition = "No general medical indication",
    evidenceFor = "Improvement may be limited to the practiced oculomotor task.",
    // Action Statement 4 of the 2022 APTA vestibular guideline is a strong recommendation
    // against exactly this family of drills for one specific group, so every drill built
    // here has to carry it rather than it living on one card.
    evidenceLimitation = "$limitation If you have diagnosed vestibular hypofunction, this is the wrong exercise: the 2022 APTA guideline strongly recommends against saccadic or smooth-pursuit exercises for that condition, because they displace gaze-stability work that does help. Use Gaze Stabilization (VORx1) under your clinician instead.",
    provenOutcomes = listOf("Task-specific performance"),
    unprovenClaims = listOf("Improves visual acuity", "Treats dyslexia", "Improves general eye health", "Helps diagnosed vestibular hypofunction"),
    digitalReproducibility = DigitalReproducibility.FULL, equipment = "None for demonstration",
    digitalValidity = "The motion stimulus is reproducible on screen; therapeutic transfer is not established.",
    whatYouDo = "Keep your head still and follow the target as instructed. Move only within a comfortable range.", whatItTrains = target,
    contraindications = "Stop for double vision, dizziness, nausea, pain, or severe headache.", safetyLevel = "SAFE GENERAL DEMONSTRATION",
    generalUserStatus = GeneralUserStatus.GENERAL_DEMONSTRATION, reviewStatus = ProfessionalReviewStatus.RESEARCH_REVIEWED,
    // The vestibular guideline is cited in the limitation above, so it ships as a source
    // too: a caution the user cannot trace back is not evidence.
    stimulus = stimulus, dose = dose, sources = sources + vestibularCpg
)

/**
 * The drill as something the player can actually run.
 *
 * Every drill becomes one phase at its own dose, so the player needs to know nothing
 * about drill types: it reads the cycle length and the stimulus off this record.
 */
/**
 * How this drill has to be guided.
 *
 * Blinking shuts the eyes, the 20-20-20 break sends them to a distant object, the
 * accommodative rock alternates to a real far target, and palming, the compress and the
 * rim massage are done with the eyes shut or covered. In none of them is the screen being
 * watched at the moment the cue is needed.
 */
val StudioDrill.guidance: DrillGuidance
    get() = when (stimulus) {
        StudioStimulus.BLINK,
        StudioStimulus.BREAK_REMINDER,
        StudioStimulus.FOCUS_SHIFT,
        StudioStimulus.PALMING,
        StudioStimulus.WARM_COMPRESS,
        StudioStimulus.ACUPRESSURE -> DrillGuidance.NON_VISUAL
        else -> DrillGuidance.VISUAL
    }

fun StudioDrill.toProtocol() = Protocol(
    id = "drill_$id",
    title = name,
    tag = issue.label,
    totalSeconds = dose.totalSeconds,
    description = whatYouDo,
    phases = listOf(
        ExercisePhase(
            title = name,
            instruction = whatYouDo,
            durationSeconds = dose.totalSeconds,
            type = stimulus.toExerciseType(),
            physiologicalBenefit = whatItTrains
        )
    ),
    targetSymptom = condition,
    userAdjustable = true,
    evidenceId = id
)

/** Nearest legacy [ExerciseType]. Rendering now follows [StudioStimulus]; this is for logging. */
private fun StudioStimulus.toExerciseType() = when (this) {
    StudioStimulus.BLINK -> ExerciseType.RAPID_BLINK
    StudioStimulus.BREAK_REMINDER,
    StudioStimulus.PALMING,
    StudioStimulus.WARM_COMPRESS,
    StudioStimulus.ACUPRESSURE -> ExerciseType.PALMING_BREATH
    StudioStimulus.EYE_ROM -> ExerciseType.SMOOTH_PURSUIT
    StudioStimulus.SACCADE -> ExerciseType.SACCADE_JUMP
    StudioStimulus.PURSUIT, StudioStimulus.HEMIFIELD_PURSUIT -> ExerciseType.SMOOTH_PURSUIT
    StudioStimulus.SCANNING -> ExerciseType.VISUAL_SEARCH
    StudioStimulus.FIXATION -> ExerciseType.PERIPHERAL_DETECTION
    else -> ExerciseType.ACCOMMODATION_SHIFT
}
