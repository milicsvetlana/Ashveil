# Windy Plains – Progression, Guardian Encounter, Scroll I, Swift Step and Hazards

## Overview

`Windy Plains` is the first outer island in Ashveil progression.

The area establishes several reusable systems that later islands can build on:

- area-specific enemy availability;
- guardian encounters;
- sealed guardian chests;
- permanent ward-clear progression;
- Scroll progression;
- reusable reward-card UI;
- Ceca contextual guidance for outer islands;
- tile-based hazards;
- Dash / Swift Step traversal;
- natural-spawn exclusion zones;
- persistent island progression across travel and save/load.

The important design goal is that Windy Plains is not implemented as a one-off scripted level.
Instead, it establishes systems that can later be parameterized for Darkroot Isle and Veilscar Passage.

---

# 1. Windy Plains role in progression

The intended progression is:

```text
Main Island
    ↓
Boat / World Map
    ↓
Windy Plains
    ↓
Guardian encounter
    ↓
Swift Step unlocked
    ↓
Scroll I obtained
    ↓
Wisp becomes globally available at night
    ↓
Scroll I read
    ↓
Darkroot Isle unlocked
```

Several progression conditions are deliberately separate.

Clearing the guardian does **not** automatically complete every Windy-related progression step.

### Guardian cleared

Unlocks:

```text
Swift Step / Dash
guardian chest becomes accessible
```

### Scroll I removed from the chest / acquired

Unlocks:

```text
Wisp ordinary-night global progression
```

### Scroll I read

Unlocks:

```text
Darkroot Isle
Ceca Scroll I reaction
```

This separation allows the player to clear the guardian and leave the Scroll behind without incorrectly advancing later story progression.

---

# 2. Area runtime architecture

Ashveil uses a separate `AreaRuntime` for each initialized area.

The world stores them in:

```java
Map<AreaID, AreaRuntime> areaRuntimes
```

The active area is referenced through:

```java
currentAreaRuntime
```

Windy Plains therefore owns its own runtime systems such as:

- `TileMap`;
- `CollisionSystem`;
- `DestructibleObjectSystem`;
- `WorldItemSystem`;
- `ProjectileSystem`;
- enemy list;
- `EnemySpawnSystem`;
- navigation / `DistanceField`;
- farming state.

When the player leaves Windy Plains, its persistent runtime data is not replaced by Main Island data.

Returning to Windy reuses the existing runtime instead of creating the entire island again.

---

# 3. Important Tiled conventions

Windy Plains uses data from Tiled rather than hardcoded world coordinates.

## Objects layer

Important named objects include:

```text
dock_arrival
scroll_chest_spawn
NoNaturalSpawn
```

Multiple rectangle objects may use the same name:

```text
NoNaturalSpawn
```

For example:

- one around the arrival / dock area;
- one around the guardian arena.

These rectangles stop procedural natural resources from spawning in reserved gameplay zones.

The Java code reads `NoNaturalSpawn` objects from the normal `Objects` object layer.

A separate `NoNaturalSpawn` layer is not required.

---

## SpecialRegions layer

The guardian combat area is represented by:

```text
guardian_arena_region
```

This rectangle determines the region in which guardian enemies are spawned.

The encounter system itself does not need hardcoded world coordinates.

---

# 4. NoNaturalSpawn system

`TileMap.isNaturalSpawnBlocked(...)` checks whether a candidate natural-spawn tile overlaps a rectangle object named:

```text
NoNaturalSpawn
```

This is used by procedural resource generation.

The result is that trees and rocks cannot spawn:

- around the dock;
- over the guardian chest;
- inside reserved guardian space;
- in any later map region explicitly marked with the same convention.

The same method also rejects hazard tiles.

Conceptually:

```text
candidate natural resource tile
        ↓
out of bounds?
        ↓
hazard?
        ↓
inside NoNaturalSpawn rectangle?
        ↓
other normal placement checks
        ↓
spawn allowed
```

This keeps map-specific reserved spaces in Tiled rather than hardcoding them in Java.

---

# 5. Windy ordinary enemy spawning

Windy Plains introduces `Wisp` as a normal local enemy.

Before Scroll I progression affects the global pool:

```text
Main Island ordinary night:
Shade

Windy Plains ordinary night:
Shade + Wisp
```

Later, after the Windy Scroll progression unlocks Wisps globally:

```text
Main Island:
Shade + Wisp

Windy Plains:
Shade + Wisp
```

The later global Wraith unlock follows the same general architecture.

Enemy availability is area-aware through `EnemySpawnSystem`.

