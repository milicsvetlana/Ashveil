# World Map and Area Travel System

## Overview

Checkpoint 16 introduces the multi-area travel flow in Ashveil.

The player is no longer limited to a single playable map. Instead, the game supports multiple persistent areas, each with its own runtime state, and a world-map-based travel flow used through the boat system.

The current areas are:

```text
MAIN_ISLAND
WINDY_PLAINS
DARKROOT_ISLE
VEILSCAR_PASSAGE
```

Checkpoint 16 establishes:

- a reusable area-based world structure;
- world-map-driven boat travel;
- persistent multi-area save/load behavior;
- dock-based area activation;
- travel restrictions based on Gold and cooldown;
- map-defined interaction and spawn-exclusion regions.

---

## Area Structure

Ashveil no longer treats the entire game world as one mutable map.

`World` owns multiple `AreaRuntime` instances:

```text
World
    ↓
Map<AreaID, AreaRuntime>
```

Each `AreaRuntime` represents the runtime state of one area.

It owns area-specific systems and data such as:

- `TileMap`;
- `CollisionSystem`;
- `DestructibleObjectSystem`;
- `FarmingSystem`;
- `WorldItemSystem`;
- enemy collection;
- `ProjectileSystem`;
- `EnemySpawnSystem`;
- area-specific departure and night metadata.

Only one runtime is active at a time, but already visited runtimes remain stored inside `World`.

This allows an area to preserve its state while the player is somewhere else.

---

## AreaID

`AreaID` identifies each playable area.

The current values are:

```text
MAIN_ISLAND
WINDY_PLAINS
DARKROOT_ISLE
VEILSCAR_PASSAGE
```

`AreaID` is used by:

- `AreaManager`;
- `World`;
- `AreaRuntime`;
- world map markers;
- progression unlock state;
- save/load data.

Using an enum avoids relying on map filenames or display names as gameplay identifiers.

---

## Current Area and Initialized Areas

The current area and initialized areas are separate concepts.

`currentAreaId` identifies the area currently used for gameplay.

The internal runtime map may contain multiple initialized areas:

```text
MAIN_ISLAND → AreaRuntime
WINDY_PLAINS → AreaRuntime
DARKROOT_ISLE → AreaRuntime
```

For example:

```text
current area = WINDY_PLAINS
initialized areas = MAIN_ISLAND + WINDY_PLAINS
```

Returning to an already initialized area reuses its existing `AreaRuntime` instead of constructing a fresh world state.

---

## First Visit and Revisit Behavior

When a destination is activated, `World` first checks whether an `AreaRuntime` already exists for that `AreaID`.

Conceptually:

```text
runtime exists
→ reuse existing runtime

runtime does not exist
→ construct new AreaRuntime
→ store it in areaRuntimes
→ initialize first-visit resources
```

This distinction prevents resources and local world state from being regenerated every time the player returns to an island.

---

## Tiled Travel Objects

Travel uses named objects stored directly in the TMX maps.

### `dock_arrival`

Each travel destination contains a point object named:

```text
dock_arrival
```

It is stored in the `Objects` object layer.

This point defines the position where the player appears after boat travel.

`player_spawn` and `dock_arrival` have different purposes:

```text
player_spawn
→ initial New Game position

dock_arrival
→ arrival position after area travel
```

The distinction allows the starting position and later boat-arrival position to be configured independently.

### `boat_interaction`

Areas that allow the player to use the boat contain a rectangle object named:

```text
boat_interaction
```

It is stored in the `SpecialRegions` object layer.

When the player presses Interact, `World` checks whether the player's collision bounds overlap this region.

If the boat is already built, interacting with this region requests the world map.

---

## Boat Visibility

The boat artwork is stored as a Tiled layer named:

```text
Boat
```

Its visibility depends on persistent progression state.

Conceptually:

```text
boatBuilt = false
→ Boat layer hidden

boatBuilt = true
→ Boat layer visible
```

`World.updateBoatVisibility()` applies this rule whenever the active map changes or boat progression is restored.

