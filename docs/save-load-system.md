# Save and Load System

## Overview

Checkpoint 13 introduces persistent game saving and loading for Ashveil.

The system converts the current runtime world into a structured snapshot, serializes that snapshot to JSON, validates saved data before loading, and reconstructs a new runtime `World` from the validated data.

The current implementation supports:

- three save slots;
- asynchronous save-file writing;
- ordered save/load file operations;
- atomic replacement through a temporary file;
- one backup file per slot;
- fallback loading from backup when the primary save is invalid;
- save-version validation;
- explicit slot states: `EMPTY`, `VALID`, and `INVALID`;
- reconstruction of persistent Player, progression, day/night, area, farming, enemy, projectile, and night-spawn state;
- separate New Game and Load Game world initialization paths.

The save system stores gameplay state only. Rendering resources, UI state, temporary AI decisions, and other transient runtime data are reconstructed or reset instead of being serialized.

## Architecture Overview

The persistence flow is divided into four main responsibilities:

```text
World
  |
  v
SaveMapper
  |
  v
SaveData DTO graph
  |
  v
SaveManager
  |
  v
JSON files
```

Loading follows the reverse direction:

```text
JSON files
  |
  v
SaveManager
  |
  v
SaveValidator
  |
  v
validated SaveData
  |
  v
SaveMapper
  |
  v
World
```

The main classes are:

- `SaveService` — coordinates save/load requests and file-I/O execution;
- `SaveManager` — owns JSON serialization and save-file handling;
- `SaveValidator` — validates deserialized save data before reconstruction;
- `SaveMapper` — converts runtime state to DTOs and DTOs back to runtime objects;
- `SaveData` and related DTO classes — represent serialized persistent state;
- `SaveSlotStatus` — represents whether a slot is empty, valid, or invalid.

## Save Slots

Ashveil currently supports three slots.

Valid slot numbers are:

```text
1
2
3
```

`SaveManager` validates the slot number before producing any file path.

Each slot may use three files during its lifecycle:

```text
slot_1.json
slot_1.tmp
slot_1.bak
```

The same naming pattern is used for slots 2 and 3.

### Primary file

The `.json` file is the current primary save.

### Temporary file

The `.tmp` file is used while a new save is being written.

It prevents an incomplete write from immediately replacing the previous valid primary save.

### Backup file

The `.bak` file stores the previous primary save before it is replaced by a new successful save.

Only one backup is currently kept per slot.

## Save Slot Status

Save slots expose three logical states through `SaveSlotStatus`:

```text
EMPTY
VALID
INVALID
```

### `EMPTY`

Neither the primary save nor the backup exists.

Selecting an empty slot may safely start a New Game.

### `VALID`

At least one usable save exists.

A slot is still considered valid when the primary file is broken but the backup file passes parsing and validation.

### `INVALID`

A save file exists, but neither the primary nor the backup can produce valid `SaveData`.

An invalid slot is not treated as empty.

This prevents the game from silently starting a New Game over a broken or incompatible save.

The future save-slot UI can use this distinction to present different visual states to the player.

## SaveData Structure

`SaveData` is the root serialized object.

Its current persistent data includes:

- save format version;
- save timestamp;
- total play time;
- Player state;
- day/night state;
- progression state;
- current area ID;
- saved area snapshots.

The save format is intentionally based on DTO classes instead of serializing the runtime `World` object directly.

This keeps persistence independent from rendering resources, LibGDX runtime objects, internal system references, temporary gameplay state, and implementation details that should not become part of the file format.

## Runtime Snapshot Creation

Saving begins in `SaveService.requestSave(...)`.

The important rule is:

> The `World` is converted to `SaveData` before the background file-I/O task starts.

Conceptually:

```text
game thread
    |
    v
SaveMapper.createSaveData(world)
    |
    v
detached SaveData snapshot
    |
    v
I/O executor
    |
    v
SaveManager.save(...)
```

The background thread therefore never reads the mutable live `World`.

This prevents the save thread from observing partially changed gameplay state while the game continues updating.

## SaveMapper

`SaveMapper` is the translation boundary between runtime objects and persistence DTOs.

It supports both directions:

```text
World -> SaveData
SaveData -> World
```

Runtime classes should not contain JSON-specific logic.