The area-specific spawn pool is separate from the progression-based global unlocks.

---

# 6. Guardian encounter model

Guardian encounters use:

```text
GuardianEncounter
GuardianEncounterState
```

The important states are:

```text
INACTIVE
ACTIVE
CLEARED
```

## INACTIVE

The guardian ward has not currently been triggered.

The guardian chest remains sealed.

Pressing interact on the sealed guardian chest starts the encounter.

---

## ACTIVE

The special guardian wave is running.

During this state:

- ordinary enemies are cleared;
- current projectiles are cleared;
- ordinary spawning is suspended;
- the guardian wave is spawned;
- the special guardian darkness override is active;
- the normal day/night cycle does not advance while the encounter is active;
- interacting with the chest does not open it.

Windy Plains final guardian composition is:

```text
10 Shade
10 Wisp
```

The enemies are spawned inside:

```text
guardian_arena_region
```

---

## CLEARED

When all guardian enemies are gone:

```text
GuardianEncounter
    ACTIVE
      ↓
    CLEARED
```

The permanent progression state is also updated.

Windy completion grants:

```text
windy ward cleared
Swift Step / Dash unlocked
Swift Step reward card requested
```

The guardian chest is then rendered as a normal closed chest and can be opened.

---

# 7. Temporary encounter state vs permanent ward state

The guardian encounter's runtime state and the progression ward state are different concepts.

## GuardianEncounter state

Represents the **current attempt**.

Example:

```text
INACTIVE
→ ACTIVE
→ death
→ INACTIVE
```

It is useful for runtime encounter behavior.

---

## Progression ward state

Represents permanent progression:

```text
windyWardCleared
```

Once the player genuinely clears the encounter:

```text
false → true
```

and it stays true across:

- travel;
- revisits;
- save/load.

This is why the project does not rely only on `GuardianEncounterState.CLEARED`.

The encounter object is runtime gameplay state.
`ProgressionState` stores the permanent fact that the ward has been defeated.

The same pattern will be reused for later islands with concrete progression fields rather than a vague generic `areaCompleted` flag.

---

# 8. Guardian reset rules

An ACTIVE guardian attempt is deliberately not permanent.

If the player:

- dies;
- travels away during the encounter;

the current guardian attempt is reset.

Reset behavior:

```text
guardian enemies cleared
guardian projectiles cleared
ACTIVE → INACTIVE
guardian darkness disabled
chest becomes sealed again
```

The player can then return and retry the encounter.

However, once:

```text
windyWardCleared == true
```

travel and death must never reseal the completed ward.

This gives the distinction:

```text
failed / abandoned attempt
    → retry later

completed ward
    → permanent
```

---

# 9. Guardian chest

Windy Plains uses a special chest kind:

```text
ChestKind.GUARDIAN
```

The guardian chest is created at the Tiled point:

```text
scroll_chest_spawn
```

Its contents are explicitly replaced so the intended reward is deterministic.

Windy chest contains:

```text
slot 7 / 15:
Scroll I
```

The chest is non-destructible.

Its visual state is selected through:

```text
ChestVisualState.SEALED
ChestVisualState.CLOSED
ChestVisualState.OPEN
```

Conceptually:

```text
guardian not cleared
    → SEALED

guardian cleared
    → CLOSED

currently opened
    → OPEN
```

This keeps rendering state derived from actual gameplay state.

---

# 10. Guardian darkness

The guardian encounter does not modify the actual `DayNightCycle` phase just to make the fight dark.

Instead, rendering has a guardian-specific darkness override.

This is important because:

```text
visual darkness
```

and:

```text
actual world time / DayPhase
```

are different concerns.

During the guardian encounter the renderer applies the dark overlay while the day/night system itself is preserved.

Once the encounter ends or resets, the special override disappears.

---

# 11. SpriteBatch alpha-state bug fixed during Windy work

A rendering bug appeared where the guardian chest became transparent when enemy render alpha was below `1`.

The cause was `SpriteBatch` render state leaking from `EnemyRenderer`.

After drawing an enemy, the batch color must be restored to:

```java
batch.setColor(1f, 1f, 1f, 1f);
```

and not to the enemy's current render alpha.

`SpriteBatch.setColor(...)` mutates shared batch state, so temporary tint or alpha values must be reset after the special draw call.

This was especially visible because enemies are rendered before the chest using the same batch.

---

# 12. Swift Step / Dash

The Windy guardian reward is the player ability displayed as:

```text
Swift Step
```

The gameplay action is Dash.

Input:

```text
Left Shift
```

Dash is progression-gated:

```text
if Dash is not unlocked
    → Shift does nothing
```

After the Windy guardian is cleared:

```text
progressionState.unlockDash()
```

allows the player to use it.

The final dash distance was increased to support meaningful Windy pit traversal:

```text
4 tiles
```

The dash still respects normal collision such as:

- walls;
- blocked terrain;
- collidable objects.

Hazards are **not** collision obstacles.

---

# 13. Swift Step reward card

Guardian completion also requests a reusable reward presentation:

```text
RewardType.SWIFT_STEP
```

The reward card is UI-only and does not represent the actual persistent progression state.

The ability is unlocked in gameplay state first.

The UI then presents the reward visually.

Important behavior:

- centered overlay;
- non-pausing;
- dim background;
- pop / fade animation;
- temporary presentation;
- not saved as persistent progression.

This distinction prevents a UI request from becoming the source of truth for gameplay progression.

---

# 14. Scroll I

Windy Plains guardian chest contains:

```text
Scroll I — The First Binding
```

Scroll items are physical inventory items.

They:

- can be picked up;
- can be dropped;
- do not despawn on the ground;
- can be read repeatedly.

The Scroll UI is separate from Ceca guidance.

The current Scroll I text is:

```text
THE FIRST BINDING

Steel could not wound it. Fire could not cleanse it.

What could not be destroyed was bound.

Three wards were raised beyond the shore,
and the heart of the rite remained at the center.

When the final mark was set, the red nights receded.
```

---

# 15. Scroll I progression

Windy progression deliberately distinguishes acquisition from reading.

## Acquisition / possession

After the cleared ward and Scroll I acquisition:

```text
Wisp ordinary-night global unlock
```

This means Wisps may later appear on the Main Island.

---

## Reading

When Scroll I is opened:

```text
scrollIRead = true
```

Reading Scroll I unlocks:

```text
Darkroot Isle
```

This ensures story progression happens only after the player actually reads the lore required to justify the next progression step.

---

# 16. Scroll UI and Ceca timing

When a Scroll is opened:

```text
activeOverlay = SCROLL
```

Ceca contextual UI is not allowed to render over the Scroll.

The Scroll read state may be marked immediately, but the Ceca reaction waits until:

```text
activeOverlay = NONE
```

after the Scroll is closed.

This gives the desired order:

```text
open Scroll
→ read lore
→ close Scroll
→ Ceca reacts
```

rather than placing Ceca dialogue on top of the lore UI.

---

# 17. Windy Ceca guidance

Windy-specific Ceca lines use the existing contextual guidance system.

They are one-time per save because `GuidanceSystem` tracks shown contextual steps.

The implementation intentionally does **not** tell the player that a Scroll/page exists on arrival.

## First Windy arrival

```text
So. Another shore.
The wind makes an empty place sound almost alive.
Almost.
```

Purpose:

- establishes atmosphere;
- gives no objective spoiler;
- Ceca does not reveal knowledge the player has no reason to know.

---

## Guardian encounter begins

```text
Well.
That got its attention.
Try not to die.
```

Purpose:

- reacts to an event the player just caused;
- slightly dry Ceca tone;
- no lore dump during combat.

---

## After Scroll I is read

```text
They did what they had to.
People become very brave when they are afraid enough.
```

Purpose:

- Ceca comments only after the player has actually read the historical text;
- introduces the first small trace of personal bitterness without revealing her role.

---

## Later Main Island night after Wisp global unlock

```text
The island is resisting us.
```

Purpose:

- acknowledges that the world has changed after the first ward-page is disturbed;
- does not explain the full truth.

---

# 18. Guidance architecture

Ceca-specific logic should not contaminate lower-level gameplay systems.

The preferred separation is:

```text
gameplay state / ordinary event
        ↓
GuidanceSystem / GameScreen
        ↓
GuideStep
        ↓
localized message
        ↓
GuidanceUi
```

The guidance system stores contextual completion state so messages do not repeat on every revisit.

Gameplay continues while Ceca messages are visible.

The player dismisses them manually.

---

# 19. Hazard system

Windy Plains contains hole / pit tiles.

The holes are implemented as **tile effects**, not static collision.

This distinction is intentional.

A hole must allow the player to physically enter the tile so the game can detect a fall.

The relevant Tiled layer is:

```text
Hazards
```

Hazard tiles use the custom property:

```text
hazard = true
```

`TileMap` exposes checks such as:

```java
isHazard(tileX, tileY)
isHazardAtWorld(worldX, worldY)
```

