# Health, Death and Gold System

## Purpose

This document describes the player/enemy health lifecycle, death and respawn flow, broken hearts, Gold rewards, and the Heart Repair consumable.

The combat targeting and damage-selection flow remains documented separately in `hittable-and-combatsystem.md`.

---

## Player health

The player uses integer HP internally.

Current configuration:

- maximum normal HP: `10`
- visible heart slots: `5`
- HP represented by one full heart: `2`
- maximum broken hearts: `3`
- minimum effective maximum HP after repeated deaths: `4`

The HUD therefore supports:

- full heart = 2 HP
- half heart = 1 HP
- empty healthy heart = 0 HP in that slot
- broken heart = unavailable maximum-health slot

The model keeps health logic independent from the temporary ShapeRenderer representation.

---

## Broken hearts

`Player` stores the number of broken hearts.

On death:

1. one broken heart is added, up to `Config.MAX_BROKEN_HEARTS`;
2. the player's effective `maxHp` is recalculated;
3. the player respawns with health restored to the new effective maximum.

Current relationship:

```text
0 broken hearts -> 10 max HP
1 broken heart  ->  8 max HP
2 broken hearts ->  6 max HP
3 broken hearts ->  4 max HP
```

Broken hearts are persistent gameplay state and are not ordinary damage.

---

## Player damage cooldown

`Player.takeDamage(...)` owns the player invulnerability/damage cooldown rule.

Enemy attack code may attempt to deal damage while contact continues, but `Player` decides whether that damage is currently accepted.

This prevents enemy classes from duplicating cooldown logic.

After respawn, the player receives a short damage cooldown so that the player cannot immediately take another hit at the checkpoint.

---

## Enemy data and lifecycle

Enemy-specific classes are grouped under:

```text
com.ashveil.entities.enemies
```

The shared enemy model consists of:

- `Enemy`
- `EnemyType`
- `EnemyState`
- concrete enemy classes such as `Shade`

`EnemyType` stores shared data such as HP, speed, attack damage, and movement type.

Behavior remains in the concrete enemy class.

### Enemy states

Current states:

```text
ALIVE
DYING
```

There is no separate `DEAD` state because an enemy is removed from the world after the DYING phase finishes.

Lifecycle:

```text
ALIVE
  |
HP reaches 0
  v
DYING
  |
dying timer expires
  v
removed from World
```

While `DYING`, an enemy:

- does not move;
- does not attack;
- cannot receive additional hits;
- does not show its normal HP bar.

`Enemy.update(float delta)` controls the common lifecycle, while concrete enemies implement only their alive behavior.

---

## Hittable validity

`Hittable` exposes whether a target can currently receive a hit.

For enemies, this is true only while the enemy is `ALIVE`.

This prevents the combat system from repeatedly hitting an enemy during its death phase and keeps the death/reward flow single-use.

---

## Shade attack ownership

Shade-specific contact attack behavior belongs to `Shade`, not to `World`.

The flow is:

```text
Shade.updateAlive(...)
    -> move toward target
    -> check overlap with Player
    -> attempt attack
```

The player damage cooldown still decides whether a contact attack actually reduces HP.

`World` therefore coordinates objects but does not contain Shade-specific combat behavior.

---

## Death and respawn transition

Player death does not replace the current game screen.

Instead, `GameScreen` owns a death transition:

```text
normal gameplay
    -> FADING_OUT
    -> full black
    -> World respawns Player
    -> camera snaps to checkpoint
    -> FADING_IN
    -> normal gameplay
```

The world is not updated while the death transition is active.

This freezes:

- player movement;
- enemy movement and attacks;
- gameplay timers driven by `World.update(...)`.

Rendering/fade logic stays in `GameScreen`; gameplay state stays in `World` and `Player`.

---

## Checkpoints

`World` owns the current respawn checkpoint coordinates.

Current main-island behavior uses the original player spawn as the initial checkpoint.

Future island/area travel can update the checkpoint through the same World-level mechanism.

The respawn flow does not need to know which island-specific system selected the checkpoint.

---

## Gold rewards

Gold is a separate currency after pickup.

The player owns a `Wallet`, which stores Gold as an integer balance.

Gold does not occupy an inventory slot after collection.

### Enemy Gold drop

When a genuinely defeated enemy finishes its death lifecycle:

1. the Gold drop amount is rolled;
2. the result is `0`, `1`, or `2`;
3. if the amount is greater than zero, a Gold `WorldItem` is created at the enemy position;
4. the enemy is then removed.

Current drop probabilities are configuration values, so balancing does not require changing the reward flow.

Gold is awarded only through a real enemy kill. Future fleeing/despawn behavior must not create kill rewards.

---

## Gold as a WorldItem

While Gold lies on the ground, it uses the existing `WorldItem` system.

This reuses:

- world position;
- ground-item rendering;
- pickup range;
- ground-item lifecycle.

The important distinction happens on pickup.

```text
ordinary WorldItem
    -> Inventory

GOLD WorldItem
    -> Wallet
```

When the player picks up Gold:

1. the Gold amount is added to `Wallet`;
2. the Gold `WorldItem` is removed;
3. no Gold `ItemStack` remains in the player's inventory.

Therefore collected Gold cannot be moved between slots or dropped with the normal inventory drop command.

---

## Gold HUD

The HUD permanently displays the current Wallet Gold balance.

The current icon/color is only a visual placeholder and can later be replaced with a proper coin sprite without changing the Wallet or pickup logic.

---

## Heart Repair item

Heart Repair is a normal consumable inventory item.

It is used through the existing `use item` input command (`F` with the current key bindings).

On use:

1. if at least one broken heart exists, one broken heart is repaired;
2. effective `maxHp` is recalculated;
3. current HP is restored to the effective maximum;
4. one Heart Repair item is consumed.

The item is consumed even when the player has no broken heart. In that case it still restores current HP to the current maximum.

If the player is already at full HP and has no broken heart, using the item still consumes it.

---

## Responsibility split

### Player

Owns:

- current HP and maximum HP;
- broken-heart count;
- damage cooldown;
- health restoration;
- Heart Repair effect;
- Wallet reference.

### Enemy

Owns:

- enemy HP;
- enemy lifecycle;
- shared lifecycle timers;
- ability to receive hits only while alive.

### Concrete enemy class

Owns:

- enemy-specific movement/attack behavior.

### World

Owns:

- collections of enemies and ground items;
- enemy removal;
- creation of Gold ground drops;
- Gold pickup routing to Wallet;
- current respawn checkpoint;
- player respawn gameplay operation;
- use-item dispatch from `PlayerInput`.

### GameScreen

Owns:

- fade-out/fade-in state;
- freezing normal world updates during death transition;
- triggering World respawn at full black;
- camera snap during teleport/respawn;
- rendering the black fade overlay.

### HudRenderer

Owns only the visual representation of:

- hearts;
- broken hearts;
- Gold balance.

It does not change health, broken-heart, or currency state.

---

## Important invariants

The current implementation should preserve these rules:

- an enemy cannot attack after entering `DYING`;
- a dying enemy cannot receive another combat hit;
- each defeated enemy is removed once;
- each defeated enemy performs its Gold roll once;
- Gold on the ground is a `WorldItem`, but collected Gold is Wallet currency;
- death never reduces inventory contents;
- death adds at most one broken heart;
- effective player max HP never drops below 4 with the current configuration;
- respawn restores health to the effective maximum;
- Heart Repair always consumes one item when used;
- rendering code does not own gameplay state.

---

## Deferred systems

The following are intentionally not part of this checkpoint:

- Shop purchase flow;
- first Heart Repair being free;
- later Heart Repair price of 20 Gold;
- any later Gold death penalty;
- advanced enemy navigation;
- Wisp and Wraith behavior;
- ordinary-night flee/dawn reward rules;
- save/load persistence;
- final heart, coin, enemy-death, and HUD assets.

These systems should reuse the health, Wallet, checkpoint, and enemy lifecycle foundations documented here.

# Farming System

## Overview

The farming system adds tile-based soil preparation, plant growth and harvesting to the world.

Farming state exists at runtime on top of the Tiled map. The `.tmx` map remains the source of the base terrain, while `FarmingSystem` stores which tiles have been tilled and which growable plants currently exist.

The system currently supports Wheat crops, Saplings that grow into Trees, and Bread as a farming-related consumable.

## Runtime Farming State

`FarmingSystem` stores two grids aligned with the dimensions of the tile map:

```text
boolean[][] tilledTiles
GrowablePlant[][] plants
```

The two grids answer different questions:

```text
tilledTiles[x][y]
→ is this tile currently farmland?

plants[x][y]
→ is there currently a growable plant on this tile?
```

A tilled tile may exist without a plant. After Wheat is harvested, the plant is removed while the farmland remains available for planting again.

## Growable Plant Hierarchy

`GrowablePlant` is the common base class for timed plant growth.

```text
GrowablePlant
├── Crop
│   └── Wheat
└── Sapling
    └── becomes Tree when mature
```

Both Crop and Sapling share the same generic growth lifecycle:

```text
EARLY
MIDDLE
LATE
MATURE
```

The shared base class stores the common growth state and timer logic, while subclasses keep their specific behaviour.

## Crop Model

`Crop` extends `GrowablePlant` and additionally stores its `CropType`.

The current crop type is `WHEAT`.

`CropType` stores crop-specific growth-duration metadata. Each planted crop owns its own timer, allowing plants placed at different times to grow independently.

## Sapling Model

`Sapling` also extends `GrowablePlant`.

A mature Sapling is not harvested. Instead:

```text
Sapling reaches MATURE
        ↓
World detects the mature Sapling
        ↓
Tree DestructibleObject is created
        ↓
Tree is added to the world
        ↓
Tree collision is registered
        ↓
Sapling is removed from FarmingSystem
```

`FarmingSystem` is responsible for plant growth state, while `World` is responsible for converting mature Saplings into world objects because `World` owns destructible objects and collision registration.

## Tiled Terrain Metadata

The Tiled map defines permanent terrain rules.

