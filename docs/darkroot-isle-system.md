# Darkroot Isle – Progression, Guardian Encounter, Blackthorn Craft and Hollowcap

## Overview

`Darkroot Isle` is the second outer island in Ashveil progression.

It builds on the reusable outer-island architecture established by Windy Plains and adds several new gameplay elements:

- `Wraith` as the island's new enemy type;
- a mixed guardian encounter using Shade, Wisp and Wraith;
- permanent Darkroot ward progression;
- the `BLACKTHORN` crafting category;
- `Thorn Fence`;
- `Briar Snare`;
- Scroll II progression;
- global Wraith night-spawn progression;
- Veilscar Passage unlock;
- `Hollowcap` as a Darkroot-specific harvestable resource;
- Tiled-defined resource zones;
- reusable region-based natural resource spawning;
- Darkroot-specific Ceca guidance;
- save/load persistence for the completed island state and spawned resources.

The main design goal is to reuse the systems already established by Windy Plains instead of implementing Darkroot as a separate scripted level.

---

# 1. Darkroot Isle role in progression

The intended progression is:

```text
Windy Plains
    ↓
Scroll I read
    ↓
Darkroot Isle unlocked
    ↓
Darkroot guardian encounter
    ↓
Blackthorn Craft unlocked
    ↓
Scroll II obtained
    ↓
Wraith becomes globally available at night
    ↓
Scroll II read
    ↓
Veilscar Passage unlocked
```

Several progression conditions remain deliberately separate.

### Guardian cleared

Unlocks:

```text
Darkroot ward cleared
BLACKTHORN crafting category
guardian chest becomes accessible
```

### Scroll II acquired / removed from the guardian chest

Unlocks:

```text
Wraith ordinary-night global progression
```

### Scroll II read

Unlocks:

```text
Veilscar Passage
Ceca Scroll II reaction
```

This keeps mechanical reward progression, enemy-world progression and story progression as separate state changes.

---

# 2. Reused area runtime architecture

Darkroot Isle uses the same `AreaRuntime` architecture as the other areas.

The world stores initialized runtimes by:

```java
Map<AreaID, AreaRuntime> areaRuntimes
```

The active area is referenced through:

```java
currentAreaRuntime
```

Darkroot therefore owns its own runtime state such as:

- `TileMap`;
- `CollisionSystem`;
- `DestructibleObjectSystem`;
- `WorldItemSystem`;
- `ProjectileSystem`;
- enemy list;
- `EnemySpawnSystem`;
- navigation / `DistanceField`;
- farming state.

Leaving Darkroot does not recreate or discard its runtime state.

Returning during the same session reuses the existing runtime.

The same area data is also included in the normal save/load flow.

---

# 3. Darkroot enemy role

Darkroot Isle introduces:

```text
Wraith
```

as the new local enemy type.

The island therefore expands the enemy set from:

```text
Shade
Wisp
```

to:

```text
Shade
Wisp
Wraith
```

Wraith is a ground ranged enemy.

It uses:

```text
MovementType.GROUND
NavigationMode.NORMAL
DistanceField
ProjectileSystem
```

Its AI uses three main states:

```text
APPROACH
ATTACK
RETREAT
```

Darkroot is the first area where the player is expected to deal with melee pressure, flying dash pressure and ranged pressure together.

---

# 4. Darkroot ordinary night progression

Before Scroll II progression changes the global enemy pool:

```text
Main Island:
Shade + previously unlocked global enemies

Darkroot Isle:
Shade + Wisp + Wraith
```

After the relevant Darkroot Scroll progression unlocks Wraith globally:

```text
Main Island and later areas:
Wraith may join ordinary-night spawning
```

The important distinction is:

```text
Darkroot local availability
        ≠
global Wraith unlock
```

The player may encounter Wraith on Darkroot before Wraith is allowed to appear globally.

---

# 5. Darkroot guardian encounter

Darkroot reuses the generic guardian encounter architecture.

The guardian arena is defined in Tiled using:

```text
guardian_arena_region
```

The guardian chest is represented by:

```text
ChestKind.GUARDIAN
```

The runtime encounter still uses the same broad states:

```text
INACTIVE
ACTIVE
CLEARED
```

The final Darkroot guardian composition is:

```text
15 Shade
15 Wisp
8 Wraith
```

This makes the second guardian encounter more demanding than Windy because all three current enemy roles participate at once.

---

# 6. Permanent Darkroot ward state

The guardian encounter runtime state and the permanent progression state remain separate.

Darkroot uses a concrete persistent flag:

```text
darkrootWardCleared
```

Conceptually:

```text
guardian attempt
INACTIVE → ACTIVE → CLEARED
```

is temporary runtime encounter state.

But:

```text
darkrootWardCleared = true
```

is permanent progression.

Once the player genuinely clears the Darkroot guardian, the ward remains cleared across:

- travel;
- revisits;
- save/load.

A failed or abandoned ACTIVE attempt still resets normally.

---

# 7. Darkroot guardian reward

Clearing the Darkroot guardian does not give another generic movement upgrade.

Instead, it unlocks:

```text
CraftingCategory.BLACKTHORN
```

Blackthorn Craft is intentionally a compact defensive branch.

It contains only:

```text
Briar Snare
Thorn Fence
```

This keeps the reward meaningful without creating a large new crafting subsystem.

The existing crafting category progression architecture is reused instead of adding a separate Darkroot-only crafting manager.

---

# 8. Thorn Fence

`Thorn Fence` is a stronger defensive fence variant.

It is represented by:

```text
ItemType.THORN_FENCE
DestructibleObjectType.THORN_FENCE
```

It behaves like a normal fence for:

- placement;
- physical blocking;
- enemy navigation;
- destruction;
- save/load;
- world rendering.

Its additional combat behavior is retaliation damage when an enemy attacks it.

Shade AI can therefore interact with it through the existing fence-destruction logic without requiring a separate AI system.

The fence is rendered through the shared object-texture rendering path.

---

# 9. Briar Snare

`Briar Snare` is a placeable single-use ground trap.

It is represented by:

```text
ItemType.BRIAR_SNARE
DestructibleObjectType.BRIAR_SNARE
BriarSnare
```

Its final gameplay behavior is:

```text
GROUND enemy enters trap
        ↓
enemy takes damage
        ↓
enemy is briefly rooted
        ↓
trap is consumed / destroyed
```

Current design rules:

- single-use;
- deals `1` damage;
- applies a short root;
- does not block movement;
- does not block navigation;
- does not drop itself after triggering;
- does not drop itself when manually destroyed.

The trap checks the generic enemy movement type:

```text
MovementType.GROUND
```

rather than checking for concrete classes such as Shade or Wraith.

This means the behavior automatically applies to future ground enemies while flying enemies such as Wisp ignore it.

---

# 10. Root behavior

Rooting is stored as generic enemy state.

`Enemy` keeps a root timer rather than knowing about `BriarSnare`.

Conceptually:

```text
BriarSnare
    ↓
Enemy.applyRoot(duration)
    ↓
Enemy movement methods temporarily refuse movement
```

This preserves the separation:

```text
trap decides to apply a root
enemy only knows that it is rooted
```

The base enemy class therefore does not depend on a concrete trap type.

---

# 11. Scroll II

Darkroot's guardian chest contains:

```text
Scroll II — The Long Vigil
```

Scroll II is a physical inventory item.

It follows the same scroll rules as Scroll I:

- it can be picked up;
- it can be dropped;
- it can be read repeatedly;
- it does not despawn like ordinary temporary drops;
- reading uses the dedicated Scroll UI.

Darkroot progression deliberately distinguishes possession from reading.

---

# 12. Scroll II acquisition progression

After the Darkroot ward has been cleared and Scroll II is acquired:

```text
Wraith global-night progression is unlocked
```

Conceptually:

```text
darkrootWardCleared
+
player inventory contains Scroll II
+
Wraith global unlock not already active
        ↓
unlock Wraith globally
```

This mirrors the Windy architecture for Wisp progression.