For example, `Player` should not know how its state is represented in JSON, `EnemySpawnSystem` should not know about `NightSpawnSaveData`, and `FarmingSystem` should not know about `PlantSaveData`.

`SaveMapper` knows both representations and performs the conversion.

## Player State

The persistent Player state includes the gameplay values required to continue a saved game:

- world position;
- current HP;
- broken hearts;
- Gold balance;
- selected hotbar slot;
- inventory contents;
- item quantities;
- individual item durability.

Inventory reconstruction preserves exact slot positions instead of simply adding items back in arbitrary order.

Each saved inventory entry contains enough information to rebuild the original stack:

```text
slot index
item type
quantity
durability
```

## Progression State

Persistent progression includes the flags and unlocked crafting categories required to continue progression correctly.

The existing runtime `ProgressionState` object is updated instead of being replaced because other runtime systems may already hold a reference to that same object.

The persistent state currently includes:

- first-tree reward state;
- ordinary-night Wisp unlock;
- ordinary-night Wraith unlock;
- unlocked crafting categories.

## Day/Night State

The saved day/night state includes:

- current day number;
- current `DayPhase`;
- elapsed timer inside the current phase.

The current phase duration is derived from the phase and day number and is not stored separately.

Transient flags such as `justBecameNight` and `justBecameDay` are not persisted because they represent one-frame runtime events.

## Area Persistence

`SaveData` stores a `currentAreaId` and a collection of `AreaSaveData` objects.

During loading, `SaveMapper` finds the `AreaSaveData` whose `areaId` matches `currentAreaId`.

The mapper does not assume that the current area is always the first element in the list.

## Destructible Objects

Persistent destructible-object state includes:

- object type;
- world position;
- current HP.

Objects are reconstructed through `DestructibleObjectSystem`, so the normal world integration path, including collision registration, is preserved.

### Chest restoration

A Chest uses the same destructible-object reconstruction path but additionally restores its own inventory.

Each Chest keeps independent storage, including saved slot positions, quantities, and durability.

## Farming State

Farming persistence stores both soil state and growable plants.

### Tilled tiles

Every saved tilled tile stores:

```text
tileX
tileY
```

### Plants

Each saved plant stores:

```text
tile position
plant kind
crop type when applicable
growth timer
```

Current plant kinds are:

```text
CROP
SAPLING
```

`GrowthStage` is not stored separately.

The persistent value is the plant's `growthTimer`, and the runtime plant recalculates its current `GrowthStage` after restore.

## Ground Items

Persistent ground-item state includes:

- world position;
- item type;
- quantity;
- durability.

Ground items are reconstructed through a dedicated bulk restore path instead of the normal gameplay `add(...)` operation, because normal insertion may merge nearby stacks or perform other spawn-related behavior.

Ground-item lifetime is intentionally not persisted.

After loading, restored ground items start with a fresh runtime lifetime because lifetime is treated as cleanup/performance state rather than important gameplay progression.

## Enemy Persistence

Persistent enemy state includes:

- enemy type;
- world position;
- current HP.

The concrete enemy is recreated through the enemy-spawn construction path so required runtime dependencies are supplied correctly.

Current enemy types are:

```text
SHADE
WISP
WRAITH
```

Detailed AI state is intentionally not persisted.

Examples include attack cooldowns, Wisp charge/dash state, Wraith approach/attack/retreat state, navigation waypoints, and temporary AI decisions.

After loading, each enemy begins from its normal initial AI state while preserving its saved type, position, and HP.

Only living enemies are included in the save snapshot.

## Projectile Persistence

Active projectiles preserve:

- world position;
- velocity X;
- velocity Y;
- damage;
- remaining lifetime.

A projectile is reconstructed from its existing velocity rather than recalculating velocity from a direction and speed.

Remaining lifetime is restored so a projectile close to disappearing does not receive a fresh full lifetime.

## Night Spawn State

Ordinary-night spawning is restored independently from already active enemies.

Persistent night-spawn state includes:

- remaining enemy queue;
- spawn timer;
- spawn interval.

The queue preserves order and allows duplicate enemy types.

Loading does not call normal night initialization because that would generate a new random queue.

Instead, the existing `EnemySpawnSystem` receives the saved queue and timer state.

## Save Validation

Deserialized JSON is never trusted automatically.

`SaveManager` parses JSON into DTOs and then calls `SaveValidator`.

