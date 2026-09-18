# Daily Retention Research and Product Plan

Date: 16 September 2026

## Executive decision

The app should build a **care rhythm**, not an exercise addiction. A day is successful after one recommended session. Extra sessions earn no additional streak credit, the weekly goal is five active days out of seven, and rest days are explicitly normal. This retains the useful parts of Duolingo's loop—clear daily success, visible continuity, milestones, and timely reminders—without copying XP grinding, public competition, guilt, or cartoon pressure.

## What the evidence says

- Duolingo's product principles emphasize long-term mechanics, experimentation, and making the core activity enjoyable instead of relying on notification spam.
- Duolingo simplified streak extension to one completed lesson instead of a larger daily XP target. Its reported experiment improved Day-14 retention by 3.3%, overall DAU by 1%, and the share of daily learners maintaining a streak by 10.5%.
- Duolingo reported about 52.7 million daily active users in Q4 2025 and a 39.6% DAU/MAU ratio. This shows the scale of its habit loop, but does not prove that every mechanic suits a health app.
- Friend Streak users were reported as 22% more likely to complete a daily lesson. Accountability may help later, but it must be optional and private in a health context.
- Reviews of mobile-health engagement repeatedly identify self-monitoring, goals, feedback, reminders, visualization, personalization, and rewards as useful components. The evidence is mixed and highly dependent on design quality.
- Health-gamification research also reports drawbacks from poorly timed notifications, competition, and leaderboards. Those mechanisms can create pressure and are unsuitable as the primary loop for eye comfort.

## Product loop for this app

1. **Trigger:** an opt-in break reminder arrives inside the user's active hours.
2. **Tiny action:** the Today screen offers a short, profile-relevant session.
3. **Immediate feedback:** completing it fills today's care dot and confirms that today counts.
4. **Visible investment:** the user sees a real seven-day history, current rhythm, personal best, total care days, and the next milestone.
5. **Healthy stopping rule:** the product states that extra drills add no reward and rest matters.

## Implemented now

- One completed session counts as one care day, regardless of how many sessions are completed that day.
- A live seven-day activity view replaces the previous placeholder chart.
- Current and longest streaks are calculated from distinct local calendar days.
- The current streak stays alive until the end of today when the last completion was yesterday.
- Weekly progress uses a flexible five-of-seven target.
- Permanent milestone targets are 1, 3, 7, 14, 30, 60, 100, 180, and 365 days.
- The Today screen gives a clear next action and a completion state.
- The Insights screen shows real care-day metrics rather than equating session count with consecutive days.
- Fabricated exercise accuracy/category recording was removed.
- Break reminders are opt-in, configurable, limited to active hours and weekdays, and recover after reboot or time-zone changes.

## Deliberately not copied

- No points for repeating exercises.
- No global leaderboard or public shame.
- No punishment copy, loss framing, or aggressive mascot messages.
- No unlimited notification escalation.
- No claim that a streak represents medical improvement.
- No incentive to ignore symptoms or a safety pause.

## Next experiments, in order

1. Ask for reminder timing during onboarding only after the user sees the plan's value; keep the default off.
2. Add reminder actions: **Start**, **Snooze 10 minutes**, and **Done elsewhere**. Measure whether snooze reduces notification disabling.
3. Add a one-tap comfort check-in before and after a session. Personalize tomorrow's plan from comfort, not from engagement alone.
4. Add milestone celebration with restrained motion and a share option that is off by default.
5. Test a home-screen widget that shows only today's status and next break.
6. Consider an optional private accountability partner after privacy controls and blocking/reporting are designed. Do not add a leaderboard.

## Measurement plan

The primary outcome should be **healthy retained use**, not raw DAU.

- Day-1, Day-7, and Day-30 retained users.
- Percentage completing at least one care day per week and at least five of seven days.
- Recommended-plan completion rate.
- Reminder open, snooze, dismissal, and opt-out rates.
- Safety-stop rate and use after symptom warnings.
- Median sessions per active day; a large increase is a potential over-exercise warning, not automatically a success.
- Comfort change, when check-ins are added.

Run experiments for enough time to include weekly behavior. Guardrails should include notification opt-outs, rapid repeat sessions, safety warnings, and app uninstalls. Never ship a retention lift that worsens these signals.

## Sources

- Duolingo, Product Principles: https://blog.duolingo.com/product-principles/
- Duolingo, Improving the Streak: https://blog.duolingo.com/improving-the-streak/
- Duolingo 2025 Form 10-K: https://investors.duolingo.com/static-files/f19d76fb-dee4-4f13-96ae-138ebfd0f2d3
- Duolingo, Product lessons from Friend Streak: https://blog.duolingo.com/product-lessons-friend-streak/
- Duolingo, notification personalization: https://blog.duolingo.com/hi-its-duo-the-ai-behind-the-meme/
- Systematic review of behavior-change techniques in health apps: https://pmc.ncbi.nlm.nih.gov/articles/PMC10545861/
- Review of engagement design in mobile health: https://journals.sagepub.com/doi/full/10.1089/tmj.2021.0176
- Systematic review of gamification and health-app engagement: https://pmc.ncbi.nlm.nih.gov/articles/PMC10028523/
- JMIR review of app engagement features: https://www.jmir.org/2020/12/e21687/
- Systematic review of gamification in health: https://pmc.ncbi.nlm.nih.gov/articles/PMC5073629/