The player does not need to read the Scroll merely to cause the world-level enemy progression.

---

# 13. Scroll II read progression

Reading Scroll II sets the corresponding read progression state.

After Scroll II has been read:

```text
Veilscar Passage
```

becomes available.

This keeps the next-area gate tied to the story action of actually reading the required lore.

The order is therefore:

```text
guardian cleared
    ↓
Scroll II acquired
    ↓
Wraith global unlock
    ↓
Scroll II read
    ↓
Veilscar Passage unlock
```

---

# 14. Darkroot Ceca guidance

Darkroot uses the existing contextual guidance architecture.

The important Scroll II reaction is:

```text
They wrote as if they endured it together.
They didn’t.
```

This line appears only after the player has read Scroll II.

It is intentionally more personal than the earlier Windy dialogue but still does not reveal Ceca's true role.

The guidance system keeps the line one-time per save through the normal shown-contextual-step persistence.

Darkroot guidance therefore remains separate from:

- enemy AI;
- guardian encounter classes;
- Scroll item implementation;
- rendering.

---

# 15. Hollowcap resource

`Hollowcap` is a Darkroot-specific harvestable biome resource.

It is not a third major Darkroot reward branch.

It is represented through the existing world-object architecture as:

```text
ItemType.HOLLOWCAP
DestructibleObjectType.HOLLOWCAP
Hollowcap extends DestructibleObject
```

Although Hollowcap is biologically a plant-like resource, it is intentionally not part of `FarmingSystem`.

The reason is that its current gameplay lifecycle is:

```text
spawn naturally in Darkroot
    ↓
player finds it
    ↓
player harvests it
    ↓
item drops
```

It does not currently use:

- tilled soil;
- planting;
- growth stages;
- growth timers;
- crop harvesting rules.

Using the existing destructible-resource lifecycle avoids creating a separate harvesting subsystem for one wild biome resource.

---

# 16. Hollowcap movement and navigation behavior

Hollowcap is a low-profile world resource.

It should not behave like a blocking tree or rock.

Therefore it does not block:

```text
player / enemy movement
navigation
```

Its gameplay footprint is:

```text
1 tile
```

This footprint is independent from its visual size.

---

# 17. Hollowcap rendering

Hollowcap uses a taller visual sprite while preserving a one-tile gameplay footprint.

Conceptually:

```text
gameplay footprint:
1 × 1 tile

visual render:
1 × 2 tiles
```

The lower tile remains the actual world-object position.

The upper visual portion is only rendering.

This means:

- collision stays predictable;
- spawn logic still treats Hollowcap as a one-tile object;
- another object may technically occupy the tile above;
- the sprite can look larger and more readable without changing gameplay geometry.

This follows the general Ashveil rule that visual sprite size and physical collision size do not need to be identical.

---

# 18. ResourceZones in Tiled

Darkroot uses a Tiled object layer:

```text
ResourceZones
```

Hollowcap spawn regions are rectangle objects named:

```text
hollowcap_zone
```

Multiple rectangles may use the same name.

Example:

```text
ResourceZones
├── hollowcap_zone
├── hollowcap_zone
└── hollowcap_zone
```

The Java code therefore does not require names such as:

```text
hollowcap_zone_1
hollowcap_zone_2
hollowcap_zone_3
```

The zones describe where the resource is allowed to appear.

They do not hardcode exact Hollowcap spawn coordinates.

---

# 19. Multiple Tiled rectangles

`TileMap` exposes a method that can return all rectangle objects with the same name.

Conceptually:

```java
getObjectRectangles(
    "ResourceZones",
    "hollowcap_zone"
)
```

returns:

```java
List<Rectangle>
```

This is different from the single-object lookup used for map objects that are expected to exist only once.

The multi-rectangle method is reusable for future resource-zone systems.

---

# 20. Region-based resource spawning

`DestructibleObjectSystem` contains a generic region-based spawn method:

```java
spawnObjectsInRegions(
    DestructibleObjectType type,
    List<Rectangle> regions,
    int amount,
    Player player
)
```

