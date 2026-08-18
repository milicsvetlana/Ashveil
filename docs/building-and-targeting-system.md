# Building and Targeting System

## Purpose

The building and targeting system provides a reusable foundation for actions that require selecting a tile in the world.

It currently supports:

- `PLACE` — used for placing buildable objects such as Fence.
- `PLANT` — reserved for planting crops and saplings.
- `TILL` — reserved for preparing soil with a Hoe.

The system separates physical input, target calculation, gameplay validation, world state changes, and rendering.

## Architecture Overview

```text
Mouse / Keyboard
      |
      v
GameScreen
      |
      +--> TileTargetingSystem
      |        |
      |        v
      |   tileX / tileY
      |   worldX / worldY
      |
      +--> World.isCurrentTargetValid(...)
      |        |
      |        v
      |     true / false
      |
      +--> WorldRenderer.renderTargetPreview(...)
      |
      +--> World.handleTargetAction(...)
               |
               +--> PLACE
               +--> PLANT
               +--> TILL
```

### Responsibility split

#### `GameScreen`

`GameScreen` handles screen-level input and connects the major systems.

Its responsibilities include:

- reading keyboard input;
- reading mouse button input;
- updating `TileTargetingSystem`;
- forwarding target actions to `World`;
- asking `World` whether the current target is valid;
- asking `WorldRenderer` to draw the preview;
- cancelling targeting when menus or death transitions require it.

`GameScreen` does not decide gameplay rules such as whether a Fence may be placed on a tile.

#### `TileTargetingSystem`

`TileTargetingSystem` answers the question:

> Where is the player currently targeting?

It:

1. reads the current mouse position;
2. converts screen coordinates to world-render coordinates through the camera;
3. converts render coordinates back to logical world coordinates using `Config.SCALE`;
4. converts logical world coordinates to tile coordinates;
5. snaps the target position to the tile grid.

The system stores:

- `tileX`
- `tileY`
- `worldX`
- `worldY`

It does not decide whether the target is valid.

#### `World`

`World` owns gameplay state and gameplay rules.

For targeting/building it:

- stores the active `TargetMode`;
- validates the current target;
- executes target actions;
- creates objects in the world;
- changes the inventory after successful actions;
- registers placed collidable objects in `CollisionSystem`.

#### `WorldRenderer`

`WorldRenderer` only renders world state.

For targeting it draws the current tile preview:

- green when the target is valid;
- red when the target is invalid.

The renderer does not perform placement validation and does not modify the world.

## Target Modes

Target modes are represented by `TargetMode`.

```java
public enum TargetMode {
    NONE,
    PLACE,
    PLANT,
    TILL
}
```

### `NONE`

No tile-targeted action is currently active.

### `PLACE`

Used for placing world objects.

Current example:

- Fence

Future example:

- Chest

### `PLANT`

Reserved for planting.

Planned examples:

- Wheat Seed
- Sapling

### `TILL`

Reserved for preparing soil.

Current tools that activate it:

- Wooden Hoe
- Stone Hoe

## Item Targeting Metadata

`ItemType` stores the `TargetMode` associated with an item.

Examples:

```text
Fence       -> PLACE
Wheat Seed  -> PLANT
Wooden Hoe  -> TILL
Stone Hoe   -> TILL
```

Placeable items also store `placedObjectType`.

Example:

```text
ItemType.FENCE
    |
    +--> TargetMode.PLACE
    |
    +--> DestructibleObjectType.FENCE
```

This allows generic placement logic without adding a separate branch for every simple placeable object.

Non-placeable items use `null` for `placedObjectType`.

## Targeting Lifecycle

The active target mode is stored in `World`.

### Activating targeting

Pressing `F` while a targetable item is selected activates its target mode.

```text
Fence selected
      |
      v
Press F
      |
      v
TargetMode.PLACE
```

### Toggling targeting

Pressing `F` again while the same target mode is active cancels it.

```text
PLACE + F -> NONE
```

### Cancelling targeting

Targeting is cancelled when:

- `ESC` is pressed;
- the selected hotbar slot changes;
- a menu is opened;
- the player enters the death transition;
- the last required placement item is consumed.

## Target Preview

The preview is rendered only while:

```text
TargetMode != NONE
```

`GameScreen` obtains the current target from `TileTargetingSystem`, asks `World` whether it is valid, and passes the result to `WorldRenderer`.

```text
TileTargetingSystem
        |
        v
World.isCurrentTargetValid(...)
        |
        +--> true  -> green preview
        |
        +--> false -> red preview
```

The current Fence preview uses a simple tile outline. Final object sprites can replace placeholder rendering later without changing targeting logic.

## Target Validation

`World.isCurrentTargetValid(...)` is the single source of truth for target validation.

Both the preview and the actual target action rely on the same validation logic.

Current validation checks:

1. target tile is inside map bounds;
2. target tile is inside player targeting range;
3. target terrain is not blocked;
4. target does not overlap an Enemy;
5. target does not overlap an existing `DestructibleObject`;
6. in `PLACE` mode, target does not overlap the Player.

### Target range

Target range is defined in tiles.

The current range is:

```text
5 tiles
```

The distance is checked using squared Euclidean distance:

```text
distanceSquared = dx * dx + dy * dy
```

This avoids calculating a square root every frame.

### Mode-specific validation

Not every targeting mode uses identical rules.

For example:

- `PLACE` cannot overlap the Player.
- `TILL` may later allow working on the tile occupied by the Player.
- `PLANT` will later require Farmland.
- `TILL` will later require valid tillable terrain.

Farming-specific validation is intentionally deferred until the farming state exists.

