# Tiled Map System

## Overview

Ashveil uses Tiled maps for world layout, static collision, terrain properties, and predefined object positions.

Static map collision and dynamic world-object collision are exposed through one movement-checking flow. `TileMap` interprets static Tiled data, while `CollisionSystem` combines that information with collision bounds registered by dynamic objects.

Other gameplay classes do not directly work with Tiled layers, cells, objects, or custom properties unless rendering requires access to the loaded map.

## Tools

### Aseprite

Aseprite is used to create and edit pixel-art tileset images.

The current tileset image is stored in:

```text
assets/tilesets/test_tiles.png
```

Each tile currently has a base size of `16 × 16` pixels.

When adding a new tile:

1. Open the tileset image in Aseprite.
2. Increase the canvas size by a multiple of `16`.
3. Draw the new tile in the added space.
4. Save the image using the existing filename.
5. Reload or reopen the tileset in Tiled.

### Tiled

Tiled is used to:

- create maps;
- place terrain tiles;
- define static collision;
- add custom tile properties;
- place predefined map objects.

The current files are:

```text
assets/maps/test_map.tmx
assets/tilesets/test_tiles.tsx
```

The `.tmx` file stores the map layout, layers, and object positions.

The `.tsx` file stores tileset information and custom properties attached to individual tiles.

### LibGDX

LibGDX loads the `.tmx` map and renders it using `OrthogonalTiledMapRenderer`.

Gameplay systems access map data through the `TileMap` class.

## Map Layers

### Ground

The `Ground` tile layer contains visible terrain such as grass and paths.

Tiles on this layer may contain custom properties that affect gameplay.

### Collision

The `Collision` tile layer represents static collision.

Any occupied cell on this layer is treated as blocked.

It is intended for permanent obstacles such as:

- water;
- map borders;
- cliffs;
- walls;
- other fixed terrain.

Dynamic objects are not added to the Tiled `Collision` layer. Trees, rocks, and future objects such as fences, buildings, and chests use the separate `CollisionSystem` and implement `CollidableObject`.

### Objects

The `Objects` object layer stores predefined positions.

The current object is:

```text
player_spawn
```

It defines the player's initial position.

`TileMap` reads the object position, while `World` decides to create the player at that position.

## Coordinate Systems

The project uses four related coordinate systems.

### Tile Coordinates

Tile coordinates represent positions in the map grid.

Example:

```text
tileX = 5
tileY = 3
```

Because indexing starts from zero, this means the sixth column and fourth row.

### World Coordinates

Entities store their positions using world coordinates.

The current logical tile size is:

```java
Config.TILE_SIZE = 16;
```

Therefore:

```text
tileX = 5
worldX = 5 × 16 = 80
```

Entities move continuously through world coordinates and are not restricted to whole tile positions.

### Render Coordinates

The game uses:

```java
Config.SCALE = 2.5f;
```

World positions are multiplied by this scale during rendering.

A logical tile size of `16` is drawn as:

```text
16 × 2.5 = 40
```

This rendered size is stored in:

```java
Config.TILE_DRAW_SIZE
```

Example:

```text
tileX = 5
worldX = 80
renderX = 200
```

Scaling changes visual size only. It does not change logical positions or collision.

### Screen Coordinates

Screen coordinates represent positions inside the game window.

The current initial window size is:

```text
1280 × 720
```

The camera determines which part of the world is visible. It does not change entity world positions or tile coordinates.

## TileMap Responsibilities

The `TileMap` class is responsible for:

- loading the Tiled map;
- accessing map layers;
- reading map dimensions;
- converting world coordinates to tile coordinates;
- converting tile coordinates to world coordinates;
- checking static collision;
- reading the player spawn position;
- reading terrain movement properties;
- disposing of the loaded map.

Important methods include:

```java
isBlocked(int tileX, int tileY)
isBlockedAtWorld(float worldX, float worldY)
worldToTileX(float worldX)
worldToTileY(float worldY)
tileToWorldX(int tileX)
tileToWorldY(int tileY)
getMovementMultiplierAtWorld(float worldX, float worldY)
```

## Unified Collision System

Movement collision is handled through the `com.ashveil.collision` package.

The current classes are:

```text
CollidableObject
CollisionSystem
```

### `CollidableObject`

`CollidableObject` is implemented by dynamic world objects that block movement.

Its contract is:

```java
Rectangle getCollisionBounds();
```

The returned `Rectangle` represents the logical area that blocks movement. It is independent from the rendered sprite size.

`ResourceObject` currently implements this interface. Future objects such as fences, buildings, and chests can implement the same contract without requiring changes to `Player`.

### `CollisionSystem`

`CollisionSystem` combines two collision sources:

1. blocked tiles from `TileMap`;
2. registered `CollidableObject` instances.

Its main movement check receives a proposed rectangular area and returns whether that area is blocked.

Conceptual flow:

```text
Player attempts movement
        ↓
CollisionSystem checks blocked Tiled cells
        ↓
CollisionSystem checks dynamic object bounds
        ↓
movement is allowed or rejected
```

The player does not need to know whether the obstacle is a wall tile, tree, rock, fence, or building.

### Registration lifecycle

`World` owns one `CollisionSystem`.

When a resource is spawned:

```text
create ResourceObject
→ add it to resourceObjects
→ register it in CollisionSystem
```

When a resource is destroyed:

```text
create its world drop
→ unregister it from CollisionSystem
→ remove it from resourceObjects
```

Unregistering is required. Removing an object only from the world list would otherwise leave an invisible collision obstacle behind.

### Player collision bounds

The current player collision bounds use the full logical tile size:

```text
width  = Config.TILE_SIZE
height = Config.TILE_SIZE
```

This is temporary while placeholder graphics are used.

The collision architecture does not depend on the full-tile size. When final sprites are introduced, the player and world objects can use smaller or offset rectangles without changing `CollisionSystem`.

For example, a future tree sprite may be visually large while only the trunk blocks movement.

### Separate axis checks

Horizontal and vertical movement remain checked separately:

```text
test X movement
→ apply X if free

test Y movement
→ apply Y if free
```

This allows the player to slide along walls and objects during diagonal movement instead of stopping completely.

### Blockers, terrain, and hazards

Not every special tile should be represented as a `CollidableObject`.

- Walls, rocks, tree trunks, and fences block movement.
- Grass, paths, mud, and wind terrain modify movement.
- Holes, portals, docks, and transitions allow entry and then trigger an effect.

Future holes should therefore be implemented as tile effects or hazards, not as blocking collision objects. The player must be able to enter a hole tile before the game can detect the fall and return the player to the dock.

## Initial Resource Placement

Initial trees and rocks are spawned procedurally by `World`.

The current placement rules ensure that:

- every existing `ResourceType` receives a guaranteed minimum count;
- additional resources may be spawned randomly;
- resources are not placed on blocked Tiled cells;
- two resources are not placed on the same tile;
- a clear area is reserved around the player spawn;
- every created resource is registered in `CollisionSystem`.

The clear spawn area prevents the player from starting inside a resource or being immediately surrounded by collidable objects.

The current rule is intentionally simple. More advanced island-specific resource tables, rarity rules, reserved paths, and guaranteed exits can be added later without changing the collision interface.

## Player Spawn

The player's starting position is not hardcoded in Java.

A point object named:

```text
player_spawn
```

is placed on the `Objects` layer in Tiled.

`TileMap` reads its `x` and `y` values.

`World` then creates the player using those coordinates.

The `Player` constructor still receives explicit coordinates. This allows the same constructor to later support:

- loading saved positions;
- respawning after death;
- arriving on another island;
- multiplayer spawning;
- special map transitions.

## Terrain Movement Multiplier

Terrain tiles can affect player movement speed through a custom Tiled property.

The property name is:

```text
movementMultiplier
```

Example values:

```text
Grass: 1.0
Path: 1.15
Mud: 0.75
```

Only special terrain tiles need to define this property.

If the property does not exist, `TileMap` returns:

```text
1.0
```

The player does not check tile names or tile IDs.

Instead, the player asks the map for the movement multiplier at its current position.

The final movement speed is calculated as:

```text
base speed × movement multiplier
```

This keeps gameplay behavior independent from tile appearance.

## Rendering

`WorldRenderer` renders the Tiled map using:

```java
OrthogonalTiledMapRenderer
```

The map renderer uses:

```java
Config.SCALE
```

Placeholder entities are rendered with `ShapeRenderer`.

Their positions are converted from world coordinates into render coordinates:

```java
entity.getX() * Config.SCALE
entity.getY() * Config.SCALE
```

Their rendered size is:

```java
Config.TILE_DRAW_SIZE
```

The Tiled map and placeholder entities therefore use the same scale and remain visually aligned.

## Current Architecture

```text
Aseprite
    creates and edits the tileset image

Tiled
    creates maps, layers, properties, and object positions

TileMap
    loads and interprets static Tiled data

CollisionSystem
    combines static tile collision with dynamic object collision

CollidableObject
    exposes collision bounds for dynamic blocking objects

World
    creates objects and registers or unregisters their collision bounds

Player
    requests movement checks without knowing obstacle types

WorldRenderer
    renders the map and world entities

CameraController
    controls which part of the world is visible
```

## Future Extensions

The map system is intended to support:

- multiple islands;
- separate Tiled maps;
- map transitions;
- additional terrain effects;
- arrival and respawn points;
- fixed map objects;
- smaller sprite-specific collision rectangles;
- collidable fences, buildings, and chests;
- tile hazards and trigger effects such as holes;
- different environment rules for each island.

The main island is planned as a hybrid map:

- fixed terrain and static collision from Tiled;
- procedurally spawned resources and enemies.

Smaller islands may use fully predefined Tiled layouts.