This keeps the boat visual synchronized with gameplay state.

---

## Opening the World Map

`World` does not directly open Scene2D UI.

Instead, boat interaction sets a request flag.

Conceptually:

```text
player presses Interact near boat
↓
World validates boat interaction
↓
worldMapRequested = true
↓
GameScreen observes request
↓
GameScreen opens WorldMapUi
```

This avoids coupling `World` to UI classes.

The world owns gameplay state, while `GameScreen` coordinates screen and overlay behavior.

---

## WorldMapAccess

`WorldMapUi` does not directly access internal world systems such as `ProgressionState`, `Wallet`, or `AreaManager` implementation details.

Instead, `World` implements `WorldMapAccess`.

The interface exposes only the data and actions needed by the world map:

- current area;
- unlocked-area state;
- Gold amount;
- boat travel cost;
- travel cooldown state;
- travel readiness;
- travel execution.

Conceptually:

```text
WorldMapUi
    ↓
WorldMapAccess
    ↓
World
```

This keeps the UI dependent on a small contract instead of the complete gameplay model.

---

## World Map Marker States

Each area marker uses one of the following states:

```text
LOCKED
AVAILABLE
CURRENT
SELECTED
```

The state priority is:

```text
current area
→ CURRENT

locked area
→ LOCKED

selected unlocked area
→ SELECTED

otherwise
→ AVAILABLE
```

This separates marker presentation from the underlying progression state.

The marker image reacts to mouse clicks through Scene2D input handling.

A locked marker cannot become a travel target.

The current-area marker cannot become a destination because traveling to the already active area is invalid.

---

## Travel Requests

`WorldMapUi` does not call world travel logic directly from the button listener.

Instead, it stores UI intent.

Conceptually:

```text
player selects destination
↓
player presses TRAVEL
↓
WorldMapUi stores travel request
↓
GameScreen reads request
↓
World.travelToArea(destination)
```

This keeps responsibilities separated:

```text
WorldMapUi
→ presentation and UI intent

GameScreen
→ orchestration

World
→ gameplay validation and state mutation
```

---

## Travel Validation

Boat travel is allowed only when all required conditions pass.

Current checks include:

```text
boat is built
area is unlocked
destination is not the current area
travel cooldown is finished
player can afford the Gold cost
```

`World.travelToArea(...)` returns a boolean result.

```text
true
→ travel completed

false
→ travel rejected
```

The backend performs its own validation even if the UI currently shows the TRAVEL button as enabled.

This prevents UI state from becoming the only protection around gameplay rules.

---

## Gold Cost

Successful travel costs:

```java
Config.BOAT_TRAVEL_GOLD
```

The world map reads the current Gold and travel cost through `WorldMapAccess`.

The UI can therefore display conditions such as:

```text
Ready to travel
Not enough gold
```

The player's wallet remains the owner of the actual Gold state.

---

## Travel Cooldown

Successful boat travel starts a cooldown:

```java
Config.BOAT_TRAVEL_COOLDOWN
```

`World` stores the remaining cooldown and decreases it during normal world updates.

The world map itself does not pause the world.

Therefore, while the map is open:

```text
world simulation continues
↓
cooldown continues decreasing
↓
WorldMapUi refreshes travel state
↓
TRAVEL becomes available automatically at 0 seconds
```

The UI does not own a separate cooldown timer.

It reads the actual world state every frame.

---

## World Map Status Bar

The world map includes a status bar used to explain the current travel state.

Typical messages include:

```text
Select a destination
Ready to travel
Not enough gold
Travel cooldown: Xs
```

The status bar may also show:

```text
current Gold
travel cost
remaining cooldown
```

The decorative status-bar image belongs to `WorldMapUi`, while the displayed text is a Scene2D `Label` rendered above it.

---

## World Map Runtime Behavior

The world map is a real-time gameplay overlay.

Opening it does not pause `World.update()`.

While it is visible:

```text
day/night continues
enemies continue updating
projectiles continue updating
farming continues
boat cooldown continues
```