If validation fails, that file is treated as unusable.

Validation covers structural and gameplay invariants including:

- supported save version;
- required root objects;
- valid Player state;
- valid inventory slots;
- valid item types, quantities, and durability;
- valid day/night phase and timer;
- valid progression categories;
- current area existence;
- valid destructible-object types and HP;
- valid Chest inventories;
- valid farming coordinates and plant data;
- duplicate farming positions;
- valid enemy types and HP;
- valid projectile state;
- valid night-spawn queue entries and timers.

`SaveValidator` protects the persistence boundary, while runtime classes still defend their own invariants.

## Save Versioning

The root save contains a `saveVersion`.

`SaveValidator` accepts only `SaveConstants.CURRENT_SAVE_VERSION`.

A save with another version is rejected.

Migration between versions is not currently implemented.

## Atomic Save Process

`SaveManager.save(...)` uses a temporary-file workflow:

```text
create JSON snapshot
        |
        v
write complete data to .tmp
        |
        v
copy previous primary save to .bak
        |
        v
replace primary file with .tmp
```

The final move first attempts `ATOMIC_MOVE`.

When the platform does not support atomic moves, the implementation falls back to a normal replace move.

If file writing fails, the temporary file is deleted and the failure is reported.

## Backup Recovery

Loading always tries the primary file first.

```text
read primary
    |
    +-- valid -> use it
    |
    +-- missing / corrupt / invalid
            |
            v
        read backup
            |
            +-- valid -> use it
            |
            +-- missing / corrupt / invalid -> load failure
```

A broken primary file does not make the slot invalid when the backup remains usable.

## SaveService and File-I/O Thread

`SaveService` owns a single-thread `ExecutorService` created with:

```java
Executors.newSingleThreadExecutor()
```

The single worker thread processes submitted file-I/O tasks in order.

### Saving

Saving uses `execute(...)` because no return value is required.

The `SaveData` snapshot is created on the game thread before the save task is submitted.

### Loading

Loading uses `submit(...)` because a result is required.

`submit(...)` returns:

```java
Future<SaveData>
```

`Future.get()` returns immediately when the result is ready, or blocks the current thread until the task finishes.

The executor performs only the file-I/O part:

```text
SaveManager.load(...)
```

`SaveMapper.createWorld(...)` remains outside the I/O task.

### Ordered save and load operations

If the user triggers:

```text
SAVE
LOAD
```

the single-thread executor preserves this order:

```text
1. SAVE
2. LOAD
```

The load therefore cannot overtake the pending save.

The same ordering is used when inspecting slot status.

## Executor Shutdown

`SaveService` shuts down its executor only when the application closes.

The shutdown flow is:

```text
shutdown()
-> reject new tasks, finish queued tasks

awaitTermination(...)
-> wait for existing work to finish

shutdownNow()
-> used only when the timeout expires or the waiting thread is interrupted
```

The current timeout is 10 seconds.

If the waiting thread receives `InterruptedException`, the interrupted status is restored with:

```java
Thread.currentThread().interrupt();
```

## SaveService Ownership

`SaveService` belongs to `GameApp`, not to an individual `GameScreen`.

It is created once for the application and shut down when the application is disposed.

A `GameScreen` uses the service but does not own its lifecycle.

## New Game and Load Game Separation

The New Game path creates a fresh `World`.

The Load Game path receives the `World` reconstructed by the save system.

```text
New Game
-> new World()
-> GameScreen

Load Game
-> SaveService.loadWorld(slot)
-> restored World
-> GameScreen
```

`GameScreen` does not decide whether a slot is new or existing.

## GameApp Slot Flow

`GameApp` exposes separate operations for:

```text
startNewGame(slot)
loadGame(slot)
openSlot(slot)
```

`openSlot(...)` uses `SaveSlotStatus`:

```text
EMPTY
-> startNewGame(slot)

VALID
-> loadGame(slot)

INVALID
-> do not overwrite the slot automatically
```

The current CP13 test flow automatically opens slot 1 because the Main Menu and slot-selection screen belong to the next checkpoint.

## Screen Lifecycle

Screen replacement is centralized so the application:

1. remembers the current screen;
2. activates the new screen;
3. disposes the previous screen.

`SaveService` is not disposed during screen changes because it belongs to `GameApp`.

