# Menu, Save Slots and Settings System

## Overview

This document describes the application-level flow introduced after the core save/load system: the main menu, save-slot selection, minimal character creation, settings persistence, display configuration, and initial localization support.

These systems sit above the gameplay world and control how the player enters, resumes, or configures the game.

---

## Application Flow

Ashveil now starts through a loading screen and then opens the main menu.

```text
Application start
    ↓
LoadingScreen
    ↓
MainMenuScreen
```

Selecting Singleplayer opens the save-slot screen:

```text
MainMenuScreen
    ↓
SaveSlotScreen
```

The selected slot determines the next action.

### Empty slot

```text
EMPTY SLOT
    ↓
CharacterCreationScreen
    ↓
Create World
    ↓
Set character name
    ↓
Initial save request
    ↓
GameScreen
```

### Valid slot

```text
VALID SLOT
    ↓
LoadingScreen
    ↓
Asynchronous save-file read
    ↓
SaveData
    ↓
World reconstruction on the main thread
    ↓
GameScreen
```

### Invalid slot

Invalid saves can be deleted so that the slot can be reused.

---

## Save Slot States

A save slot can currently be:

- empty;
- valid;
- invalid.

An empty slot starts the new-game flow.

A valid slot displays saved metadata and allows the player to load or delete the save.

An invalid slot cannot be loaded and can instead be deleted.

The save-slot UI uses the character name stored in the save data when displaying a valid save.

---

## Character Creation

Character creation is currently intentionally minimal.

The player enters a character name before starting a new game. The same newly created `World` instance is then passed into `GameScreen`, while an initial save request is also issued for the selected slot.

The character name is persisted through the save system:

```text
Player.characterName
    ↓
PlayerSaveData.characterName
    ↓
JSON save file
    ↓
SaveSlotInfo.characterName
```

Additional character customization can be added later without changing the current new-game flow.

---

## Asynchronous Save Loading

Existing saves are read asynchronously so that file I/O does not block the render thread.

The flow is:

```text
SaveService.requestLoad(slot)
    ↓
background JSON/file read
    ↓
Future<SaveData>
    ↓
LoadingScreen waits for completion
    ↓
SaveService.completeLoad(...)
    ↓
World reconstruction
```

The actual `World` reconstruction is completed on the main/render thread because creating the world can involve graphical and Tiled-map resources.

---

## Settings

The settings screen currently contains:

- Master Volume;
- Music Volume;
- SFX Volume;
- Fullscreen;
- Language;
- Key Bindings placeholder.

Settings are represented by `GameSettings` and managed through `SettingsService`.

Persistent settings are stored using libGDX `Preferences`.

`SettingsService` is responsible for:

1. loading stored settings;
2. exposing the current settings;
3. writing changed settings to preferences;
4. applying display-related settings.

The three volume values are already persisted, but they are not yet connected to the final audio system.

The Key Bindings configuration screen is postponed.

---

## Display Configuration

Ashveil currently uses a borderless window for the Fullscreen option instead of exclusive fullscreen.

When fullscreen is enabled, the game removes window decoration and resizes the window to the current monitor resolution.

```java
Gdx.graphics.setUndecorated(true);
```

### Windows / LWJGL refresh-rate workaround

During testing on Windows with a 120 Hz external monitor and a 60 Hz secondary display, an undecorated window whose dimensions exactly matched the monitor resolution was presented at 60 Hz while the game window had focus.

For example:

```text
1920 x 1080 -> focused window behaved as 60 Hz
```

The issue disappeared when the borderless window was made one pixel taller:

```text
1920 x 1081 -> correct 120 Hz behavior
```

For this reason the current fullscreen implementation intentionally uses:

```java
Gdx.graphics.setWindowedMode(
        mode.width,
        mode.height + 1
);
```

The extra pixel is a compatibility workaround and should not be removed without retesting fullscreen behavior.

The desktop launcher currently uses an explicit 120 FPS foreground limit with VSync disabled because VSync caused the focused game window to synchronize at 60 FPS on the tested setup.

---

## Localization

Initial localization support is implemented using libGDX `I18NBundle`.

The resource files are:

```text
assets/i18n/messages.properties
assets/i18n/messages_sr.properties
```

The default bundle contains English strings and the `_sr` bundle contains Serbian strings.

`LocalizationService` owns the active bundle and exposes localized strings by key:

```java
i18n.get("menu.singleplayer");
```

The selected language is persisted through `GameSettings`.

Localization is being introduced incrementally instead of converting every existing screen at once.

The current proof-of-concept scope covers:

- Main Menu;
- Settings.

Future user-facing text should receive localization keys as it is introduced. Expected key groups include:

```text
menu.*
settings.*
tutorial.*
item.*
interaction.*
```

Two resource files are sufficient for the current project size. They can be split into multiple bundles later only if they become difficult to maintain.

---

## Font Support

The final font system is intentionally postponed until tutorial/message UI work begins.

This allows fonts to be tested against real tutorial and dialogue content rather than placeholder text.

If Serbian localization is used, the selected font system must support Serbian Latin glyphs:

```text
č ć š ž đ
Č Ć Š Ž Đ
```

Final font visual polish can still be revisited during the later UI/polish phase.

---

## Current Limitations

The following parts are intentionally postponed:

- applying persisted volume values to the final audio system;
- the Key Bindings configuration screen;
- localization of every existing screen;
- the final font system;
- additional character customization;
- final UI polish.

These limitations do not block the current application flow.

---

## Regression Test Checklist

### Startup and menu

- application opens through `LoadingScreen`;
- Main Menu appears correctly;
- Singleplayer opens Save Slots;
- Settings opens correctly;
- Quit exits the application.

### New game

- selecting an empty slot opens Character Creation;
- an empty character name is rejected;
- a valid character name starts a new game;
- an initial save is requested;
- the character name is stored correctly.

### Existing saves

- valid slots display the stored character name;
- valid saves can be loaded;
- loading uses `LoadingScreen`;
- save-file reading happens asynchronously;
- the reconstructed world starts correctly;
- saves can be deleted;
- invalid saves can be removed.

### Settings

- Master Volume persists after restart;
- Music Volume persists after restart;
- SFX Volume persists after restart;
- Fullscreen persists after restart;
- Language persists after restart;
- fullscreen can be enabled and disabled;
- fullscreen remains smooth at the intended refresh rate;
- Apply stores changes;
- Back returns to Main Menu;
- ESC from Settings returns to Main Menu.

### Localization

- English can be selected;
- Serbian can be selected;
- changing the language updates the localized screens;
- the selected language remains after restart;
- Main Menu uses localization keys;
- Settings uses localization keys.

---

## Result

The application now has a complete path from startup to either creating a new playable world or loading an existing one.

Save-slot data, application settings, and localization preferences are kept separate from gameplay state, while the main menu and settings screens provide the player-facing entry point for the existing game systems.