The player receives neutral gameplay input so UI interaction does not also move or attack.

This follows the same real-time UI principle already used by other gameplay interfaces.

---

## Death While the World Map Is Open

Because the world continues updating, the player can still die while the world map is visible.

Before the normal death transition begins, `GameScreen` closes the world map.

This prevents:

- the world map remaining visible over the death transition;
- its Scene2D stage retaining the input processor after death.

The world map is therefore treated as part of the normal overlay lifecycle.

---

## Scene2D Input Ownership

`WorldMapUi` owns its own Scene2D `Stage`.

When the world map opens:

```text
Gdx.input processor
→ WorldMapUi Stage
```

When it closes:

```text
input processor
→ cleared / returned to normal GameScreen flow
```

`GameScreen.show()` and `GameScreen.hide()` also account for the world-map stage so input ownership remains consistent across screen lifecycle changes.

---

## Area Runtime State During Travel

Inactive areas are not recreated every frame and do not continue running their complete local simulation.

Their stored runtime state remains available for future revisits.

`AreaRuntime` also stores metadata related to area departure and ordinary-night processing:

```text
lastDepartureDayCount
ordinaryNightDayCount
```

This information allows `World` to decide what should happen when an area becomes active again.

For example, an area revisited on a later day may need its old ordinary-night enemy state cleaned before the current night's spawning rules are applied.

---

## Multi-Area Save Model

The save format stores multiple areas.

Conceptually:

```text
SaveData
├── currentAreaId
└── areas
    ├── AreaSaveData MAIN_ISLAND
    ├── AreaSaveData WINDY_PLAINS
    └── ...
```

`currentAreaId` identifies the active area at the moment of saving.

`areas` contains the persistent states of initialized areas.

This is important because the current area is not the only area whose state must survive save/load.

---

## Saving Initialized Areas

`World` exposes a list of initialized `AreaRuntime` objects for persistence.

The save mapper iterates over them:

```text
for each initialized AreaRuntime
↓
create AreaSaveData
↓
add to SaveData.areas
```

Only initialized/visited areas need full runtime save data.

An area that has never been visited does not need a runtime snapshot yet.

---

## AreaSaveData

Each `AreaSaveData` stores persistent state belonging to one area.

Current saved area data includes:

- `areaId`;
- destructible objects;
- tilled tiles;
- plants;
- ground items;
- enemies;
- projectiles;
- ordinary-night spawn state;
- departure-day metadata;
- ordinary-night day metadata.

This turns the in-memory `AreaRuntime` into a JSON-friendly DTO representation.

---

## Loading Multiple Areas

Loading reconstructs all saved area runtimes rather than restoring only the current one.

Conceptually:

```text
read currentAreaId
↓
create World with saved current area
↓
for each AreaSaveData
    resolve AreaID
    get or create matching AreaRuntime
    apply saved state to that runtime
↓
restore active player/world state
```

This allows the following scenario:

```text
Main Island
→ drop item
→ travel to Windy Plains
→ drop different item
→ save on Windy Plains
→ load game
→ Windy item is restored
→ return to Main Island
→ Main item is also restored
```

The two areas preserve separate state across a complete save/load cycle.

---

## Progression Persistence

Travel progression is stored separately from area-local runtime state.

`ProgressionSaveData` includes travel-related progression such as:

- whether the Boat Kit was crafted;
- whether the boat was built;
- whether Old Jetty was discovered;
- unlocked areas.

These values are restored into `ProgressionState` during loading.

After progression restoration, boat visibility is refreshed so the active map matches the restored boat state.

---

## Save Validation

`SaveValidator` validates serialized unlocked-area values before they are restored.

Serialized enum names must correspond to real `AreaID` values.

Invalid or duplicate values are rejected according to the save-validation rules.

This prevents malformed area progression data from reaching the runtime mapper unchecked.

---

## NoNaturalSpawn Layer

Procedural natural resources such as trees and rocks should not appear in handcrafted traversal or gameplay spaces.

Examples include:

- bridges;
- docks;
- arena areas;
- important paths;
- manually designed structures.

