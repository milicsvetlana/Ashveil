# Ashveil

Ashveil is a 2D top-down dark-fantasy survival/adventure game developed in Java with LibGDX as an OOP university project.

The project is built around a systems-first architecture: gameplay logic, rendering, input, persistence, UI, and progression are kept in separate responsibilities so that new content can be added without concentrating the entire game inside `GameScreen` or `World`.

The long-term game loop combines survival, exploration, farming, crafting, combat, progression across multiple areas, and a narrative guidance layer delivered by Ceca.

---

## Current Project Status

The project already contains the main technical foundation needed for the final single-player game.

Implemented systems include:

- player movement and directional interaction;
- collision and tile-based world logic;
- smooth camera tracking;
- inventory and hotbar;
- item pickup and dropping;
- crafting;
- resource harvesting;
- tools and tool/resource progression;
- farming and crop growth;
- building and placement targeting;
- chest storage;
- health, damage, healing, death, and gold;
- day/night progression;
- enemy spawning;
- melee/ranged-style enemy behavior and navigation support;
- multiple enemy types and enemy-specific behavior;
- save/load with multiple slots;
- asynchronous save-file loading;
- main menu;
- save-slot UI;
- character creation with persistent character names;
- loading screen;
- persistent settings;
- fullscreen/windowed display configuration;
- initial localization infrastructure.

The remaining work is primarily progression/content, additional areas, narrative guidance, endgame systems, final art/audio, polish, and packaging.

---

## Core Gameplay

### Player

The player can:

- move using directional controls;
- dash;
- attack or use tools;
- interact with world objects;
- pick up and drop items;
- select hotbar slots;
- use consumable items;
- open crafting and other overlays.

Player-facing controls are kept separate from entity logic through input/key-binding classes.

### World and Map

The world uses a tile-based map with collision and interaction data.

The map layer supports systems such as:

- blocked/passable terrain;
- harvestable world objects;
- placement targeting;
- farmland;
- chests;
- enemies;
- world items;
- future area transitions.

Tiled-map loading and collision responsibilities are kept outside gameplay rendering.

### Camera

The camera follows the player while respecting map boundaries.

The camera is handled separately from the player and world state so rendering concerns do not become part of entity logic.

---

## Inventory and Hotbar

The player has an inventory and hotbar used by the rest of the gameplay systems.

Supported behavior includes:

- storing stackable items;
- non-stackable tools;
- selecting active hotbar slots;
- item pickup;
- dropping one item;
- dropping a full stack;
- moving items between inventory and storage interfaces.

The hotbar is intended to remain the primary quick-access interface during gameplay.

---

## Crafting

Crafting is data-driven rather than being hardcoded directly into UI logic.

Recipes define the materials required to create items, while the UI presents currently available crafting options.

The crafting system is designed so that new recipes can be added without rewriting the core crafting logic.

Crafting is used by progression systems such as:

- tools;
- weapons;
- food;
- farming-related items;
- building-related items;
- future boat/progression items.

---

## Resource and Tool Progression

World resources can be harvested through the shared interaction/targeting systems.

Implemented progression covers resources such as trees and rocks and the tools needed to interact with them efficiently.

Tools are treated as gameplay items rather than UI-only concepts, which allows durability/progression behavior to remain in the gameplay layer.

---

## Farming

The farming system supports:

- tilling valid ground;
- planting seeds;
- crop growth stages;
- mature-only harvesting;
- crop drops and seed returns.

Farming is integrated with inventory, world state, save/load, and future food progression.

The first major crop is wheat, which connects farming with healing/food crafting.

---

## Building and Targeting

Placement and world interaction use dedicated targeting logic rather than embedding targeting rules directly inside rendering code.

Building/placement systems are designed to work with tile/world validation so an item can determine whether a target position is valid before modifying the world.

---

## Chest Storage

Chests provide persistent storage separate from the player inventory.

The storage system supports transferring items between the player inventory and chest contents while keeping item state part of the world/save data.

---

## Combat

Combat is real-time.

The player attacks toward the direction they are facing, using target/range rules instead of simply damaging every nearby entity.

The combat layer is separated through shared hittable/damage concepts so destructible objects and enemies can participate in damage handling without duplicating unrelated logic.

---

## Health, Healing, Death and Gold

The player uses a heart/HP system rather than a hunger system.

Food and consumables are used primarily for healing.

Enemies can drop gold, which is reserved for later economy/shop progression.

Death handling already exists as a gameplay flow and will be expanded later when the final Broken Hearts/death-economy design is implemented.

---

## Enemy Systems

Enemy behavior is separated from rendering and uses shared navigation/AI infrastructure.

Implemented enemy work includes:

- enemy spawning;
- chase/navigation behavior;
- attack logic;
- melee-oriented enemies;
- ranged/projectile-style behavior;
- special movement/behavior variants.

