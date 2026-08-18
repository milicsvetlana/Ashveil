# Chest Storage System

## Overview

Chest is a placeable and destructible world object that provides additional persistent item storage.

Each placed Chest owns a separate 15-slot `Inventory`. There is no shared global Chest inventory, so an unlimited number of Chest objects can exist independently in the world.

The system reuses the existing inventory backend instead of implementing separate storage rules.

---

## Chest Object

`Chest` extends `DestructibleObject` and owns its own `Inventory`.

```text
Chest
├── DestructibleObject behaviour
│   ├── position
│   ├── HP
│   ├── collision
│   └── destruction
└── Inventory
    └── 15 slots
```

Chest is created through the same world-object creation path used by other placeable destructible objects.

The `CHEST` item uses placement targeting and creates a `Chest` object after successful placement validation.

---

## Interaction

The player interacts with a nearby Chest using `E`.

`World` searches for the nearest valid Chest inside the interaction range before checking ground-item pickup.

When a Chest is found, `World` stores it as the currently active Chest.

`GameScreen` detects this state and opens the Chest overlay.

The Chest can be closed with:

- `E`
- `ESC`

Opening the Chest cancels active placement targeting.

---

## Overlay Architecture

Gameplay overlays are represented by `GameOverlay`.

Current relevant states are:

```text
NONE
MENU
CHEST
SCROLL
```

Only one gameplay overlay is active at a time.

While `CHEST` is active:

- normal world update is paused;
- Chest UI receives keyboard input;
- the shared overlay `Stage` is updated and rendered;
- world interaction and combat input are not processed.

`GameScreen` owns the overlay lifecycle, while `ChestUI` only represents the Chest interface and inventory interaction logic.

---

## Chest UI

The Chest interface displays Player storage and Chest storage at the same time.

Player inventory is divided into:

```text
Main inventory: slots 5-19
Hotbar:         slots 0-4
```

Chest contains:

```text
Chest inventory: slots 0-14
```

The UI reuses `InventoryGridUi` for all three grids.

This prevents Chest storage from duplicating slot rendering and drag-and-drop behaviour already used by the normal inventory screen.

---

## Mouse Item Transfer

All grids share the same LibGDX `DragAndDrop` instance.

Drag payload stores:

- source `Inventory`;
- source slot index.

The destination grid provides:

- destination `Inventory`;
- destination slot index.

Transfer is therefore handled through the same inventory operation:

```text
sourceInventory.moveSlot(
    sourceIndex,
    destinationInventory,
    destinationIndex
)
```

The existing inventory backend determines whether the operation becomes:

- move;
- swap;
- stack merge.

Split-stack behaviour is also shared with the normal inventory UI.

Items remain in the source inventory until a successful drop occurs.

---

## Keyboard Item Transfer

Keyboard navigation works across Player and Chest inventories.

Navigation supports the existing movement/menu keys.

`K`, the game's primary-action key, is used to select and place an item.

The keyboard state stores both the inventory reference and slot index:

```text
selectedInventory
selectedSlotIndex

keyboardMoveSourceInventory
keyboardMoveSourceIndex
```

The inventory reference is required because Player slot `3` and Chest slot `3` are different locations.

First `K` press:

```text
selected slot
    ↓
stored as keyboard move source
```

Second `K` press:

```text
source inventory + source slot
            ↓
Inventory.moveSlot(...)
            ↓
selected inventory + selected slot
```

Pressing `K` again on the original source slot cancels the move.

Keyboard and mouse transfers therefore use the same inventory backend and cannot develop separate move/swap/merge rules.

---

## ItemStack State Preservation

Non-stackable items may contain per-instance state, currently durability.

For example:

```text
AXE 63/100
```

must remain `63/100` when moved through:

```text
Inventory
→ Ground
→ Inventory
```

or:

```text
Inventory
→ Chest
→ destroyed Chest
→ Ground
→ Inventory
```

For this reason, world transfer does not reconstruct non-stackable items only from `ItemType`.

`WorldItem` can store an existing `ItemStack`.

`Inventory.extractFromSlot(...)` removes a stack from an inventory while preserving its state.

`Inventory.addStack(...)` inserts an existing stack into an inventory.

Cross-inventory `moveSlot(...)` also transfers the existing `ItemStack`.

This ensures that durability is preserved throughout the complete item lifecycle.

---

## Chest Destruction

Chest participates in the normal destructible-object damage system.

When destroyed:

1. the Chest item itself is dropped;
2. every non-empty Chest inventory slot is converted into a `WorldItem`;
3. the existing `ItemStack` is passed to the world item;
4. the Chest collider and world object are removed.

Because the concrete `ItemStack` is preserved, tools dropped from a destroyed Chest keep their current durability.

Example:

```text
AXE 63/100
    ↓
stored in Chest
    ↓
Chest destroyed
    ↓
AXE WorldItem 63/100
    ↓
picked up
    ↓
AXE 63/100
```

---

## Responsibilities

### `Chest`

- owns its 15-slot inventory;
- behaves as a destructible world object.

### `Inventory`

- owns item stacks;
- performs move, swap, merge and split operations;
- supports transfers between different inventories;
- preserves concrete `ItemStack` state.

### `InventoryGridUi`

- renders an arbitrary inventory slot range;
- provides reusable drag-and-drop sources and targets;
- does not own gameplay rules.

### `ChestUI`

- combines Player and Chest inventory grids;
- manages keyboard selection state;
- forwards transfers to `Inventory`.

### `World`

- creates and destroys Chest objects;
- finds the nearest Chest during interaction;
- stores the currently active Chest;
- drops Chest contents into the world.

### `GameScreen`

- opens and closes the Chest overlay;
- routes keyboard input;
- pauses normal gameplay while Chest is open.

---

## Important Rules

- Every Chest owns a separate inventory.
- Chest capacity is 15 slots.
- Chest storage uses normal Inventory rules.
- Gold remains in `Wallet` and is not stored as a normal inventory item.
- WorldItem transfers preserve item-instance state.
- Destroying a Chest never deletes its stored items.
- Mouse and keyboard transfers use the same inventory backend.
- `K` is the primary item action in Inventory and Chest interfaces.
- `E` interacts with and closes a Chest.
- `ESC` closes the active Chest overlay.
