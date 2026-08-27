# Day/Night, Enemy Spawning and Ordinary Night Loop

## Overview

Checkpoint 12 completes the ordinary survival loop built around the existing combat and enemy-AI systems.

The world now progresses through three time phases:

```text
DAY → DUSK → NIGHT → DAY
```

The first day is intentionally longer to leave more time for the future tutorial and initial progression.

Regular later cycles use the current test balance:

```text
DAY   = 108 s
DUSK  = 18 s
NIGHT = 54 s
```

The full regular cycle therefore lasts 180 seconds.

These values are balance constants and can be changed later without changing the architecture.

---

## DayPhase and DayNightCycle

`DayPhase` represents the current part of the cycle:

```text
DAY
DUSK
NIGHT
```

`DayNightCycle` stores:

- the current phase;
- the timer of the current phase;
- the configured duration of the current phase;
- the current day number;
- transition flags for entering NIGHT and entering DAY.

The first constructed DAY uses `FIRST_DAY_DURATION`.
After that, regular DAY/DUSK/NIGHT durations are used.

`phaseTimer` tracks elapsed time inside the current phase.

When a phase finishes:

```text
DAY
→ DUSK

DUSK
→ NIGHT
→ justBecameNight = true

NIGHT
→ DAY
→ dayCount++
→ justBecameDay = true
```

`getPhaseProgress()` exposes normalized phase progress:

```text
0.0 → phase just started
1.0 → phase is ending
```

This value is intended to be reused later by the circular Don't-Starve-style clock HUD.

---

## Dusk

DUSK is a warning phase.

It is technically still a safe part of the day:

- ordinary enemy spawning does not begin during DUSK;
- the player can use it to prepare for NIGHT;
- a warm overlay gives visual warning that NIGHT is approaching.

Enemy spawning starts only when the cycle actually enters `NIGHT`.

---

## Night Threat Budget

Ordinary nights use a threat-budget system instead of a fixed enemy count.

Current test values are:

```text
initial threat budget = 3
increase per later day = 2
```

Conceptually:

```text
Night 1 → 3 tokens
Night 2 → 5 tokens
Night 3 → 7 tokens
...
```

Enemy types have individual threat costs:

```text
SHADE  = 1
WISP   = 2
WRAITH = 3
```

These values are temporary balance data.

The budget system is intentionally independent from enemy AI.

---

## Progression-Aware Night Roster

Shade is always available for ordinary nights.

Wisp and Wraith are progression unlocks.

The intended progression is:

```text
start of game
→ ordinary nights can spawn Shade

complete the relevant first island
→ Wisp becomes permanently available for ordinary nights

complete the relevant later island
→ Wraith becomes permanently available for ordinary nights
```

Island-local enemy rosters are separate from the ordinary-night roster.
An island may contain a specific enemy before that enemy becomes available on the main island at night.

`ProgressionState` stores the permanent Wisp/Wraith ordinary-night unlock state so it can later be saved in JSON.

---

## EnemySpawnSystem

`EnemySpawnSystem` owns ordinary enemy-spawn rules.

It is responsible for:

- building the spawn queue at the start of NIGHT;
- consuming the threat budget;
- choosing randomly among currently unlocked affordable enemy types;
- spacing enemy spawns over the early part of NIGHT;
- finding valid spawn positions around the player;
- constructing the selected Shade, Wisp or Wraith;
- adding the created enemy to the shared enemy collection.

It is not responsible for:

- enemy AI;
- combat;
- player damage;
- day/night timing;
- island progression;
- Crimson Veil rules.

### Queue generation

When NIGHT starts:

```text
calculate current threat budget
↓
build list of unlocked enemy types
↓
choose a random affordable type
↓
add it to the queue
↓
subtract its threat cost
↓
repeat until the remaining budget cannot buy another type
```

The queue uses ordinary Java randomization consistently with the rest of the project.

### Spawn timing

The queue is distributed across the early portion of NIGHT.

Current implementation uses approximately the first 70% of the night for new spawns.

This leaves the final part of NIGHT without additional ordinary spawns, giving already-created enemies time to reach and fight the player before dawn.

---

## Spawn Area

Enemies do not spawn anywhere on the entire map.

Spawn positions are searched inside a minimum/maximum tile-distance ring around the player's current position.