## Target Action Flow

The physical mouse click is detected in `GameScreen`.

`GameScreen` then forwards the snapped target coordinates to `World`.

```text
Left Mouse Button
       |
       v
GameScreen.handleTargetActionInput()
       |
       v
World.handleTargetAction(...)
```

`World.handleTargetAction(...)`:

1. verifies that a target mode is active;
2. verifies that the target is valid;
3. delegates to the method for the active target mode.

Conceptually:

```text
PLACE -> placeTarget(...)
PLANT -> plantTarget(...)
TILL  -> tillTarget(...)
```

The input device itself is not known by `World`.

# Destructible Object Model

The former resource-only model was generalized:

```text
ResourceObject -> DestructibleObject
ResourceType   -> DestructibleObjectType
```

This reflects the fact that Trees, Rocks, Fences, and future destructible world structures share the same core runtime behavior.

`DestructibleObject`:

- extends `WorldObject`;
- implements `Hittable`;
- implements `CollidableObject`;
- has HP;
- has collision bounds;
- can receive hits;
- has a `HitCategory`;
- can block movement;
- can be destroyed.

Current types include:

- `TREE`
- `ROCK`
- `FENCE`

## Natural Spawning

`DestructibleObjectType` contains metadata indicating whether a type may spawn naturally.

Current behavior:

```text
TREE  -> naturally spawned
ROCK  -> naturally spawned
FENCE -> not naturally spawned
```

Initial world generation filters object types using this metadata.

## Destructible Object Drops

Drop mapping is owned by `World` through:

```java
Map<DestructibleObjectType, ItemType>
```

Conceptually:

```text
TREE  -> WOOD
ROCK  -> STONE
FENCE -> FENCE
```

This avoids a circular enum dependency between `ItemType` and `DestructibleObjectType`.

Drop amount remains part of the destructible object type metadata through `minDrop` and `maxDrop`.

# Fence Building

Fence is the first implemented buildable object.

## Placement flow

```text
Fence selected in hotbar
        |
        v
Press F
        |
        v
TargetMode.PLACE
        |
        v
Move mouse over tile
        |
        +--> green = valid
        +--> red   = invalid
        |
        v
Left click on valid tile
        |
        v
World.placeTarget(...)
        |
        +--> read selected ItemType
        +--> obtain placedObjectType
        +--> consume one Fence item
        +--> create DestructibleObject(FENCE)
        +--> add it to destructibleObjects
        +--> register it in CollisionSystem
```

The Fence item is consumed only after a valid placement action.

Invalid clicks do not consume items.

If more Fence items remain in the selected stack, `PLACE` mode stays active so multiple Fences can be placed consecutively.

When the last Fence is consumed, targeting is cancelled.

## Fence Runtime Behavior

After placement, Fence behaves as a normal destructible world object.

It:

- exists in `destructibleObjects`;
- blocks ground movement;
- participates in collision;
- can receive player hits;
- can be destroyed;
- drops one Fence item when destroyed.

Fence currently uses placeholder rendering so its placement can be visually tested.

Final Fence art can later replace the placeholder without changing gameplay logic.

# Current Input Mapping Relevant to This System

```text
F            -> use selected item / toggle target mode
ESC          -> cancel/back
1-5          -> select hotbar slot
Left Click   -> execute target action
K            -> normal primary combat/tool action
E            -> interact
```

`K` remains the normal tool/combat action even when a Hoe exists.

`TILL` is activated through `F` and executed through the tile-targeting system.

# Current Status

Implemented:

- mouse-to-tile targeting;
- snapped world target coordinates;
- target modes;
- item-to-target-mode metadata;
- generic placeable object metadata;
- target mode lifecycle;
- target preview;
- basic shared validation;
- mode-specific Player overlap rule for `PLACE`;
- generic target action routing;
- `DestructibleObject` refactor;
- natural-spawn filtering;
- destructible-object drop mapping;
- Fence placement;
- Fence collision;
- Fence destruction and drop;
- inventory consumption on successful placement;
- placeholder Fence rendering.

Not yet implemented:

- farming terrain state;
- tilling;
- planting;
- crop growth;
- harvesting;
- Chest inventory/interactions;
- enemy AI intentionally attacking Fences;
- final Fence sprites/connected visuals;
- save/load persistence for placed objects.

# Planned Extensions

The existing targeting foundation is intended to be reused by:

```text
PLACE
├── Fence
└── Chest

PLANT
├── Wheat Seed
└── Sapling

TILL
└── Hoe
```

Future systems should reuse:

- `TileTargetingSystem` for target location;
- `World.isCurrentTargetValid(...)` for gameplay validation;
- `World.handleTargetAction(...)` for action routing;
- `WorldRenderer` for preview rendering.

New systems should not duplicate mouse-to-tile conversion or placement validation logic.

## Chest Placement and Interaction

`CHEST` is a placeable item that uses `TargetMode.PLACE`.

After normal placement validation succeeds, `World` creates a `Chest` through the destructible-object creation path.

Unlike ordinary `DestructibleObject` instances, `Chest` extends `DestructibleObject` because it owns additional runtime state: a 15-slot `Inventory`.

Every placed Chest therefore has independent storage.

Nearby Chest interaction uses `E`. `World` selects the nearest Chest inside the interaction range and exposes it as the active Chest to `GameScreen`.

While the Chest overlay is open, normal world gameplay is paused.

Chest destruction uses the normal destructible-object lifecycle. The Chest item is dropped together with every stored item, while existing `ItemStack` objects are preserved so tool durability is not reset.

Detailed Chest UI and storage behaviour is documented in:

`docs/chest-storage-system.md`
