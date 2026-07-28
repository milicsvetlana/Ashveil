# Tiled Map System

## Overview

Ashveil uses Tiled maps for world layout, static collision, terrain properties, and predefined object positions.

The map-specific logic is kept inside the `TileMap` class. Other gameplay classes do not directly work with Tiled layers, cells, objects, or custom properties unless rendering requires access to the loaded map.

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

Dynamic objects such as trees, rocks, buildings, and chests will later use a separate dynamic collision system.

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

## Static Collision

The player collision check uses four points representing the corners of the player:

```text
top-left       top-right

bottom-left    bottom-right
```

For each point:

1. `Player` provides a world position.
2. `TileMap` converts it into tile coordinates.
3. `TileMap` checks the corresponding cell on the `Collision` layer.
4. Movement is blocked if any checked corner is inside a blocked tile.

Horizontal and vertical movement are checked separately, allowing the player to slide along walls when moving diagonally.

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
    loads and interprets Tiled data

World
    creates and manages gameplay objects

Player
    handles movement, combat, inventory, and interactions

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
- dynamic collision for resource objects and buildings;
- different environment rules for each island.

The main island is planned as a hybrid map:

- fixed terrain and static collision from Tiled;
- procedurally spawned resources and enemies.

Smaller islands may use fully predefined Tiled layouts.