This avoids two problems:

- an enemy appearing directly beside or visibly on top of the player;
- an enemy spawning on the far side of the large map and never reaching the player before dawn.

Spawn search uses a bounded number of attempts instead of an unlimited random loop.

Ground enemy spawn positions must:

- be inside map bounds;
- be outside the minimum distance;
- be inside the maximum distance;
- not overlap another enemy;
- not be blocked for ground movement;
- be reachable through the shared distance field.

Shade reachability uses `NavigationMode.BREAK_FENCES` because Shade can destroy a Fence that blocks its path.

Wraith uses normal ground navigation.

Wisp is a flying enemy and may approach across water, while obviously invalid spawn overlap with world objects is still avoided.

---

## Dawn and Fleeing

Ordinary NIGHT ends when its timer expires even if enemies are still alive.

At dawn:

```text
NIGHT → DAY
↓
remaining ordinary spawn queue is stopped
↓
every still-ALIVE enemy enters FLEEING
```

Enemies already in `DYING` are not changed and finish their normal death lifecycle.

### FLEEING state

`FLEEING` is a shared `EnemyState`.

When an enemy begins fleeing:

1. it determines the nearest edge of the world once;
2. it stores a target point slightly beyond that edge;
3. normal AI is disabled;
4. it moves rapidly and directly toward the stored target;
5. collision is ignored during the escape;
6. it cannot attack;
7. it cannot receive damage;
8. it becomes partially transparent;
9. it is removed after it reaches the off-map target.

Ignoring collision during fleeing prevents enemies from becoming permanently trapped behind fences or other world objects after they are no longer allowed to attack.

### Gold rule

Removal and killing are deliberately different.

```text
DYING enemy removed
→ normal Gold roll

FLEEING enemy removed
→ no Gold
```

An enemy killed immediately before dawn remains `DYING`, finishes the death timer, and still uses the normal reward flow.

Already-fired Wraith projectiles are not removed at dawn; they continue their existing lifecycle naturally.

---

## Real-Time Gameplay Interfaces

Inventory, Crafting, Shop and Chest interfaces no longer pause world simulation.

While these interfaces are open:

```text
world time continues
enemy AI continues
projectiles continue
farming continues
night spawning continues
```

The player receives neutral gameplay input so UI navigation does not also move, attack or use items.

This means the player may still take damage while using a gameplay interface.

If the player dies while an interface is open, the interface is closed before the normal death transition continues.

---

## True Pause

A dedicated `PAUSE` overlay is the only in-game interface that stops `World.update()`.

Current Pause Menu contains:

```text
Continue
```

A future Main Menu action can be added after the real Main Menu screen exists.

ESC behavior follows context:

```text
MENU + ESC
→ close MENU

CHEST + ESC
→ close CHEST

targeting + ESC
→ cancel targeting

normal gameplay + ESC
→ open PAUSE

PAUSE + ESC
→ continue
```

The Continue button uses a `Runnable` callback supplied by `GameScreen`.

`Runnable` is used only as a synchronous callback here; no new thread is created.

A method reference such as:

```text
this::closePause
```

passes the current `GameScreen.closePause()` method as that callback.
It is equivalent in purpose to:

```text
() -> closePause()
```

---

## Future Extensions

The current architecture is prepared for later systems without implementing them inside CP12.

### Circular clock HUD

The future clock can render:

- DAY;
- DUSK;
- NIGHT;
- current phase progress.

It should read `DayNightCycle` state rather than keeping its own gameplay timer.

### Boat travel time

Area travel may later advance game time by a fixed amount.
The current design keeps time progression inside `DayNightCycle` so travel can reuse the same phase transitions.

### Crimson Veil

Crimson Veil will reuse the day/night foundation but apply different event rules.

The intended distinction is:

```text
ordinary NIGHT
→ timer expires
→ surviving enemies flee
→ dawn continues

Crimson Veil
→ ordinary dawn is prevented until the event is cleared
→ event may use explicit enemy waves
→ clock receives Crimson Veil visual treatment
```

### Area-specific spawning

Mini-islands may use local enemy rosters and spawn profiles independent of the main-island ordinary-night roster.

This allows a Wisp-focused island, for example, to contain Wisps before Wisp is permanently unlocked for ordinary main-island nights.