Tillable terrain uses:

```text
tillable = true
```

Tree-plantable terrain uses:

```text
treePlantable = true
```

These properties are stored on concrete tiles in the tileset rather than on the entire Ground layer.

The properties describe what the base terrain allows; they do not store runtime farming state.

## Growth Update

`World` owns the `FarmingSystem` and updates it during the normal world update.

```text
World.update(delta)
        ↓
FarmingSystem.update(delta)
        ↓
each existing GrowablePlant updates its timer
        ↓
GrowthStage changes when required
```

Growth uses gameplay `delta` time instead of the operating-system clock, so plants do not continue growing while the world is paused.

## Tilling

The Hoe uses:

```text
F → TargetMode.TILL
```

The existing tile-targeting system is reused.

A till target is valid only when the target is in range, the base terrain has `tillable = true`, the tile is not already tilled, and normal world validation succeeds.

A successful till action marks the tile as farmland in `FarmingSystem`.

Hoe durability is reduced only after a successful till.

The base Tiled map is never modified at runtime.

## Plant Targeting

Both Wheat Seeds and Saplings use:

```text
F → TargetMode.PLANT
```

The selected item determines the target rule.

### Wheat Seeds

Wheat can only be planted on empty farmland.

After successful planting:

1. one Wheat Seed is removed;
2. a Wheat Crop is created;
3. growth begins from the first stage;
4. targeting is cancelled when the selected seed stack becomes empty.

### Sapling

Saplings can only be planted on valid terrain marked:

```text
treePlantable = true
```

A Sapling cannot be planted on a tile already occupied by another growable plant.

Saplings use normal terrain rather than farmland.

## Rendering

Farming is rendered as a runtime layer above the Tiled ground.

```text
Tiled ground
    ↓
farmland texture
    ↓
growable-plant texture
    ↓
world entities and objects
```

`WorldRenderer` uses `SpriteBatch` for farming textures.

Growth-stage graphics are stored in spritesheets and split into `TextureRegion` arrays, allowing one image file per plant type instead of one image per stage.

The renderer selects the appropriate region according to the current `GrowthStage`.

`WorldRenderer` only displays state and does not contain farming gameplay rules.

## Harvesting Wheat

Mature Wheat is harvested with:

```text
E → Interact
```

No harvesting tool is required.

A successful Wheat harvest produces:

```text
WHEAT x1
```

and a seed roll:

```text
75% → WHEAT_SEED x1
25% → WHEAT_SEED x2
```

The Wheat and Seed drops are placed at different positions inside the original farm tile so they cannot randomly spawn in nearby invalid terrain such as water.

After harvesting, the plant is removed while the farmland remains tilled and can immediately be planted again.

## Bread

Bread is used with:

```text
F → use item
```

Bread heals the Player by 1 HP when current health is below maximum health.

If the Player is already at full health, Bread is not consumed.

Bread does not use tile targeting.

## Interaction Priority

`World.handleInteract(...)` coordinates interaction priority:

```text
1. Chest interaction
2. Mature Crop harvest
3. Ground-item pickup
```

Concrete interactions are separated into dedicated helper methods so one `E` press cannot trigger several unrelated actions in the same frame.

## Responsibilities

### `GrowablePlant`

- stores common growth-stage state;
- stores the growth timer;
- implements the shared timed growth lifecycle.

### `Crop`

- extends `GrowablePlant`;
- represents a harvestable crop;
- stores its `CropType`.

### `Sapling`

- extends `GrowablePlant`;
- represents a planted tree before conversion into a Tree world object.

### `CropType`

- stores crop-specific growth metadata.

### `GrowthStage`

- represents a generic timed plant-growth phase.

### `FarmingSystem`

- stores tilled runtime tiles;
- stores `GrowablePlant` objects by tile coordinate;
- creates and removes plants;
- updates plant growth.

### `TileMap`

- exposes permanent Tiled terrain rules such as `tillable` and `treePlantable`.

### `World`

- owns the farming system;
- validates farming target actions;
- consumes seeds and Hoe durability;
- coordinates harvesting and drops;
- converts mature Saplings into Trees;
- controls interaction priority.

### `WorldRenderer`

- renders farmland and plant-stage texture regions;
- does not contain farming gameplay rules.

## Current Rules

- Only terrain with `tillable = true` can be tilled.
- Tilling uses `F` while a Hoe is selected.
- Hoe durability is consumed only by a successful till.
- Wheat Seeds use `F` to plant on empty farmland.
- Saplings use `F` to plant on valid `treePlantable` terrain.
- Only one `GrowablePlant` may occupy a plant tile.
- Every growable plant has an independent gameplay-time timer.
- Mature Wheat is harvested with `E`.
- Harvesting Wheat does not require a tool.
- Wheat always drops one Wheat.
- Wheat drops one Seed with 75% probability and two Seeds with 25% probability.
- Harvest drops remain inside the harvested farm tile.
- Farmland remains tilled after harvesting.
- Mature Saplings are converted into Tree world objects.
- Bread heals 1 HP and is not consumed at full health.