The method is intentionally not called:

```text
spawnHollowcaps(...)
```

because the spawning algorithm is not Hollowcap-specific.

Its job is:

```text
receive object type
    ↓
receive allowed regions
    ↓
choose a random region
    ↓
choose a random tile inside that region
    ↓
validate the candidate tile
    ↓
spawn the requested object type
```

Darkroot is simply the first feature using this reusable method.

---

# 21. Rectangle-to-tile conversion

Tiled rectangle coordinates are world-space coordinates.

Resource spawning needs tile coordinates.

For each rectangle, the code calculates:

```text
minTileX
maxTileX
minTileY
maxTileY
```

The minimum uses the first complete tile inside the region.

The maximum uses the final complete tile before the rectangle's outer edge.

This prevents an object from being placed on a tile that only partially overlaps the Tiled region.

Conceptually:

```text
Tiled rectangle in world coordinates
        ↓
convert to valid tile-coordinate range
        ↓
choose random tile
```

The selected tile is later converted back into world coordinates when the object is created.

---

# 22. Spawn-attempt limit

Region-based spawning uses a maximum number of attempts.

The purpose is to avoid an infinite loop when a region cannot provide enough valid tiles.

For example, a region may be mostly:

- blocked terrain;
- reserved tiles;
- already occupied object tiles;
- invalid natural-spawn positions.

Conceptually:

```text
spawned < requested amount
AND
attempts < maximum attempts
```

If the system cannot find enough valid positions, it stops safely instead of searching forever.

---

# 23. Reuse for future Main Island resource areas

The region-based spawning system is intentionally generic.

It can later support Main Island resource concentrations such as:

```text
forest_zone
    → TREE

rock_zone
    → ROCK
```

This allows the world to combine:

```text
ordinary random resource spawning
+
designer-controlled dense resource zones
```

For example, trees may still appear across the Main Island while `forest_zone` creates a visibly denser forest area.

The spatial layout remains defined in Tiled rather than through hardcoded Java coordinates.

---

# 24. Hollowcap harvesting and drop

Hollowcap uses the normal destructible-object cleanup and drop pipeline.

The object-drop mapping includes:

```text
HOLLOWCAP
    → ItemType.HOLLOWCAP
```

When the world object is destroyed:

```text
Hollowcap world object
    ↓
destroyed-object cleanup
    ↓
HOLLOWCAP item drop
    ↓
player may pick up the item
```

This allows the resource to reuse:

- hit handling;
- world item creation;
- pickup;
- inventory;
- save/load.

---

# 25. Hollowcap as a consumable

The collected Hollowcap item can be used from the hotbar.

Input:

```text
F
```

Hollowcap does not use tile targeting.

Its use flow is:

```text
HOLLOWCAP selected
    ↓
press F
    ↓
player heals
    ↓
one Hollowcap is removed from the selected slot
```

It behaves as a stronger healing resource than ordinary Bread.

If the player is already at full health, the item should not be consumed.

This keeps consumable behavior consistent with the existing Bread rule.

---

# 26. Item-use responsibility

Hollowcap use is handled through the existing item-use path in `World`.

This is preferable to placing healing logic inside:

- `Hollowcap` world object;
- `Inventory`;
- UI code.

The world object only represents the resource while it exists in the environment.

The inventory item is what is consumed.

The player performs the actual health change.

Conceptually:

```text
World
    ↓
selected ItemType
    ↓
Player.heal(...)
    ↓
Inventory removes one item
```

---

# 27. Save/load behavior

Hollowcap does not require a separate save-data type.

It is already represented by the normal destructible-object save structure.

The relevant persisted data includes:

```text
object type
x
y
current HP
```

During save, initialized area runtimes serialize their destructible objects.

During load, the object type is restored through the normal object factory path.

Because:

```text
DestructibleObjectType.HOLLOWCAP
```

is reconstructed as:

```text
new Hollowcap(...)
```

the correct subclass behavior is restored automatically.

---

# 28. Spawn persistence

Hollowcap generation is performed only during initial area content creation.

