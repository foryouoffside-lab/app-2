# Reduce text clutter: exercise picker + Profile page

Two related cleanup passes on this vision-training app, focused on cutting text
clutter. Don't touch anything else.

## 1. Remove subtitles from the exercise picker

File: `app/src/main/java/com/example/ui/CustomRoutineDialog.kt`

The "Select Exercises" dialog (used when building a custom routine) shows each
drill as: name + duration on one line, then a description line underneath
(`template.whatYouDo`). Remove that description line entirely — each row
should just be checkbox + name + duration, nothing else. Check whether the
row's padding/height should tighten up now that it's one line instead of two,
so the list doesn't look overly spaced out.

## 2. Reduce crowding on the Profile page

File: `app/src/main/java/com/example/ui/screens/ProfileScreen.kt`

Go section by section (Eye Comfort, Age, Daily Plan, Display & Sound, Screen
Breaks) and:

- Convert any 2-line detail/description text under a heading to 1 line where
  the meaning survives — shorten the wording rather than just truncating.
- Remove a subtitle/detail line entirely if it's not telling the user
  something they need (e.g. restating what the control obviously does).
- Cut any other text that isn't pulling weight — labels, captions, footnotes.
- Don't remove anything safety- or permission-related (e.g. the
  "notifications blocked" hint, the battery-saver/reschedule note) — those
  exist because silent failures were already a reported problem here.
- Goal is a visibly less busy page, not a redesign — keep the same sections
  and controls, just tighter copy.

## When done

Build and install on a connected device/emulator afterward and visually
confirm both changes.