The project currently includes enemy concepts such as Shade, Wisp, and Wraith, with later balancing and final art still subject to polish.

---

## Day and Night

The world advances through a day/night cycle.

Current progression distinguishes:

- day;
- dusk warning;
- night.

The cycle affects enemy behavior/spawning and world presentation.

The later Crimson Veil event builds on top of this system rather than replacing it.

---

## Save and Load

Ashveil supports multiple independent save slots.

The save system persists gameplay state instead of only storing player position.

Saved state includes the world/player data required to reconstruct an active game, including character information and system-specific persistent state.

### Save Architecture

The save pipeline separates:

- save-file management;
- mapping runtime objects to save-data objects;
- mapping save-data back into runtime state;
- asynchronous file I/O.

Save files are read on a background executor.

`World` reconstruction is completed on the main/render thread because world creation can involve graphical or Tiled resources that should not be created on the save I/O worker thread.

### Application Flow

```text
Application Start
    ↓
LoadingScreen
    ↓
MainMenuScreen
    ↓
Save Slots
```

For a new game:

```text
Empty Slot
    ↓
CharacterCreationScreen
    ↓
Create World
    ↓
Set Character Name
    ↓
Initial Save
    ↓
GameScreen
```

For an existing save:

```text
Valid Slot
    ↓
LoadingScreen
    ↓
Asynchronous File Read
    ↓
World Reconstruction
    ↓
GameScreen
```

Invalid save slots can be deleted and reused.

---

## Character Creation

Character creation is intentionally minimal in the current build.

The player currently chooses a character name.

The name is persisted through the save system and displayed again in the save-slot UI.

Additional cosmetic customization may be added later without changing the current new-game architecture.

---

## Main Menu and Save Slots

The application now has a complete player-facing entry flow.

The main menu provides access to:

- Singleplayer;
- Settings/Options;
- Quit;
- Multiplayer placeholder for future work.

The save-slot screen distinguishes:

- empty slots;
- valid saves;
- invalid saves.

Valid slots can be loaded or deleted.

---

## Settings

The settings screen currently supports:

- Master Volume;
- Music Volume;
- SFX Volume;
- Fullscreen;
- Language;
- Key Bindings placeholder.

Settings are stored separately from game saves through LibGDX `Preferences`.

The volume values are already persisted but will be connected to the final audio system later.

Key-binding configuration is planned but not yet implemented.

### Fullscreen Behavior

The current Fullscreen option uses a borderless window instead of exclusive fullscreen.

On the tested Windows/LWJGL setup, an undecorated window that exactly matched the 120 Hz monitor resolution was presented at 60 Hz while focused.

The compatibility workaround is intentionally:

```java
Gdx.graphics.setWindowedMode(
        mode.width,
        mode.height + 1
);
```

The extra pixel prevents the window from entering the problematic fullscreen-like presentation path on that setup.

The desktop launcher currently uses an explicit 120 FPS foreground cap with VSync disabled.

---

## Localization

Localization infrastructure uses LibGDX `I18NBundle`.

Current resource files:

```text
assets/i18n/messages.properties
assets/i18n/messages_sr.properties
```

The current localized proof-of-concept covers the Main Menu and Settings screen.

Localization will be expanded incrementally: new player-facing text should receive a localization key when it is introduced instead of being converted in one large pass at the end of development.

The final font system will be introduced together with the in-game Ceca guidance/message UI so it can be tested against real dialogue-length text.

---

## Planned Ceca Guidance System

Ceca is the game's narrative guide and primary source of information for the player.

This is not intended to behave like a conventional step-by-step tutorial.

Instead, Ceca appears contextually and:

- introduces the player to the world;
- points toward important locations;
- explains mechanics when they become relevant;
- gives progression guidance;
- provides lore and interpretation;
- reacts to important events;
- gradually leads the player through the main game progression.

Messages are intended to appear as an in-game dialogue/guidance panel rather than a permanent objective tracker.

The player dismisses a message manually and then continues playing normally.

---

## Planned World Progression

The broader game progression is planned around:

1. establishing the player on the main island;
2. learning core survival/crafting/farming/combat systems naturally;
3. preparing for night encounters;
4. obtaining or constructing the means to travel;
5. reaching additional islands/areas;
6. collecting lore and progression artifacts;
7. discovering the truth about the curse;
8. triggering the endgame ritual/final night;
9. completing the ending and credits sequence.

The exact story order and Ceca's role in the final reveal are being refined before implementation.

---

## Planned Areas and Islands

The older design documents establish three additional island concepts as the main progression structure:

- Shadow Island;
- Wind Island;
- Blood-themed/endgame island.

Each area is intended to provide:

- a different environment;
- a different gameplay pressure;
- unique progression rewards;
- lore;
- one piece of the larger endgame progression.

The exact mechanics and rewards may continue to evolve as implementation reaches those checkpoints.

---

## Planned Boat and Travel

The player will eventually gain access to a boat/travel system used to leave the main island and reach other areas.

The travel system is intended to become part of progression rather than an immediately available mechanic.

Ceca may provide broad directional guidance, but exploration should still require the player to search and interpret the environment rather than follow a permanent map marker.

---

## Planned Lore and Scrolls

Lore scrolls are intended to reveal parts of the island's history and the curse.

They are separate from Ceca's normal guidance messages.

Scrolls are physical gameplay/lore items that the player reads manually.

The combined information from multiple areas will ultimately explain the final ritual and endgame progression.

---

## Planned Crimson Veil

Crimson Veil is the recurring high-danger night event planned for later development.

It builds on the existing day/night and enemy systems and is intended to introduce:

- stronger pressure;
- special visual presentation;
- increased enemy threat;
- later progression/reward hooks.

Dusk remains a warning phase rather than an enemy-spawn phase.

The final endgame night will be distinct from ordinary progression and will be implemented later.

---

## Planned Economy, Shop and Death Progression

Later progression will expand gold and death handling through systems such as:

- shop/economy;
- special purchases;
- cosmetics;
- rare items/seeds;
- Broken Hearts;
- death-related repair/recovery rules.

These systems are intentionally postponed until the core progression/area structure is in place.

---

## Planned Capling

Capling is the planned pet/companion.

The pet is intended to:

- be discovered in the world;
- follow the player;
- remain a lightweight companion rather than another major survival responsibility.

The system will be added later in progression development.

---

## Endgame and Credits

The planned final phase includes:

- completion of the major island progression;
- collection of required lore/artifacts;
- final ritual activation;
- final high-danger night;
- ending state;
- final/credits screen.

Credits are planned to include contributors and a dedicated Game Testers section.

---

## Pause Menu

The next immediate application-level feature is the in-game Pause Menu.

Planned initial actions:

- Resume;
- Settings;
- Back to Main Menu;
- Quit Game.

The game world should stop updating while paused.

The same Settings screen should be reusable from both the Main Menu and Pause Menu, returning to the correct previous context.

---

## Controls

| Control | Action |
|---|---|
| `W`, `A`, `S`, `D` | Move |
| `K` | Attack / contextual tool action |
| `E` | Interact / pick up / world interaction |
| `F` | Use consumable / contextual item action |
| `Q` | Drop one item |
| `Left Ctrl + Q` | Drop full stack |
| `Left Shift` | Dash |
| `1–9` | Select hotbar slot |
| `Tab` | Open or close crafting/inventory UI |

Controls may be adjusted when key rebinding is implemented.

---

## Technical Documentation

System documentation is stored in the `docs/` directory.

Current documentation includes:

- `inventory-system.md`
- `crafting-system.md`
- `farming-system.md`
- `resource-tool-progression.md`
- `building-and-targeting-system.md`
- `hittable-and-combat-system.md`
- `health-death-and-gold-system.md`
- `day-night-and-enemy-spawning-system.md`
- `enemy-ai-and-navigation-system.md`
- `tiled-map-system.md`
- `chest-storage-system.md`
- `save-load-system.md`
- menu/save-slot/settings documentation

New major systems should continue to receive their own focused technical document instead of turning the README into an implementation manual.

---

## Development Priorities

Immediate order:

1. Finish/verify CP14 menu, settings, localization, and documentation.
2. Implement the in-game Pause Menu.
3. Finalize the high-level story/progression flow.
4. Implement the Ceca guidance/progression system.
5. Introduce the real font/message UI system.
6. Continue area/boat/island progression.
7. Add economy/death progression and Capling.
8. Implement Crimson Veil and endgame systems.
9. Finish final art, animation, audio, particles, and UI polish.
10. Package the final build and complete submission/defense documentation.

Optional LAN multiplayer remains a later stretch goal and is not part of the current single-player priority.

---

## Project Goals

Ashveil is designed to demonstrate:

- object-oriented design;
- separation of responsibilities;
- reusable gameplay systems;
- real-time update/render architecture;
- AI/navigation;
- persistent state and asynchronous file I/O;
- data-driven gameplay;
- UI architecture;
- event/progression systems;
- maintainable expansion of a larger Java project.

The project is intentionally developed as a complete desktop game rather than as a server/mobile application.

---

## Gameplay

<p align="center">
  <img src="assets/screenshots/gameplay1.jpg" alt="Ashveil gameplay screenshot 1" width="48%">
  <img src="assets/screenshots/gameplay2.jpg" alt="Ashveil gameplay screenshot 2" width="48%">
</p>
