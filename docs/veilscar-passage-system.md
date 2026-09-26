# Veilscar Passage – Final Outer Island, Guardian, Bloodthirst and Scroll III

## Overview

`Veilscar Passage` is the third and final outer island in Ashveil progression.

It is the most hostile outer area and the final preparation before the player returns to the Main Island for the Ashen Rite. Veilscar reuses the area, guardian, progression, save/load and contextual-guidance architecture established by Windy Plains and Darkroot Isle instead of introducing a separate one-off system.

The area completes:

- the full ordinary-night enemy roster;
- the largest outer-island guardian encounter;
- the Bloodthirst sword transformation;
- Scroll III — The Broken Oath;
- the final outer-island Ceca guidance;
- the transition from island progression toward the Main Island finale.

---

# 1. Role in progression

```text
Darkroot Isle
    ↓
Scroll II read
    ↓
Veilscar Passage unlocked
    ↓
Veilscar guardian
    ↓
Bloodthirst awakened
    ↓
Scroll III obtained
    ↓
Scroll III read
    ↓
Return toward the Main Island / central ritual area
```

Veilscar does not unlock another normal island. It completes the three-scroll outer-island progression and prepares the player for the final Ashen Rite sequence.

---

# 2. Area runtime and revisiting

Veilscar uses the same `AreaRuntime` architecture as the other areas.

Each initialized area owns its own:

- `TileMap`;
- `CollisionSystem`;
- `DestructibleObjectSystem`;
- `WorldItemSystem`;
- `ProjectileSystem`;
- enemy list;
- `EnemySpawnSystem`;
- navigation state;
- farming state.

When the player leaves Veilscar and later returns, the existing runtime is reused.

---

# 3. Environment and natural resources

Veilscar is intentionally harsher and less fertile than the previous areas.

Its natural procedural resource set is:

```text
ROCK
```

Natural trees do not spawn on Veilscar.

The area can use a restrained crimson renderer-only atmosphere, but the global `DayNightCycle` is not replaced or frozen.

---

# 4. Ordinary enemy roster

Veilscar uses the full current enemy roster locally:

```text
Shade
Wisp
Wraith
```

This local roster does not depend on the player's global ordinary-night unlock state.

---

# 5. Guardian encounter

Veilscar uses the same generic guardian system as the previous islands.

The guardian chest begins sealed. Interacting with it before the ward has been cleared starts the encounter:

```text
sealed guardian chest
    ↓
start guardian encounter
    ↓
clear ordinary enemies/projectiles
    ↓
stop ordinary spawning
    ↓
spawn Veilscar guardian composition
```

The final intended Veilscar guardian composition is:

```text
20 Shade
18 Wisp
12 Wraith
```

Smaller values may be used temporarily during development testing, but they are not the intended final balance.

The encounter uses the Tiled region:

```text
guardian_arena_region
```

---

# 6. Guardian reset and completion

An active guardian attempt is temporary runtime state.

If the player leaves or dies during the active attempt:

```text
ACTIVE
    ↓
reset
    ↓
retry later
```

The permanent Veilscar ward flag changes only after the encounter is actually cleared.

Once cleared:

```text
veilscarWardCleared = true
```

and remains cleared across travel, revisits and save/load.

---

# 7. Bloodthirst reward

Clearing the Veilscar guardian requests:

```text
RewardType.BLOODTHIRST
```

and upgrades the player's weapon to:

```text
ItemType.BLOODTHIRST_SWORD
```

Bloodthirst is a transformation of the Stone Sword into a cursed blood-bound weapon.

Final gameplay identity:

```text
Entity damage: 9
Durability: none
```

The lack of durability is intentional. The awakened blade sustains itself through blood and therefore no longer dulls or breaks.

The reward card is presentation only; the gameplay reward is applied first.

---

# 8. Reward card

The Bloodthirst card uses:

```text
WEAPON AWAKENED
Bloodthirst
```

The localized description is rendered as Scene2D text over the lower empty portion of the card instead of being baked permanently into the PNG.

English description:

```text
The blade feeds on blood.
Its edge will never dull.

Deals 9 damage to enemies.
No durability.
```

---

# 9. Scroll III

The Veilscar guardian chest contains:

```text
Scroll III — The Broken Oath
```

Scroll III is a physical inventory item and can be picked up, dropped and read through the normal Scroll UI.

Current English text:

```text
THE BROKEN OATH

The Rite was never lost.

We divided it ourselves.

What was done once must never be done lightly again.

So its pages were carried apart, beyond the sea.

We told ourselves it was the kinder choice.

Some promises are easier to break when the one you made them to cannot follow.
```

Unlike Scroll I and Scroll II, Scroll III does not unlock another outer island or another global enemy type.

---

# 10. Scroll III read state

Reading Scroll III marks:

```text
scrollIIIRead = true
```

The intended UI order is:

```text
open Scroll III
    ↓
read lore
    ↓
close Scroll UI
    ↓
Ceca reacts
```

