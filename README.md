# Ashveil

Ashveil is a 2D top-down survival game developed in Java using LibGDX.

## About

The project focuses on building reliable gameplay systems and a clean object-oriented architecture before introducing final assets, animations, audio, and visual polish.

Gameplay logic, rendering, input handling, world data, and UI are kept separated so that individual systems can be expanded without tightly coupling the entire project.

## Current State

Ashveil is currently in active development.

The core gameplay foundation is functional, while most visual elements are still represented by simple placeholder shapes. The project now uses Tiled maps for world rendering and static collision data.

Custom pixel art, animations, sound effects, multiple areas, and additional gameplay systems will be introduced gradually.

## Current Features

- Tiled map loading and rendering
- Collision defined through a dedicated Tiled collision layer
- Camera movement limited to map boundaries
- Player movement with diagonal movement normalization
- Configurable input-binding structure
- Simple enemy movement and combat
- Direction-based melee attacks
- Resource harvesting
- Procedural resource and enemy spawning
- Item drops and ground-item pickup
- Inventory and five-slot hotbar
- Single-item and full-stack dropping
- Basic crafting system
- Day and night cycle
- Basic health, hotbar, crafting, and day HUD
- Responsive HUD in windowed and fullscreen modes
- Basic death and game-over flow

## Architecture

The central `World` class owns the current game state and coordinates gameplay updates.

The project separates responsibilities between several systems:

- `World` manages entities, resources, items, crafting, and gameplay events.
- `TileMap` loads the Tiled map and provides map dimensions and collision information.
- `WorldRenderer` renders the map, entities, resources, and ground items.
- `HudRenderer` renders interface elements independently from the game world.
- `CameraController` follows the player while respecting map boundaries.
- `PlayerInput` represents gameplay input without directly coupling entities to LibGDX controls.
- `KeyBindings` stores the current control configuration and prepares the project for future user-defined controls.

## Controls

| Control | Action |
|---|---|
| `W`, `A`, `S`, `D` | Move |
| `K` | Primary action / attack / harvest |
| `E` | Interact / pick up |
| `F` | Use item |
| `Q` | Drop one item from the selected hotbar slot |
| `Left Ctrl + Q` | Drop the entire selected stack |
| `Left Shift` | Dash |
| `1–5` | Select hotbar slot |
| `Tab` | Open or close the crafting overlay |

## Project Structure

The project is divided into focused packages for:

- application and screens
- entities
- world systems
- input handling
- rendering
- items and inventory
- crafting
- resources and world objects

This structure is intended to keep gameplay logic independent from rendering and platform-specific input code.

## Screenshot

Gameplay screenshot will be added as the visual side of the project develops.

## Development Goals

Planned systems include:

- improved combat and enemy behavior
- tools and durability
- expanded crafting
- farming and building
- save and load support
- settings and control rebinding
- multiple islands and map transitions
- tutorial progression
- shops and progression systems
- custom pixel art, animations, audio, and visual effects

## Purpose

This project is primarily used to practice:

- object-oriented game architecture
- separation of responsibilities
- real-time update loops
- input abstraction
- collision systems
- map integration
- entity and resource management
- incremental development of larger gameplay systems
