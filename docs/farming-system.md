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