Ceca should not appear over the Scroll UI.

---

# 11. Veilscar Ceca guidance

Veilscar adds two contextual guidance moments.

## First arrival

Localization key:

```text
guidance.veilscarArrival
```

Ceca is more focused and slightly impatient, but still not openly hostile.

## After Scroll III

Localization key:

```text
guidance.veilscarScrollIIIRead
```

Canonical reaction:

```text
Convenient, isn't it?
To call something mercy once you're no longer the one paying for it.

Keep the page.
We're close now.
```

This is the first strong suspicion moment without explicitly revealing that Ceca is the original Anchor.

---

# 12. Guidance persistence

Veilscar contextual guidance uses the existing `GuidanceSystem` and its shown-contextual-step persistence.

This prevents:

```text
VEILSCAR_ARRIVAL
VEILSCAR_SCROLL_III_READ
```

from repeating after acknowledgement.

---

# 13. Save/load expectations

The following Veilscar state must persist:

```text
Veilscar ward cleared
Bloodthirst sword in inventory
Scroll III inventory state
Scroll III read state
Veilscar area runtime state
shown Veilscar contextual guidance
```

Temporary presentation/current-attempt state should not be treated as permanent progression:

```text
active guardian attempt
visible reward card
reward request
temporary atmosphere presentation
```

After loading a completed Veilscar state:

- the ward remains cleared;
- the guardian does not restart;
- Bloodthirst remains Bloodthirst;
- Scroll III read state remains true if already read;
- one-time Ceca messages do not repeat.

---

# 14. Important design separation

```text
Area identity
    !=
global enemy unlock state

Guardian attempt
    !=
permanent ward completion

Reward card
    !=
gameplay reward state

Scroll possession
    !=
Scroll read state

Veilscar atmosphere
    !=
global DayNightCycle
```

---

# 15. Main classes involved

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
GuardianEncounterDefinition
GuardianEncounterState
```

## Progression

```text
ProgressionState
```

## Enemy

```text
EnemySpawnSystem
EnemyType
Shade
Wisp
Wraith
```

## Items / combat

```text
ItemType
CombatSystem
Inventory
```

## UI

```text
RewardCardUi
RewardType
ScrollUi
GuidanceUi
GuidanceSystem
GuideStep
```

## Persistence

```text
SaveMapper
SaveData
Progression save data
Area runtime save data
Guidance save data
```

---

# 16. Final behavior checklist

## Travel and area

- Veilscar is unlocked after Scroll II is read.
- Player can travel to Veilscar through the world map.
- Player arrives at the Veilscar dock.
- The area runtime is revisitable.

## Environment

- Natural trees do not spawn.
- Natural rocks may spawn.
- Veilscar atmosphere does not replace global day/night state.

## Ordinary enemies

- Shade is available.
- Wisp is available.
- Wraith is available.

## Guardian

- Guardian chest starts sealed.
- Interacting with it starts the encounter.
- Ordinary enemies/projectiles are cleared.
- Ordinary spawning is suspended during the guardian attempt.
- The final intended encounter uses 20 Shade, 18 Wisp and 12 Wraith.
- Death or travel resets an unfinished attempt.
- Clearing the ward is permanent.

## Reward

- Guardian clear awakens Bloodthirst.
- Bloodthirst deals 9 entity damage.
- Bloodthirst does not use durability.
- Bloodthirst reward card is shown.
- Reward description is localized.

## Scroll III

- Guardian chest contains Scroll III.
- Scroll III is a physical inventory item.
- Scroll III can be dropped and read.
- Reading marks `scrollIIIRead`.
- Reading does not unlock another enemy type.
- Ceca reacts only after the Scroll UI is closed.

## Persistence

- Ward clear survives save/load.
- Bloodthirst survives save/load.
- Scroll III read state survives save/load.
- One-time Veilscar guidance does not repeat.

---

# 17. Defense notes

## Why does Veilscar have all enemy types even if global progression is different?

Because area-specific availability and global ordinary-night progression are separate systems. Veilscar is intentionally the final outer island and defines its own local roster as Shade + Wisp + Wraith.

## Why is Bloodthirst a separate item type?

Because the transformation changes gameplay identity and persistence identity. A dedicated item type makes the awakened weapon explicit in inventory, combat, save/load and UI.

## Why does Bloodthirst have no durability?

It is a progression reward rather than a normal crafted tool. Its lore and mechanics are aligned: the blood-bound blade sustains itself and therefore does not dull.

## Why does Scroll III not unlock another island?

Because Veilscar is the final outer island. Scroll III completes the distributed Rite-page progression and redirects the player toward the Main Island finale.

## Why is Veilscar atmosphere renderer-only?

Because visual tone and world time are different responsibilities. Changing the actual `DayNightCycle` only to force a visual mood would couple presentation to gameplay state and make travel, ordinary nights and save/load harder to reason about.