The property is read as nullable `Boolean` rather than primitive `boolean` because a tile may not define the property.

Safe pattern:

```java
Boolean hazard = cell.getTile()
    .getProperties()
    .get("hazard", Boolean.class);

return Boolean.TRUE.equals(hazard);
```

This produces:

```text
true property    → true
false property   → false
missing property → false
```

without an unboxing `NullPointerException`.

---

# 20. Player hazard behavior

After normal movement and Dash are processed, the world checks the player's final position.

Conceptual update order:

```text
normal movement
    ↓
Dash
    ↓
hazard check
    ↓
navigation update
```

If the player's center is on a hazard tile:

```text
pit damage
    ↓
if alive:
teleport to area checkpoint / dock

if dead:
normal death flow handles respawn
```

Pit damage is separate from ordinary enemy damage cooldown behavior so falling in a pit cannot be ignored merely because the player was recently hit by an enemy.

---

# 21. Dash and holes

Dash is intentionally able to cross holes.

The hazard check occurs after the dash finishes.

Therefore:

```text
start on safe ground
→ Dash crosses hazard tiles
→ finish on safe ground
→ no fall
```

but:

```text
start on safe ground
→ Dash ends on hazard
→ fall
```

The hazard itself must therefore **not** be placed on the normal collision layer.

This creates useful Windy gameplay:

```text
small / medium gap
→ Swift Step can cross

large gap
→ cannot simply Dash across
```

The ability is therefore not only a movement speed feature but also a traversal reward tied directly to Windy level design.

---

# 22. Hazard restrictions for building and farming

Hazard tiles are invalid targets for normal tile actions.

The player cannot:

- till a hole;
- plant Wheat on a hole;
- plant a Sapling on a hole;
- place buildable objects on a hole.

Target validation rejects the tile before the normal action-specific logic proceeds.

This keeps all placement systems consistent with the map hazard data.

---

# 23. Hazard restrictions for natural resources

Procedural natural-resource spawning treats hazard tiles as invalid.

Therefore trees and rocks cannot spawn on pits.

This is combined with the existing `NoNaturalSpawn` rectangle system.

The map can therefore protect space through two independent mechanisms:

```text
hazard tile
→ inherently invalid natural-spawn position

NoNaturalSpawn rectangle
→ designer-reserved region
```

---

# 24. Ground enemy navigation around holes

Ground enemies treat hazards as non-walkable navigation cells.

Examples:

```text
Shade
Wraith
```

The `DistanceField` excludes hazard cells from valid ground navigation.

This prevents ground enemies from walking directly across pits.

The important distinction remains:

```text
player collision:
hole is enterable

ground AI navigation:
hole is non-walkable
```

These are separate gameplay rules.

---

# 25. Wisp and hazards

Wisp uses:

```text
MovementType.FLYING
```

Flying enemies are allowed to move over pits.

Therefore:

```text
Shade  → must navigate around pit
Wraith → must navigate around pit
Wisp   → may fly over pit
```

The same distinction is also respected during enemy spawning.

Ground enemies cannot spawn on hazard tiles.

Flying enemies do not need the same restriction when the terrain below them is a hole.

---

# 26. Guardian spawning and hazards

Guardian enemies use region-based spawning.

Ground guardian enemies must still obey hazard restrictions.

Therefore a guardian Shade cannot be created inside a hole merely because the hole lies within `guardian_arena_region`.

Wisp may occupy airspace above hazard terrain because its movement type is flying.

This allows encounter regions to contain environmental hazards without requiring custom spawn coordinates for every enemy.

---

# 27. Save/load expectations

Permanent Windy progression belongs in persistent progression/save state.

Examples include:

```text
Windy ward cleared
Dash / Swift Step unlocked
Wisp global-night unlocked
Scroll I read
Darkroot Isle unlocked
```

Area runtime data and chest contents are also restored through the normal save/load architecture.

Temporary presentation or current-attempt data should not become permanent progression.

Examples:

```text
active guardian attempt
reward card currently requested
temporary guardian darkness
currently visible Ceca panel
```

An ACTIVE guardian encounter is not treated as permanently completed.

After load / travel / death, the game uses the permanent ward state to determine whether the Windy ward is actually finished.

---

# 28. Important reusable pattern established by Windy

Windy Plains establishes the pattern later islands should reuse:

```text
Tiled guardian arena
        ↓
guardian chest
        ↓
sealed interaction
        ↓
GuardianEncounter
        ↓
area-specific enemy composition
        ↓
permanent concrete ward flag
        ↓
mechanical reward
        ↓
Scroll acquisition progression
        ↓
Scroll read progression
        ↓
next island
```