## New Game vs Load Initialization

A loaded game must not execute New Game-only initialization such as generating another set of initial resources.

The runtime world therefore separates common system construction, New Game initialization, and Load Game reconstruction.

`World.createForLoad(...)` prepares runtime infrastructure without generating fresh gameplay state that would duplicate saved objects.

## Persistent and Transient State

### Persistent examples

- Player position and health;
- broken hearts;
- Gold;
- inventory contents and durability;
- progression unlocks;
- day and phase timer;
- destructible-object HP;
- Chest storage;
- tilled soil;
- plant growth timers;
- ground-item contents;
- enemy type, position and HP;
- projectile velocity and remaining lifetime;
- night-spawn queue and timers.

### Transient examples

- rendering resources;
- active UI overlay;
- hover and selection state;
- one-frame day/night transition flags;
- enemy AI state and waypoints;
- hit-flash timers;
- temporary targeting state;
- ground-item cleanup lifetime.

## Failure Handling

The current system distinguishes:

```text
EMPTY
-> no primary or backup exists

VALID
-> primary or backup parses and validates

INVALID
-> save files exist, but neither is usable
```

An invalid slot is never treated as an empty slot automatically.

Unexpected exceptions raised inside a submitted I/O task are surfaced through `ExecutionException`.

Interrupted waiting restores the thread interruption state before reporting failure.

## Manual Validation Performed During CP13

The current Save/Load flow has been manually checked for:

- empty slot starting a New Game;
- valid slot loading a previous game;
- application restart restoring saved world state;
- Player and inventory reconstruction;
- destructible objects and Chest contents;
- farming and growth progress;
- ground items;
- enemies;
- projectiles;
- night-spawn state;
- invalid save handling;
- corrupt primary save falling back to a valid backup;
- unsupported save versions being rejected;
- correct `EMPTY`, `VALID`, and `INVALID` slot status.

## Current Limitations

The current persistence system intentionally does not implement:

- save-format migration;
- multiple backup generations;
- cloud saving;
- multiplayer synchronization;
- save-slot deletion UI;
- final save-slot metadata presentation;
- Main Menu slot selection;
- character/avatar metadata.

These are outside CP13.

## CP14 Integration

The next checkpoint can build the user-facing flow on top of the existing backend:

```text
Main Menu
    |
    v
Start Game
    |
    v
Save Slot Screen
    |
    +--> EMPTY slot -> New Game
    |
    +--> VALID slot -> Load Game
    |
    +--> INVALID slot -> unavailable/corrupt state
```

Custom button textures, menu backgrounds, save-slot frames, avatar images, and hover/pressed states remain presentation concerns above the persistence backend.

## Responsibility Summary

### `SaveService`

- coordinates save/load requests;
- creates snapshots before background I/O;
- owns the single-thread I/O executor;
- preserves file-operation ordering;
- reconstructs runtime worlds after loading;
- exposes save-slot status.

### `SaveManager`

- owns save-file paths;
- serializes and deserializes JSON;
- performs temporary-file saving;
- creates backups;
- replaces primary saves;
- performs primary-to-backup fallback;
- determines slot status.

### `SaveValidator`

- verifies save version;
- rejects malformed or inconsistent DTO state;
- protects runtime reconstruction.

### `SaveMapper`

- converts runtime state to DTOs;
- reconstructs runtime state from DTOs;
- keeps file representation separate from gameplay classes.

### `GameApp`

- owns `SaveService`;
- chooses New Game or Load Game for a slot;
- manages gameplay-screen replacement.

### `GameScreen`

- uses the selected runtime `World`;
- requests saves for its active slot;
- does not own SaveService lifecycle.

## Important Rules

- The live `World` is never serialized directly.
- A detached snapshot is created before asynchronous file writing starts.
- Save-file I/O is ordered through one single-thread executor.
- Runtime world reconstruction is not performed on the I/O executor.
- The previous primary save is preserved as one backup before replacement.
- Invalid external save data is rejected before runtime reconstruction.
- A backup may keep a slot valid when the primary file is corrupt.
- An invalid slot is never treated as empty automatically.
- New Game initialization and Load Game reconstruction remain separate.
- Persistent state is stored only when it materially affects continued gameplay.
- Transient UI, rendering, and short-lived AI state are reconstructed or reset.
