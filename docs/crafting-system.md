# Crafting System

## Overview

The crafting system is data-driven and uses recipes loaded from a CSV resource. Gameplay crafting logic is separated from the Scene2D user interface so that the UI can display recipes and request crafting without directly modifying the player's inventory.

The current implementation supports:

- recipe loading from CSV;
- recipe validation;
- stable recipe IDs;
- recipe categories;
- locked and unlocked crafting categories;
- ingredient validation;
- successful and unsuccessful crafting results;
- inventory overflow dropped into the world;
- a functional Scene2D crafting menu;
- automatic UI refresh after crafting and when the menu is reopened.

## Recipe Data

Each recipe contains the following information:

- unique recipe ID;
- crafting category;
- result item type;
- result amount;
- required ingredients and their quantities.

Ingredients are stored as:

```java
Map<ItemType, Integer>
```

The key represents the required item type, while the value represents the required quantity.

Example logical recipe:

```text
ID: wooden_axe
Category: TOOLS
Result: WOODEN_AXE x1
Ingredients:
- WOOD x5
```

The exact CSV column layout is defined by the current `RecipeBook` parser. Any change to the CSV structure must be reflected in that parser and its validation rules.

## Main Classes

### `Recipe`

Represents one crafting recipe.

Responsibilities:

- stores the recipe ID;
- stores the category;
- stores the result item and amount;
- stores ingredient requirements;
- validates recipe data.

### `RecipeBook`

Loads all recipes from the CSV resource and converts them into `Recipe` objects.

Responsibilities:

- reads recipe data;
- parses item types, categories, quantities and IDs;
- validates malformed or incomplete data;
- exposes the loaded recipe collection.

### `CraftingManager`

Contains the core crafting rules.

Responsibilities:

- stores all loaded recipes;
- tracks unlocked crafting categories;
- finds recipes by ID;
- checks whether a category is unlocked;
- checks whether the inventory contains all required ingredients;
- removes ingredients after a successful validation;
- adds the crafted result to the inventory;
- returns a `CraftingResult` containing the status and overflow amount.

The manager does not create world drops directly. World-specific behavior remains in `World`.

### `CraftStatus`

Represents the result of validating a crafting request.

Current statuses include:

- `SUCCESS`;
- `MISSING_INGREDIENTS`;
- `CATEGORY_LOCKED`.

### `CraftingResult`

Represents the result of a crafting attempt.

It contains enough information for the caller to determine:

- whether crafting succeeded;
- which item was produced;
- how many items were produced;
- how many items did not fit in the inventory;
- why crafting failed when it was unsuccessful.

### `CraftingAccess`

Defines the limited crafting operations exposed to the UI.

```java
public interface CraftingAccess {
    CraftStatus getCraftStatus(String recipeId);
    CraftingResult tryCraft(String recipeId);
    int getOwnedQuantity(ItemType itemType);
}
```

`World` implements this interface. The UI therefore receives only the crafting operations it needs instead of receiving direct access to the entire world, inventory or crafting manager.

## Crafting Flow

The complete request path is:

```text
CraftingPanel
    -> CraftingAccess
        -> World
            -> CraftingManager
                -> Inventory
```

Detailed flow:

1. The player selects a recipe in `CraftingPanel`.
2. The panel requests the current `CraftStatus` through `CraftingAccess`.
3. The Craft button is enabled only when the status is `SUCCESS`.
4. Clicking Craft calls `CraftingAccess.tryCraft(recipeId)`.
5. `World` resolves the recipe by ID.
6. `World` delegates ingredient validation and inventory modification to `CraftingManager`.
7. `CraftingManager` returns a `CraftingResult`.
8. `World` handles any overflow by creating a `WorldItem` near the player.
9. The UI refreshes the selected recipe details and current ingredient quantities.

## Category Progression

`CraftingManager` stores the set of unlocked categories.

A recipe is craftable only when its category is unlocked. The world exposes the recipes from currently unlocked categories through:

```java
public List<Recipe> getAvailableRecipes()
```

The UI does not contain hardcoded checks for specific categories such as `ANCIENT`. It displays the recipes it receives from the world.

This keeps progression rules outside the UI and allows special categories to use a different presentation later.

## Inventory Overflow

Inventory space is not a requirement for starting a craft.

When the crafted result does not completely fit in the inventory:

1. the ingredients are still consumed;
2. the amount that fits is added to the inventory;
3. `CraftingResult` reports the remaining overflow amount;
4. `World.tryCraft()` creates a `WorldItem` near the player for that overflow.

For this reason, UI code must always request crafting through `World.tryCraft()` via `CraftingAccess`. Calling `CraftingManager.craft()` directly from the UI would bypass world-drop handling.

## Crafting Menu

The crafting menu uses LibGDX Scene2D.

### Left panel

The left panel contains:

- category names as ordinary labels;
- recipes grouped under their categories;
- clickable recipe entries;
- a `ScrollPane` for longer lists.

Recipe entries remain selectable even when the player lacks ingredients. This allows the player to inspect the recipe requirements.

The current entries are text-based. They are intended to be replaced later by reusable recipe list items containing:

- item icon;
- item name;
- hover state;
- unavailable visual state;
- selected state.

### Details panel

The right panel currently displays:

- result item name;
- required ingredients;
- owned and required quantities;
- produced amount;
- Craft button;
- crafting result message.

Example:

```text
Wooden Axe

Required:
Wood: 5 / 5

Produces: Wooden Axe x1

CRAFT
```

The Craft button is disabled when `CraftStatus` is not `SUCCESS`.

### Refresh behavior

The details panel refreshes when:

- a recipe is selected;
- crafting is attempted;
- the Crafting tab is opened;
- the game menu is reopened while the Crafting tab is selected.

This ensures that ingredient quantities and the Craft button state match the current inventory.

## Scene2D Menu Integration

The game menu contains three main tabs:

- Inventory;
- Crafting;
- Shop.

`GameMenuUi` owns the Scene2D `Stage`, tab buttons and panel container.

When the menu opens:

- the world update is paused;
- the Stage becomes the active input processor;
- the selected panel is refreshed;
- the menu is drawn after the world and HUD.

When the menu closes:

- the Stage is removed as the input processor;
- gameplay updates continue.

`HudRenderer` is no longer responsible for drawing or handling the crafting menu. It only renders the permanent gameplay HUD.

## Ownership and Responsibilities

The current separation is:

```text
RecipeBook
- loads recipe data

CraftingManager
- validates and performs inventory-level crafting

World
- exposes crafting to gameplay and handles overflow drops

CraftingAccess
- limits what the UI can request

CraftingPanel
- displays recipes and sends crafting requests
```

This prevents the UI from directly mutating gameplay state and keeps one authoritative crafting path.

## Current Limitations and Planned UI Work

The current interface is functional but temporary.

Planned improvements include:

- item icons in the recipe list;
- larger result icon in the details panel;
- ingredient icons;
- custom button and panel textures;
- clearer unavailable and selected states;
- item descriptions;
- final fonts and spacing;
- special presentation for Ancient recipes when their progression is implemented.

These changes should affect presentation only. The existing recipe, crafting and overflow logic should remain unchanged.

## Validation Status

The backend crafting flow has covered the following scenarios during development:

- successful crafting;
- missing ingredients;
- locked category;
- inventory overflow.

The Scene2D menu has also been manually checked for:

- opening and closing;
- tab switching;
- recipe selection;
- disabled Craft button;
- ingredient quantity refresh;
- menu refresh after reopening.

A full final regression pass should still be performed before release or before major changes to inventory capacity, recipe data or category progression.