It is not regenerated every time the player travels back to Darkroot.

Therefore:

```text
first Darkroot initialization
    ↓
spawn Hollowcaps in resource zones
```

but later:

```text
leave Darkroot
    ↓
return to Darkroot
    ↓
reuse existing state
```

After save/load, the remaining Hollowcap objects are restored from saved area data instead of rerolling all resource positions.

This means harvested Hollowcaps stay harvested after saving and loading.

---

# 29. Darkroot save/load expectations

Permanent Darkroot progression includes:

```text
darkroot ward cleared
BLACKTHORN category unlocked
Wraith global-night unlocked
Scroll II read
Veilscar Passage unlocked
```

Area runtime persistence also covers:

```text
remaining Hollowcap objects
remaining destructible objects
ground item drops
chest state / contents
enemy state
other normal runtime state
```

Temporary encounter presentation is not treated as permanent completion.

For example:

```text
currently ACTIVE guardian attempt
temporary darkness
temporary UI request
```

must not replace the permanent ward-clear state.

---

# 30. Important reusable patterns established by Darkroot

Darkroot extends the reusable outer-island structure with two important patterns.

## Progression reward pattern

```text
guardian clear
    ↓
concrete permanent ward flag
    ↓
area-specific mechanical reward
    ↓
Scroll acquisition progression
    ↓
Scroll read progression
    ↓
next island unlock
```

## Resource-zone pattern

```text
Tiled ResourceZones
    ↓
multiple same-name rectangles
    ↓
generic region-based spawn method
    ↓
tile validation
    ↓
persistent world resource
```

The first pattern continues the Windy architecture.

The second can be reused for future forests, rock concentrations and other biome-specific resources.

---

# 31. Main classes involved

## World / area

```text
World
AreaID
AreaRuntime
AreaManager
TileMap
```

## Guardian / progression

```text
GuardianEncounter
GuardianEncounterState
ProgressionState
```

## Enemy

```text
Enemy
EnemyType
Shade
Wisp
Wraith
EnemySpawnSystem
DistanceField
ProjectileSystem
MovementType
NavigationMode
```

## Objects

```text
DestructibleObject
DestructibleObjectType
DestructibleObjectSystem
Chest
ChestKind
ThornFence behavior
BriarSnare
Hollowcap
```

## Items / crafting

```text
ItemType
CraftingCategory
CraftingManager
Inventory
```

## UI / story

```text
GameScreen
GuidanceSystem
GuideStep
GuidanceUi
Scroll UI
```

## Rendering

```text
WorldRenderer
EnemyRenderer
```

## Save/load

```text
SaveMapper
AreaSaveData
DestructibleObjectSaveData
ProgressionSaveData
```

---

# 32. Final Darkroot Isle behavior checklist

### Area progression

- Darkroot becomes available after Scroll I is read.
- Darkroot uses its own persistent `AreaRuntime`.
- Darkroot state survives revisits.
- Darkroot state survives save/load.

### Enemies

- Wraith is locally available on Darkroot.
- Darkroot ordinary combat can use Shade, Wisp and Wraith.
- Wraith is not globally unlocked merely by first entering Darkroot.

### Guardian

- Guardian chest starts sealed while the ward is uncleared.
- Interacting with the guardian chest starts the encounter.
- Guardian encounter uses Shade, Wisp and Wraith.
- Failed / abandoned ACTIVE attempts reset.
- Clearing the guardian permanently clears the Darkroot ward.

### Reward

- Clearing the guardian unlocks `BLACKTHORN`.
- Blackthorn Craft contains Briar Snare and Thorn Fence.
- The category remains unlocked after save/load.

### Thorn Fence

- Thorn Fence is placeable.
- Thorn Fence blocks ground movement like a normal fence.
- Shade can attack it when required by navigation.
- Thorn Fence retaliates against attackers.
- It renders through the object texture system.

### Briar Snare

- Briar Snare is placeable.
- It does not block movement or navigation.
- Ground enemies can trigger it.
- Wisp does not trigger it because Wisp is flying.
- Triggering applies damage and root.
- The trap is single-use.
- It does not drop itself after destruction.

