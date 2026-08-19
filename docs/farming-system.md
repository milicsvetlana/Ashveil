# Farming System

## Overview

The farming system adds tile-based soil preparation, crop planting, crop growth and harvesting to the world.

Farming state exists at runtime on top of the Tiled map. The `.tmx` map remains the source of the base terrain, while `FarmingSystem` stores which tiles have been tilled and which crops currently exist.

The current implementation supports the complete Wheat lifecycle.

---

## Runtime Farming State

`FarmingSystem` stores two grids aligned with the dimensions of the tile map:

```text
boolean[][] tilledTiles
Crop[][] crops
```

The two grids answer different questions:

```text
tilledTiles[x][y]
→ is this tile currently farmland?

crops[x][y]
→ is there currently a crop on this tile?
```

A tilled tile may exist without a crop.

This is important after harvesting because the crop is removed while the farmland remains available for planting again.

---

## Tiled Terrain Metadata

The Tiled map defines which base terrain types are allowed to become farmland.

Tillable terrain tiles use the custom boolean property:

```text
tillable = true
```

The property is stored on the concrete tile in the tileset, not on the entire Ground layer.

`TileMap.isTillable(...)` reads this property from the Ground layer cell.

The Tiled property describes a permanent rule of the terrain:

```text
"this terrain can be tilled"
```

It does not store runtime farming state.

Whether a specific tile has already been tilled is stored by `FarmingSystem`.

---

## Crop Model

A `Crop` represents one planted crop.

It stores:

```text
CropType
CropStage
growthTimer
```

Each crop owns its own timer, allowing crops planted at different times to grow independently.

The current crop type is:

```text
WHEAT
```

`CropType` stores the duration metadata used by the crop growth logic.

The current Wheat growth phases are:

```text
SEED
SPROUT
GROWING
MATURE
```

The configured phase durations are:

```text
SEED:     15 seconds
SPROUT:   20 seconds
GROWING:  25 seconds
```

Wheat therefore reaches `MATURE` after 60 seconds of gameplay time.

`MATURE` has no additional duration because the plant remains mature until harvested.

---

## Growth Update

`World` owns the `FarmingSystem` and updates it during the normal world update.

```text
World.update(delta)
        ↓
FarmingSystem.update(delta)
        ↓
each existing Crop updates its timer
        ↓
CropStage changes when required
```

Growth uses gameplay `delta` time instead of the operating-system clock.

Therefore crops do not continue growing while the world itself is paused.

---

## Tilling

The Stone Hoe uses:

```text
F → TargetMode.TILL
```

The existing tile-targeting system is reused.

A till target is valid only when:

- the target is inside the map;
- the target is inside Player targeting range;
- the base terrain has `tillable = true`;
- the tile is not already tilled;
- the tile is not blocked by invalid world content.

After a successful till action:

```text
FarmingSystem.till(tileX, tileY)
```

marks the tile as farmland.

Hoe durability is reduced only after a successful till action.

The base Tiled map is not modified.

---

## Planting Wheat

Wheat Seeds use:

```text
F → TargetMode.PLANT
```

A Wheat target is valid only when:

- the tile is already tilled;
- the tile does not already contain a crop;
- the normal target validation succeeds.

After successful planting:

1. one Wheat Seed is removed from the selected hotbar slot;
2. `FarmingSystem` creates a Wheat `Crop` on the target tile;
3. the crop begins at `CropStage.SEED`;
4. the crop timer begins at zero;
5. targeting is cancelled when the selected seed stack becomes empty.

---

## Rendering

Farming is rendered as a runtime layer above the Tiled ground.

The rendering order is conceptually:

```text
Tiled ground
    ↓
farmland texture
    ↓
crop-stage texture
    ↓
world entities and objects
```

`WorldRenderer` uses `SpriteBatch` to render farming textures.

The current textures are:

```text
farm_tile.png
wheat_seed.png
wheat_sprout.png
wheat_growing.png
wheat_mature.png
```

For each tilled tile, `farm_tile.png` is drawn.

If the tile also contains a crop, the texture matching the crop's current `CropStage` is drawn above the farmland texture.

`WorldRenderer` only displays farming state. It does not decide whether tilling, planting or harvesting is valid.

---

## Harvesting

A mature Wheat crop is harvested with:

```text
E → Interact
```

No harvesting tool is required.

Only crops in `CropStage.MATURE` can be harvested.

Harvesting is instant from the gameplay perspective.

A successful Wheat harvest produces:

```text
WHEAT x1
```

and a seed roll:

```text
75% → WHEAT_SEED x1
25% → WHEAT_SEED x2
```

The Wheat and Seed drops are placed at different positions inside the same farm tile.

Keeping both drops inside the original crop tile prevents them from being randomly placed into nearby invalid terrain such as water.

After harvesting:

```text
crops[x][y] = null
```

while:

```text
tilledTiles[x][y] = true
```

remains unchanged.

The same farmland tile can therefore be planted again immediately.

---

## Interaction Priority

`World.handleInteract(...)` coordinates interaction priority.

The current order is:

```text
1. Chest interaction
2. Mature Crop harvest
3. Ground-item pickup
```

The concrete interactions are separated into dedicated helper methods instead of being implemented as one large interaction method.

This prevents one `E` press from performing multiple unrelated interactions in the same frame.

---

## Responsibilities

### `FarmingSystem`

- stores tilled runtime tiles;
- stores crops by tile coordinate;
- creates crops;
- removes harvested crops;
- updates crop growth.

### `Crop`

- represents one planted crop;
- stores the crop type;
- stores the current growth stage;
- stores and updates its individual growth timer.

### `CropType`

- describes crop-specific growth-duration metadata.

### `CropStage`

- represents the visible growth phase of a crop.

### `TileMap`

- exposes the permanent `tillable` terrain rule from Tiled.

### `World`

- owns the farming system;
- validates farming target actions;
- consumes seeds and tool durability;
- coordinates harvesting and world-item drops;
- controls interaction priority.

### `WorldRenderer`

- renders farmland;
- renders the texture corresponding to each crop stage;
- does not contain farming gameplay rules.

---

## Current Rules

- Only terrain with `tillable = true` can be tilled.
- Tilling uses `F` while a Hoe is selected.
- Hoe durability is consumed only by a successful till.
- Wheat Seeds use `F` to plant.
- Wheat can only be planted on empty farmland.
- Every crop has an independent gameplay-time growth timer.
- Wheat progresses through `SEED`, `SPROUT`, `GROWING` and `MATURE`.
- Mature Wheat is harvested with `E`.
- Harvesting does not require a tool.
- Wheat always drops one Wheat.
- Wheat drops one Seed with 75% probability and two Seeds with 25% probability.
- Harvest drops remain inside the harvested farm tile.
- Farmland remains tilled after harvesting.