Darkroot Isle should therefore reuse this architecture rather than duplicate a completely separate special-case implementation.

---

# 29. Main classes involved

The Windy feature touches or relies on the following project areas.

## World / area

```text
World
AreaID
AreaRuntime
AreaManager
TileMap
```

## Encounter

```text
GuardianEncounter
GuardianEncounterState
```

## Progression

```text
ProgressionState
```

## Enemy / navigation

```text
EnemySpawnSystem
EnemyType
Shade
Wisp
DistanceField
MovementType
NavigationMode
```

## Objects

```text
Chest
ChestKind
ChestVisualState
DestructibleObjectSystem
```

## Player

```text
Player
PlayerInput
Config
```

## UI

```text
GameScreen
RewardCardUi
RewardType
GuidanceUi
GuidanceSystem
GuideStep
```

## Rendering

```text
WorldRenderer
EnemyRenderer
```

---

# 30. Final Windy Plains behavior checklist

The completed Windy implementation should satisfy all of the following.

### Travel and map

- Windy can be reached through the world-map travel flow.
- Player arrives at `dock_arrival`.
- The Windy runtime persists across revisits.

### Ordinary enemies

- Windy ordinary nights include Wisp locally.
- Main Island does not gain ordinary Wisp spawning until the relevant Windy progression occurs.

### Guardian

- Guardian chest begins SEALED.
- Interacting with it starts the guardian encounter.
- Guardian wave uses 10 Shade + 10 Wisp.
- Ordinary enemies/projectiles are cleared on activation.
- Ordinary spawning is suspended.
- Guardian darkness is active.
- Day/night progression is not mutated merely to produce darkness.
- Death during ACTIVE resets the attempt.
- Travel during ACTIVE resets the attempt.
- Re-entering allows another attempt.
- Clearing the guardian is permanent.

### Reward

- Clearing the guardian unlocks Dash / Swift Step.
- Swift Step reward card appears.
- Reward presentation does not pause gameplay.
- Dash remains unlocked after save/load.

### Chest and Scroll

- Cleared guardian chest becomes accessible.
- Scroll I is stored in the guardian chest.
- Scroll I is a physical inventory item.
- Scroll I does not despawn on the ground.
- Scroll I can be read repeatedly.
- Reading opens the dedicated Scroll UI.

### Story progression

- Scroll I acquisition enables Wisp global-night progression.
- Scroll I read unlocks Darkroot Isle.
- Ceca reacts after the Scroll overlay is closed.

### Hazards

- Hole tiles use `hazard=true`.
- Walking into a pit damages the player.
- Surviving returns the player to the checkpoint / dock.
- Lethal pit damage uses the normal death flow.
- Natural resources do not spawn on pits.
- Building / planting / tilling are invalid on pits.
- Shade/Wraith do not navigate through pits.
- Ground enemies do not spawn on pits.
- Wisp can fly across pits.
- Dash can cross a pit when it ends on safe ground.
- Dash ending on a hazard causes a fall.

---

# 31. Defense notes

## Why is a hole not collision?

Because collision prevents entering the tile.

The hole's gameplay effect requires the player to enter the tile first, so it is modeled as a hazard / tile effect instead.

---

## Why can Dash cross a hole?

The game checks the final player position after Dash.

Hazard tiles crossed during the Dash are not treated as walls.

If the final position is safe, the traversal succeeds.

---

## Why do ground enemies avoid holes if the player can enter them?

Player hazard behavior and AI navigation are different systems.

The player may enter a hazard and suffer its effect.

Ground AI treats the same tile as invalid navigation terrain.

---

## Why are `GuardianEncounterState` and `windyWardCleared` separate?

`GuardianEncounterState` describes the current runtime attempt.

`windyWardCleared` is permanent progression.

A failed ACTIVE encounter must reset, while a truly CLEARED ward must remain completed forever.

---

## Why is Swift Step reward UI separate from progression?

The persistent truth is:

```text
dashUnlocked
```

The reward card is only presentation.

Gameplay progression must not depend on whether a temporary UI animation was shown successfully.

---

## Why are Tiled regions used?

Map-specific spatial information belongs in map data.

Java should know the gameplay meaning of:

```text
guardian_arena_region
NoNaturalSpawn
scroll_chest_spawn
hazard
```

but it should not need hardcoded Windy coordinates.

That keeps the system reusable for Darkroot Isle and Veilscar Passage.