For this reason, TMX maps may contain an object layer named:

```text
NoNaturalSpawn
```

The layer contains rectangular objects that represent spawn-exclusion regions.

---

## TileMap NoNaturalSpawn Check

`TileMap` reads the `NoNaturalSpawn` object layer as a `MapLayer`.

This differs from layers such as `Ground` and `Collision`, which are tile-grid layers represented by `TiledMapTileLayer`.

Conceptually:

```text
Ground / Collision
→ TiledMapTileLayer
→ grid cells

NoNaturalSpawn
→ MapLayer
→ map objects / rectangles
```

For a candidate resource tile, `TileMap` constructs the tile's world-space rectangle and tests it against the exclusion rectangles.

If there is an overlap:

```text
natural spawn blocked
```

This allows the player to walk across a bridge while still preventing a random tree or rock from spawning on it.

---

## Natural Resource Spawn Validation

The natural-object spawn flow now checks both terrain collision and map-defined spawn exclusions.

Conceptually:

```text
candidate tile
↓
inside map?
↓
not blocked by terrain?
↓
not inside NoNaturalSpawn?
↓
not overlapping forbidden runtime objects?
↓
valid natural spawn position
```

The same rule can be shared by different natural destructible-object types, such as trees and rocks.

This avoids hardcoding bridge or dock coordinates in Java.

---

## Separation of Responsibilities

The CP16 architecture follows several responsibility boundaries.

### `TileMap`

Responsible for interpreting map-authored data such as:

- collision layers;
- named object positions;
- rectangle interaction regions;
- NoNaturalSpawn regions.

### `AreaRuntime`

Responsible for runtime systems and state belonging to one area.

### `AreaManager`

Responsible for area definitions and current-area identity.

### `World`

Responsible for gameplay-level travel rules, area activation, cooldown, Gold validation, and ownership of initialized area runtimes.

### `WorldMapUi`

Responsible for world-map presentation, marker selection, status text, and UI requests.

### `GameScreen`

Responsible for coordinating overlays, Scene2D input ownership, travel requests, and renderer synchronization after an area switch.

### `SaveMapper`

Responsible for mapping runtime state to and from save DTOs.

---

## Current Architecture

```text
Tiled TMX maps
    define area layout, collisions, docks and interaction zones

AreaRuntime
    owns the runtime systems of one area

World
    owns all initialized AreaRuntime instances
    owns travel rules and progression-facing travel state

WorldMapAccess
    exposes a restricted travel contract to UI

WorldMapUi
    displays markers, status and travel controls
    records UI intent

GameScreen
    opens/closes the world map
    forwards travel requests to World
    synchronizes rendering after travel

SaveMapper
    serializes every initialized AreaRuntime
    restores every saved AreaRuntime

SaveData / AreaSaveData
    provide JSON-friendly persistent representations
```

---

## Design Decisions

The current implementation follows these rules:

```text
Travel does not create a new World.

Each visited area has its own persistent AreaRuntime.

New Game uses player_spawn.

Boat travel uses dock_arrival.

World Map UI does not own gameplay travel rules.

World revalidates travel before mutating gameplay state.

World Map does not pause gameplay.

Boat cooldown belongs to World, not UI.

Only initialized areas require full AreaSaveData.

currentAreaId identifies the active area, not the only saved area.

Tiled owns spatial configuration such as boat interaction and spawn-exclusion regions.

NoNaturalSpawn areas are map data rather than hardcoded Java coordinates.
```

---

## Checkpoint 16 Result

With Checkpoint 16 complete, Ashveil supports a full multi-area foundation:

```text
build boat
↓
interact at dock
↓
open world map
↓
select unlocked destination
↓
validate Gold and cooldown
↓
travel to destination dock
↓
retain previous area runtime
↓
revisit areas without resetting their local state
↓
save multiple initialized areas
↓
load them again with independent persistent state
```

This foundation is now ready for Checkpoint 17, where the outer islands can receive their actual progression content, encounters, rewards, and story-related gameplay.