### Scroll II

- Scroll II exists as a physical inventory item.
- Acquiring it after the cleared ward unlocks Wraith globally.
- Reading it unlocks Veilscar Passage.
- Reading it triggers the Darkroot Ceca reaction.

### Hollowcap

- Hollowcap spawns only inside Darkroot `hollowcap_zone` regions.
- Multiple `hollowcap_zone` rectangles are supported.
- Hollowcap uses a one-tile gameplay footprint.
- Hollowcap may render visually as one tile wide and two tiles high.
- Hollowcap does not block movement.
- Harvesting it creates a Hollowcap item drop.
- The item can be picked up.
- Pressing `F` consumes one Hollowcap and heals the player.
- Full-health use does not waste the item.
- Harvested Hollowcaps do not regenerate on ordinary area revisit.
- Hollowcap state survives save/load.

---

# 33. Defense notes

## Why is Hollowcap not in FarmingSystem?

Because its current lifecycle is a wild biome resource, not a planted crop.

`FarmingSystem` is responsible for:

```text
tilling
planting
growth stages
growth timers
crop harvesting
```

Hollowcap currently needs:

```text
natural spawn
harvest
drop
inventory use
```

The existing destructible-resource lifecycle already provides those responsibilities.

---

## Why can Hollowcap render as 1×2 while occupying only one tile?

Rendering geometry and gameplay geometry are separate concerns.

The resource only owns its lower tile for:

- spawning;
- collision;
- persistence;
- object position.

The taller sprite is only a visual representation.

This gives better readability without changing gameplay footprint.

---

## Why use multiple Tiled rectangles with the same name?

The exact number of resource regions is a map-design concern.

Java only needs to know:

```text
all rectangles named hollowcap_zone
```

The level designer may add or remove zones without creating new Java identifiers such as:

```text
hollowcap_zone_1
hollowcap_zone_2
hollowcap_zone_3
```

---

## Why is `spawnObjectsInRegions(...)` generic?

The algorithm does not depend on mushrooms.

It only needs:

```text
object type
allowed regions
amount
spawn validation
```

Therefore the same method can later spawn:

```text
TREE in forest_zone
ROCK in rock_zone
```

without duplicating spawn logic.

---

## Why use an attempt limit for region spawning?

A requested region may not contain enough valid free tiles.

Without an attempt limit, random search could continue forever.

The limit guarantees that spawning fails safely if the map data does not provide enough usable space.

---

## Why is Briar Snare based on `MovementType.GROUND`?

The gameplay rule is that the trap lies on the ground.

The trap should therefore react to a movement category, not to a hardcoded list of enemy classes.

This makes the behavior automatically compatible with future ground enemies.

---

## Why does Enemy store only root state?

`Enemy` should not know what caused the root.

The source may currently be Briar Snare, but future systems could apply the same status effect.

This preserves a cleaner dependency direction:

```text
gameplay source
    → applies root

Enemy
    → stores and obeys root state
```

---

## Why are Scroll II acquisition and reading separate?

They represent different progression events.

Acquiring the Scroll changes the world's enemy progression:

```text
Wraith global unlock
```

Reading the Scroll advances the story:

```text
Veilscar Passage unlock
```

Keeping them separate prevents the game from treating possession and reading as the same action.

---

## Why is the ward flag separate from the guardian runtime state?

The guardian runtime represents the current attempt.

The ward flag represents permanent completed progression.

An ACTIVE attempt may reset after death or travel.

A cleared ward must remain cleared permanently.

---

# 34. Future extensions

The current implementation leaves several clean extension points without requiring immediate changes.

Possible later work includes:

- dedicated inventory icon for Hollowcap;
- final inventory and shop presentation;
- stronger healing recipe or consumable based on Hollowcap;
- Main Island `forest_zone`;
- Main Island `rock_zone`;
- additional biome-specific resource zones;
- final art and font polish;
- Veilscar Passage progression using the same guardian / Scroll architecture.

These are presentation or content extensions around the already established gameplay systems.
