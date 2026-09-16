# Global eye-exercise review and Studio decisions

Research checked: 15 September 2026

## Bottom line

“Eye exercise” is not one treatment category. The evidence and safe use depend on the
target problem:

- **Blink and break behaviours** can help some symptoms of dry eye or digital eye
  strain. They do not improve refractive error or visual acuity.
- **Vergence/accommodative therapy** can be appropriate for a diagnosed binocular or
  focusing disorder. The best evidence is for a clinician-delivered, multicomponent
  programme in symptomatic convergence insufficiency—not for a generic exercise used
  by everyone.
- **Tracking, figure-eight and range-of-motion tasks** reproduce an eye-movement task,
  but evidence that they improve healthy eyesight, reading, myopia, presbyopia or
  general eye health is absent or insufficient.
- **Relaxation/comfort practices** such as palming may feel restful, but there is no
  credible evidence that they change eyesight. Palming must not include pressure on the
  eyes.

For that reason, Studio contains both general wellness behaviours and professional
orthoptic demonstrations, but only entries marked `GENERAL_TRAINING` may enter an
automatic daily plan.

## What the three supplied articles contain

The [Dr Agarwal article](https://www.dragarwal.com/blog/eye-wellness/simple-eye-exercises-that-will-improve-your-vision-and-eyesight/),
[Kraff Eye article](https://kraffeye.com/blog/8-easy-eye-exercises-to-improve-vision-techniques-and-tips),
and [Healthline overview](https://www.healthline.com/health/eye-health/eye-exercises)
mostly use different names for the same small set of practices. Healthline also states
the important limitation: exercises have not been shown to correct myopia,
hyperopia, astigmatism or presbyopia, and formal vision therapy is specialized care.

| Article term | Studio decision | Why |
|---|---|---|
| Blinking | Existing `Complete Blink + Gentle Squeeze` | A 2025 randomized dose-finding trial supports a specific short-term dry-eye symptom/blink-completeness claim. No eyesight claim. |
| 20-20-20 | Existing `20-20-20 Break Reminder` | A small uncontrolled intervention supports short-term symptom relief; most clinical signs did not change. |
| Pencil push-up, zooming, thumb/pen convergence | Existing `Pencil / Near Target Push-Up`; article names added as aliases | This is a convergence procedure, not a way to sharpen normal eyesight. Home push-ups underperformed office-based therapy in children. |
| Near/far focus, refocusing, focus change | Existing `Near / Far Accommodative Rock`; aliases added | A phone cannot create a true far accommodative target. Clinical benefit comes from assessed, multicomponent care. |
| Figure eight, infinity tracing, lazy eight | Existing `Figure-Eight Pursuit`; aliases added | The motion can be reproduced, but health/vision transfer is unproven. |
| Around the world, eye rolls | Existing `Full-Range Gaze`; aliases added | Kept as a gentle movement demonstration, not an eyesight treatment. |
| Palming | Existing `Palming / Dark Rest`; aliases added | Comfort only. Instructions prohibit rubbing or pressing the globes. |
| Brock string | Existing `Brock String` | A recognized component of vergence therapy. It requires a real string/beads and appropriate clinical context. |
| Barrel card | **Added** as `Barrel-Card Convergence` | Present in office-based vergence/accommodative protocols, but its independent effect is not established. Requires the physical card and clinician direction. |

Adding duplicate cards for “zooming,” “refocusing,” or “infinity tracing” would make the
library look larger while repeating the same stimulus and dose. Studio therefore shows
these as searchable/visible aliases beneath the canonical exercise.

## Second pass, 16 September 2026

The same three articles were re-read against the shipped library to check for anything
missing. **No new exercise was found.** All fifteen distinct practices named across the
three articles already exist as drills; every remaining difference was wording.

What the re-read did surface was duplication in the alias data itself:

- `Focus change` was listed on **both** `Pencil / Near Target Push-Up` and
  `Near / Far Accommodative Rock`. Healthline's "Focus Change" moves a finger away and
  back while holding focus, which is the accommodative near/far task, not a convergence
  push-up. It now belongs to the accommodative rock alone.
- `Figure of eight`, `Blinking`, `20-20-20 rule`, `Pencil push-ups`,
  `Near and far focusing` and `Roll your eyes` are the exact words the articles use and
  were not aliases of anything. Added to their canonical drills.

Two tests now hold this in place rather than a written policy:

- `every exercise named in the source articles resolves to one drill` fails if an article
  term stops mapping, so a future contributor adding "Zooming" as its own card sees the
  existing owner first.
- `no alias is claimed by two drills` fails if one name is attached to two exercises,
  which is how a deduplicated list quietly re-duplicates.

## Web search for missing exercises, 16 September 2026

A wider search was run for exercises the library did not already contain. One was added,
one protocol was rejected, and one finding changed existing entries.

### Added: Gaze Stabilization (VORx1)

The clearest gap. Gaze stability exercises hold a target in focus *while the head moves*,
which no existing drill does -- every tracking drill in the library keeps the head still.

The [2022 APTA clinical practice guideline](https://pubmed.ncbi.nlm.nih.gov/34864777/)
(Hall et al, J Neurol Phys Ther 2022;46:118-177) rates gaze stability exercises as having
strong evidence of effectiveness in peripheral vestibular hypofunction. A Level I trial in
chronic unilateral loss brought 12 of 13 patients to normal dynamic visual acuity while no
placebo patient improved. Dose in the trials was one-minute bouts totalling 20-40 minutes
daily; the guideline sets a minimum of three sessions a day.

It is marked `CLINICIAN_SUPERVISED`, not general training. Every trial enrolled people whose
loss was confirmed by caloric or rotational-chair testing, which the app cannot do, and
dizziness has causes that need assessment rather than exercise. The drill needs no
equipment -- the phone is the fixation target -- so it is `FULL` reproducibility.

### Rejected: the "I-Exercises" protocol

A [2025 RCT](https://pmc.ncbi.nlm.nih.gov/articles/PMC13505804/) reported symptom
improvement from a seven-stage ocular module. It is not safe to ship:

- Stage 1 includes **sunning**, a Bates-method practice of exposing the eyes to the sun.
  Deliberate sun gazing risks solar retinopathy. That alone disqualifies the protocol.
- It also includes Tibetan eye-chart movements and Trataka candle gazing, neither of which
  has a credible mechanism for eyesight.
- The trial was n=40, unblinded, with no sham arm, subjective outcomes only, no objective
  ophthalmic measure and no follow-up.
- Its defensible components -- blinking, pencil push-ups, circular eye movements, acupoint
  massage -- are already in the library individually.

### Changed: a strong recommendation against our own tracking drills

Action Statement 4 of the same guideline reads: "Clinicians should not offer saccadic or
smooth-pursuit exercises to patients with unilateral or bilateral vestibular hypofunction.
(Evidence quality: I; Recommendation Strength: Strong)" -- a preponderance of *harm* over
benefit, because they displace gaze-stability work that does help.

The library offers six such drills. They are fine as general demonstrations, which is how
they are classified, but they were silent on this. Every drill built by `performanceDrill`
now carries the caution in its limitation, lists "Helps diagnosed vestibular hypofunction"
as an unproven claim, and ships the guideline as a citable source.

### Checked and already covered

Chinese eye exercises of acupoints (`Orbital Rim Massage`), Hart-chart rock, near/far
focusing, figure-eight tracking, convergence push-ups and blink training were all already
present. The [meta-analysis of Chinese eye exercises](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC10036375/)
found no significant myopia association after covariate adjustment, matching the app's
existing refusal to make an eyesight claim for massage.

## Additional professional orthoptic tools added

### Dot-Card Convergence

Dot cards are described in UK NHS orthoptic guidance for people assessed with
convergence weakness. The phone shows an educational animation, but treatment requires
the physical card, prescribed correction/prism if applicable, and the schedule set by
an orthoptist. It is evidence grade D as an individual component because trials support
multicomponent programmes rather than isolating the dot card.

### Free-Space Fusion Stereogram

Added with the common names cat stereogram, bucket stereogram, LifeSaver card and
free-space fusion card. Near and distance versions use different viewing strategies;
selecting the wrong version is not a harmless variation. The app therefore gives setup
and stop guidance but marks the exercise clinician-guided and equipment-required.

### Barrel-Card Convergence

Added as a physical red/green card procedure. A 2022 randomized pilot of a complete
office programme included barrel cards alongside Brock string, vectograms, LifeSaver
cards, aperture rule, eccentric circles and prisms. That supports the programme—not a
claim that the barrel card alone treats convergence insufficiency.

The library already contained the other commonly used professional components found in
clinical programmes: Brock string, vectogram fusion, random-dot stereograms, aperture
rule, accommodative rock and prescribed lens-flipper work.

## Evidence calibration used in the app

### Convergence insufficiency

The [2020 Cochrane network meta-analysis](https://pubmed.ncbi.nlm.nih.gov/33263359/)
found that office-based vergence/accommodative therapy with home reinforcement was more
effective for several outcomes in children than home computer therapy or pencil/target
push-ups. Evidence in adults was less clear. The
[American Academy of Ophthalmology evidence report](https://pubmed.ncbi.nlm.nih.gov/34172337/)
reached a similar cautious conclusion. Neither supports prescribing one universal drill
to users based only on age, screen time or glasses.

The [2022 randomized pilot](https://pubmed.ncbi.nlm.nih.gov/35448970/) compared VR-based
therapy with a multicomponent office-based programme. It helps document professional
protocol components but cannot assign the programme’s outcome to one card or device.

### Dry eye and screen strain

The [2025 blinking trial](https://pubmed.ncbi.nlm.nih.gov/40467388/) found short-term
improvement in symptoms, incomplete blinks and conjunctival staining with 15
close–squeeze–open cycles three times daily for two weeks. Several tear measures did not
improve and much of the gain returned to baseline after stopping, so the app states the
tested outcome and its limits.

The [20-20-20 intervention study](https://pubmed.ncbi.nlm.nih.gov/35963776/) reported
short-term symptom improvement, but it was small and uncontrolled and most measured
signs did not change. The [TFOS digital-environment review](https://pubmed.ncbi.nlm.nih.gov/37062428/)
supports addressing blinking, breaks and the work environment as symptom-management
behaviours, not eyesight correction.

### Myopia and eyesight claims

The [Cochrane review of interventions for slowing myopia progression](https://pubmed.ncbi.nlm.nih.gov/37740051/)
does not support eye exercises as a way to reverse refractive error. Exercises in Studio
therefore never claim to remove glasses, reverse myopia, restore presbyopic focusing or
increase visual acuity in healthy eyes.

## Why these professional drills do not enter Daily automatically

The daily-plan questionnaire measures age, screen exposure, correction use, symptoms,
known clinical context and urgent warning signs. Those answers can reasonably select
low-risk wellness behaviours. They cannot measure near point of convergence, fusional
vergence, suppression, ocular alignment, accommodation or the correct stereogram
direction.

Consequently:

1. Dot card, free-space stereogram, barrel card, Brock string, vectogram, aperture rule
   and lens-flipper work remain in Training/Studio for education and clinician-directed
   use.
2. Equipment-dependent additions are tagged `CLINICAL_EQUIPMENT_REQUIRED`.
3. The automatic plan accepts only `GENERAL_TRAINING` entries.
4. A report of diagnosed convergence insufficiency, amblyopia/eye turn or neurological
   recovery displays a clinical-care message; it does not unlock a treatment protocol.

## Safety boundary

NHS instructions for [dot-card and stereogram exercises](https://www.fhft.nhs.uk/patients-and-visitors/patient-information-library/orthoptic-exercises/submit/7561)
and [dot-card practice](https://www.ulh.nhs.uk/patients/patient-information-library/how-to-carry-out-your-dot-card-exercises/)
were used for setup and stopping guidance. The app tells users to stop for persistent
double vision, eyes remaining crossed after relaxation, significant headache, nausea,
dizziness, pain or a new visual disturbance. Sudden vision loss, flashes with a curtain,
new neurological symptoms, injury or recent eye surgery are outside the training flow
and need appropriate clinical assessment.

This is a research-informed product classification, not a diagnosis or an individualized
vision-therapy prescription. The professional-review status stays visible in the data so
an optometrist/orthoptist can approve or revise these entries before clinical deployment.
