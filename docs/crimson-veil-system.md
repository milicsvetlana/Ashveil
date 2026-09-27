# Ashveil — Crimson Veil System

## Checkpoint 19

Crimson Veil is a recurring high-danger night event that replaces an ordinary night when scheduled.

The system is intentionally built on top of the existing day/night, enemy spawning, area, save/load and guidance architecture instead of introducing a separate combat framework.

---

## 1. Core behavior

A Crimson Veil is scheduled randomly.

- First Crimson Veil: Day 5 or Day 6.
- After a completed Crimson Veil: the next one is scheduled 4–6 days later.
- The exact day is intentionally unpredictable.
- `nextVeilDay` is persistent.
- `completedVeils` is persistent and is used for difficulty scaling.

State flow:

```text
INACTIVE
→ WARNING
→ ACTIVE
→ RECOVERY
→ INACTIVE
```

### WARNING

The event enters `WARNING` when the scheduled day reaches DUSK.

During this state:

- normal DUSK still exists;
- a subtle Crimson visual warning is shown;
- the first Crimson Veil can trigger a one-time Ceca warning.

### ACTIVE

At the DUSK → NIGHT transition:

- the Crimson Veil becomes active;
- ordinary night spawning is not started;
- existing ordinary enemies/projectiles are cleared when the event begins;
- the normal night timer is frozen;
- dawn cannot happen until the event is completed;
- farming and the rest of the world continue updating;
- inventory, crafting and shop remain usable;
- boat/world-map travel is blocked.

### RECOVERY

After the final wave is cleared:

- the event enters `RECOVERY`;
- a short recovery timer runs;
- after recovery, the game forces DAY;
- the day count advances normally;
- the next Crimson Veil is scheduled.

---

## 2. Wave structure

Crimson Veil uses three waves.

```text
Wave 1 → 25%
Wave 2 → 35%
Wave 3 → remaining 40%
```

The percentages apply to the total Crimson threat budget.

The event reuses the normal enemy threat-cost system instead of hardcoding enemy counts.

Available enemy types still depend on the current area.

This means Crimson Veil can happen on whichever area the player is currently visiting and uses that area's allowed enemy pool.

### Wave transitions

For Waves 1 and 2:

- if all living enemies are dead and there are no pending spawns, a short grace timer starts;
- after the grace period, the next wave begins;
- if the wave is not cleared quickly enough, the next wave begins after the maximum wave duration anyway;
- surviving enemies remain alive, so waves can accumulate.

Wave 3 is different:

- it does not advance because of the maximum wave timer;
- the player must fully clear the final wave.

---

## 3. Grace timer

`clearGraceTimer` is a short pause after a wave has been fully cleared.

Example:

```text
last enemy dies
→ grace timer starts
→ short pause
→ next wave
```

`CRIMSON_VEIL_CLEAR_GRACE_DURATION` defines how long that pause lasts.

The timer resets if the wave is not fully clear.

---

## 4. Threat budget

The base Crimson threat budget is derived from the same ordinary-night progression formula and then increased by Crimson-specific bonuses.

Conceptually:

```text
ordinary night budget
+ Crimson base bonus
+ completed Crimson Veils × scaling bonus
```

This allows later Crimson Veils to become harder without creating new enemy types.

---

## 5. Enemy spawning reuse

`EnemySpawnSystem` was refactored so ordinary nights and Crimson Veil can both use the same threat-budget spawning logic.

The reusable method is conceptually:

```java
startThreatWave(threatBudget, areaID, spawnDuration)
```

Responsibilities stay separated:

- `CrimsonVeilSystem` decides event state, wave number, timers and wave budget.
- `EnemySpawnSystem` decides which enemy types can appear and builds/spawns the queue.
- `World` coordinates the two systems.

This avoids duplicating enemy-selection logic.

---

## 6. Day/night integration

During `ACTIVE` and `RECOVERY`, normal day/night progression is frozen.

The important reason for calling the day/night update with zero delta instead of completely skipping it is that the day/night system also resets transition flags during update.

After recovery, the system forces DAY rather than waiting for the original night duration to expire.

This ensures:

```text
Crimson cleared
→ short recovery
→ dawn
```

instead of:

```text
Crimson cleared
→ wait through another full normal night
```

---

## 7. Death behavior

If the player dies during an active Crimson Veil:

- current Crimson enemies are cleared;
- projectiles are cleared;
- the current spawn queue is cleared;
- the player respawns normally;
- the Crimson Veil remains active;
- the event restarts from Wave 1;
- the night does not end.

The total threat budget is not recalculated because it is still the same Crimson Veil attempt.

---

## 8. Boat and travel behavior

During `ACTIVE` or `RECOVERY`:

- the player cannot open/use world travel;
- `travelToArea(...)` is rejected.

The warning DUSK itself does not lock travel.

---

## 9. Guardian interaction

Guardian encounters and Crimson Veil do not run at the same time.

The existing guardian encounter already takes priority over normal day/night progression.

This prevents overlapping special encounters and keeps the combat state predictable.

---

## 10. Save/load persistence

Crimson Veil has dedicated persistent state.

Saved fields include:

```text
state
nextVeilDay
completedVeils
currentWave
totalThreatBudget
waveTimer
clearGraceTimer
recoveryTimer
```

The existing area save system already stores:

```text
living enemies
projectiles
remaining spawn queue
spawn timer
spawn interval
```

Therefore Crimson persistence only stores the logical event state.

This separation is intentional:

```text
CrimsonVeilSystem
→ logical encounter state

AreaRuntime / EnemySpawnSystem
→ physical runtime enemy/spawn state
```

A save made during Wave 2 can therefore restore:

- NIGHT;
- Crimson ACTIVE state;
- Wave 2;
- currently living enemies;
- pending spawns;
- current wave timer;
- the same future Crimson schedule.

Older saves without Crimson data are allowed to load and simply keep the newly constructed default Crimson schedule.

---

## 11. Visual feedback

Crimson Veil uses a darker night-style environment tint with a red/crimson cast.

The goal is not to make the screen look like a red daytime filter.

The intended visual reading is:

```text
night
+ subtle crimson tone
```

Further atmosphere such as special sky, moon, particles, music or clock styling is polish work and is not required for the core checkpoint.

---

## 12. Ceca warning

The first Crimson Veil can show a one-time contextual Ceca message.

English:

```text
That color... I was hoping you wouldn't see it this soon.
The Veil is falling. Once it does, there's no leaving until it breaks.
```

Serbian Latin:

```text
Ta boja... Nadala sam se da je nećeš videti ovako rano.
Veo se spušta. Kada padne, nema odlaska dok ne popusti.
```

The message uses the existing contextual guidance persistence so it is shown only once per save.

A separate `CRIMSON VEIL` title at the exact NIGHT transition is optional presentation polish and is not required for the functional checkpoint.

---

## 13. Why a GameEvent is used

The Crimson warning is represented as a semantic gameplay event instead of directly making `World` control Ceca UI behavior.

Preferred flow:

```text
World detects gameplay condition
→ emits GameEvent.CRIMSON_VEIL_WARNING
→ GuidanceSystem decides which GuideStep is relevant
→ Guidance UI displays localized text
```

This preserves the existing event-driven guidance architecture.

`World` should know that a Crimson warning happened, but it should not contain Ceca-specific presentation logic.

Not every older contextual message currently uses a `GameEvent`, because some island guidance was added through direct contextual activation/request flow. The Crimson warning uses an event because it naturally originates from a gameplay transition and fits the intended architecture cleanly.

---

## 14. Final configuration

Final Crimson scheduling:

```java
CRIMSON_VEIL_FIRST_DAY_MIN = 5;
CRIMSON_VEIL_FIRST_DAY_MAX = 6;

CRIMSON_VEIL_INTERVAL_MIN = 4;
CRIMSON_VEIL_INTERVAL_MAX = 6;
```

Final event timing/balance values:

```java
CRIMSON_VEIL_BASE_BONUS_BUDGET = 10;
CRIMSON_VEIL_COMPLETED_BONUS_BUDGET = 4;

CRIMSON_VEIL_WAVE_MAX_DURATION = 25f;
CRIMSON_VEIL_CLEAR_GRACE_DURATION = 5f;
CRIMSON_VEIL_WAVE_SPAWN_DURATION = 8f;
CRIMSON_VEIL_RECOVERY_DURATION = 3f;
```

The temporary short DAY/DUSK test values must be restored to the project's normal day/night values before the checkpoint commit.

Temporary `System.out.println(...)` debug output should also be removed.

---

## 15. Main classes involved

```text
CrimsonVeilState
CrimsonVeilSystem
World
DayNightCycle
EnemySpawnSystem
WorldRenderer
GuideStep
GuidanceSystem
GameEvent
SaveData
CrimsonVeilSaveData
SaveMapper
SaveValidator
Config
```

---

## 16. Final test checklist

Before closing CP19:

- first Crimson is scheduled for Day 5 or 6;
- scheduled DUSK enters WARNING;
- Crimson NIGHT starts instead of an ordinary night;
- Wave 1, Wave 2 and Wave 3 spawn correctly;
- early wave clear uses grace delay;
- slow Wave 1/2 allows enemy accumulation;
- Wave 3 must be fully cleared;
- dawn cannot happen during ACTIVE;
- boat/world-map travel is blocked during ACTIVE/RECOVERY;
- death restarts the Crimson Veil at Wave 1;
- save/load during an active wave restores the same state;
- recovery ends in DAY;
- next Crimson is scheduled 4–6 days later;
- ordinary nights still work outside Crimson Veil.

---

## 17. Defense summary

Crimson Veil is implemented as a dedicated encounter state machine that coordinates existing systems instead of replacing them.

The design demonstrates:

- enum-based state modeling;
- encapsulation of event-specific state;
- reuse of existing enemy spawning logic;
- separation of responsibilities;
- persistence through DTO mapping;
- validation of restored state;
- integration with day/night and area systems;
- event-driven guidance;
- explicit death and travel rules.

The important architectural idea is that Crimson Veil decides **when and how the special event progresses**, while existing systems remain responsible for their own domains.
