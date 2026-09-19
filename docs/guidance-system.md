# Guidance System

## Overview

Ashveil uses an event-driven guidance system to introduce the player to the main gameplay loop and to provide occasional contextual hints.

The guidance is presented through Ceca, who currently acts as a UI and narrative guide rather than as a physical world NPC.

The system is intentionally separate from direct keyboard input. Gameplay systems report meaningful events such as movement, planting, collecting resources, opening crafting, or taking damage. The guidance system reacts to those events instead of checking specific physical keys.

This allows the input bindings to change later without requiring the tutorial logic to be rewritten.

The guidance system currently supports two types of messages:

- ordered main guidance steps;
- contextual messages that may appear when a specific gameplay situation occurs.

The guidance state is persistent and is stored as part of the save data.

---

## Main Guidance Flow

The current introductory sequence is:

```text
MOVEMENT
    ↓
STARTER_CHEST
    ↓
PLANTING
    ↓
COLLECT_WOOD
    ↓
OPEN_CRAFTING
    ↓
CRAFT_EQUIPMENT
    ↓
DUSK_WARNING
```

The sequence introduces the player to the basic actions required before the first night.

The player is not required to complete a specific crafting recipe. Any successful craft satisfies the crafting step.

After the first successful craft, the ordered guidance pauses until dusk begins naturally.

The tutorial does not control the day/night cycle and does not force the first night to start.

---

## GuideStep

`GuideStep` represents a guidance message or guidance state.

Each main guidance step contains:

```java
messageKey
trigger
```

The `messageKey` references a localized text from the i18n resource bundle.

Example:

```text
guidance.movement
guidance.starterChest
guidance.planting
guidance.collectWood
```

The trigger describes the gameplay event required by that step.

Example:

```java
MOVEMENT(
    "guidance.movement",
    new GuideTrigger(GameEvent.PLAYER_MOVED)
)
```

The guidance logic therefore does not contain the final displayed text directly.

---

## GameEvent

`GameEvent` describes meaningful gameplay actions that may be relevant to guidance.

Current events include:

```text
PLAYER_MOVED
STARTER_CHEST_OPENED
CHEST_CLOSED
SEED_PLANTED
WOOD_COLLECTED
CRAFTING_OPENED
ITEM_CRAFTED
PLAYER_DAMAGED
DUSK_STARTED
WHEAT_HARVESTED
```

Not every declared event must currently participate in the main sequence.

Events describe gameplay meaning rather than physical input.

For example:

```text
SEED_PLANTED
```

is used instead of:

```text
F_PRESSED
```

This is important because planting may later be triggered by a different input method, controller binding, or another player.

---

## GuideTrigger

`GuideTrigger` currently represents a simple event requirement.

It stores the expected `GameEvent` and checks whether an incoming event matches it.

Conceptually:

```text
expected event = SEED_PLANTED

incoming event = SEED_PLANTED
                ↓
              match
```

A trigger does not know anything about UI, keyboard bindings, world rendering, or localization.

This keeps the trigger model focused on gameplay state.

More complex triggers can later be introduced if a future guidance step requires additional conditions.

---

## GuidanceSystem

`GuidanceSystem` owns the persistent state of the guidance flow.

It tracks:

```text
currentStepIndex
triggerSatisfied
messageAcknowledged
shownContextualSteps
activeContextualStep
```

The system does not render Ceca or read player input directly.

Its responsibility is to decide:

```text
which main guidance step is active
whether its gameplay condition has been completed
whether its message has been acknowledged
which contextual messages were already completed
whether a contextual message is currently active
```

---

## Main Step Progression

A main guidance step has two independent requirements:

```text
triggerSatisfied
messageAcknowledged
```

`triggerSatisfied` means that the required gameplay action occurred.

`messageAcknowledged` means that the player confirmed Ceca's current main message with Enter.

The guidance advances only when both values are true.

Conceptually:

```text
gameplay requirement complete
        +
message acknowledged
        ↓
advance to next main step
```

This allows the two actions to happen in either order.

For example, if the current step is movement:

```text
currentStep = MOVEMENT
triggerSatisfied = false
messageAcknowledged = false
```

If the player acknowledges the message first:

```text
triggerSatisfied = false
messageAcknowledged = true
```

The step does not advance yet.

After the player moves:

```text
triggerSatisfied = true
messageAcknowledged = true
```

The step advances.

When the next step begins, both flags are reset to `false`.

---

## Delayed Guidance Messages

`GameScreen` can delay the display of the next guidance message.

This prevents several Ceca messages from appearing immediately one after another after a gameplay interaction.

The current system uses:

```text
guidanceMessagePending
guidanceMessageDelay
```

The delay is UI/runtime state and is not part of the persistent tutorial progression.

Different transitions may use different delays.

For example, the planting message does not need to appear at exactly the same moment that the starter chest is closed.

---

## Movement Guidance

The movement step begins when a new game starts.

`GameScreen` records the player's position before the world update and compares it with the position afterwards.

Actual movement triggers:

```java
GameEvent.PLAYER_MOVED
```

Simply pressing a movement key without successfully moving the player is not enough.

---

## Starter Chest Guidance

The starter chest contains the initial items needed for the early gameplay loop.

After the chest interaction is completed and the chest is closed, `GameScreen` reports:

```java
GameEvent.CHEST_CLOSED
```

This completes the gameplay requirement for the starter chest step.

---

## Planting Guidance

Planting guidance reacts to successful gameplay state changes rather than to button presses.

Before performing a target action, `GameScreen` checks whether the target tile already contains a plant.

After the action, it verifies that a crop was actually created.

Only a successful planting action reports:

```java
GameEvent.SEED_PLANTED
```

Invalid placement therefore does not advance the tutorial.

---

## Wood Collection Guidance

After planting, the player is instructed to gather wood.

The guidance does not advance after the first Wood pickup.

A minimum amount is configured through:

```java
Config.GUIDANCE_WOOD_TARGET
```

The current target is:

```text
5 Wood
```

`GameScreen` compares the player's Wood quantity before and after the world update.

The step is completed when the quantity increases and reaches the configured threshold.

The reported event is:

```java
GameEvent.WOOD_COLLECTED
```

---

## Crafting Guidance

Crafting guidance has two main parts:

```text
OPEN_CRAFTING
CRAFT_EQUIPMENT
```

### Opening Crafting

Opening the crafting tab reports:

```java
GameEvent.CRAFTING_OPENED
```

The event is produced by the UI itself rather than by checking only keyboard navigation in `GameScreen`.

This is necessary because the player may open the Crafting tab using either:

```text
keyboard navigation
mouse input
```

The callback therefore reacts to the actual selected tab instead of assuming how the player reached it.

The system also handles the case where the player opens Crafting faster than the delayed guidance message can appear.

If the instruction to open Crafting has become obsolete because the player already opened it, the system can advance to the crafting step instead of displaying an unnecessary instruction.

### Successful Craft

`CraftingPanel` reports successful crafting through a callback.

Only a successful crafting result triggers:

```java
GameEvent.ITEM_CRAFTED
```

A failed craft does not complete the step.

The tutorial does not require a specific recipe.

Any valid successful craft completes the introductory crafting requirement.

---

## Free Time Before Dusk

After the player successfully crafts an item, the main guidance does not immediately display another message.

The player receives free gameplay time until dusk begins naturally.

`DUSK_WARNING` may already be the current internal main step, but its message must not be shown before the actual dusk transition occurs.

For this reason the step waits for:

```java
GameEvent.DUSK_STARTED
```

before its message becomes eligible for display.

---

## Dusk Guidance

`DayNightCycle` exposes a transient flag:

```java
justBecameDusk()
```

The flag becomes true only during the update in which the phase changes:

```text
DAY
 ↓
DUSK
```

`GameScreen` converts that transition into:

```java
GameEvent.DUSK_STARTED
```

The Dusk message is therefore synchronized with the real world phase rather than with tutorial timing.

If another overlay is open when dusk begins, the guidance message remains pending and is shown after the overlay is closed.

The Dusk guidance does not start the night itself.

The existing day/night system continues normally:

```text
DAY
↓
DUSK
↓
NIGHT
```

---

## Contextual Guidance

Not every Ceca message belongs to the ordered main tutorial.

Some messages are situational and may occur independently from the current main guidance step.

These are called contextual guidance messages.

The first implemented contextual message is:

```text
HEALING
```

It may occur whenever the player first receives non-lethal damage.

A contextual message must not advance or modify the current main tutorial step.

For example:

```text
current main step = COLLECT_WOOD

player takes damage
        ↓
HEALING message appears
        ↓
player acknowledges HEALING
        ↓
current main step is still COLLECT_WOOD
```

---

## Contextual Guidance State

Contextual guidance uses two separate states:

```text
activeContextualStep
shownContextualSteps
```

`activeContextualStep` represents a contextual message that has been triggered but has not yet been acknowledged.

Example:

```text
activeContextualStep = HEALING
```

`shownContextualSteps` contains contextual messages that the player has already acknowledged and completed.

Example:

```text
shownContextualSteps = [HEALING]
```

`EnumSet<GuideStep>` is used internally because all stored values are `GuideStep` enum values and duplicates are not useful.

A contextual step can be activated only if:

```text
it has not already been completed
and
another contextual step is not already active
```

---

## Healing Guidance

`GameScreen` records the player's HP before the world update:

```java
healthBeforeUpdate
```

After the update it reads:

```java
healthAfterUpdate
```

A non-lethal decrease:

```text
healthAfterUpdate < healthBeforeUpdate
and
healthAfterUpdate > 0
```

attempts to activate:

```java
GuideStep.HEALING
```

Increasing HP does not trigger the message.

This prevents healing itself from being interpreted as damage.

Once activated, the message remains part of the persistent guidance state until the player acknowledges it.

Acknowledging the contextual message:

```text
moves HEALING into shownContextualSteps
clears activeContextualStep
```

The message therefore cannot appear repeatedly after it has been completed.

---

## Guidance UI

`GuidanceUi` is responsible only for presentation.

It displays:

```text
Ceca portrait
speaker name
localized message
optional control hint
```

The UI does not decide which tutorial step is active.

The persistent state belongs to `GuidanceSystem`.

`GameScreen` connects the two systems:

```text
gameplay event
        ↓
GuidanceSystem
        ↓
current guidance state
        ↓
GameScreen
        ↓
GuidanceUi
```

This keeps progression logic separate from Scene2D presentation.

---

## Guidance Input

Enter is currently used to acknowledge a visible Ceca message.

Main and contextual messages are handled differently.

For a main message:

```text
Enter
 ↓
acknowledgeCurrentMessage()
```

For a contextual message:

```text
Enter
 ↓
acknowledgeActiveContextualStep()
```

This prevents a contextual message such as HEALING from accidentally acknowledging or advancing the current main tutorial step.

`contextualGuidanceVisible` is runtime UI state that tells `GameScreen` which type of visible message is currently being acknowledged.

It is not the persistent record of contextual progression.

---

## Localization

Guidance text is stored in the localization resource bundle instead of being hardcoded in `GuidanceSystem`.

Example keys include:

```text
guidance.movement
guidance.starterChest
guidance.planting
guidance.collectWood
guidance.openCrafting
guidance.craftEquipment
guidance.healing
guidance.dusk
```

`GuideStep` stores the localization key.

`GameScreen` obtains the final displayed text through `LocalizationService`.

This allows Serbian and additional languages to be added without changing the guidance progression logic.

---

## Persistence

Guidance state is stored as part of the normal game save.

`World` owns the persistent `GuidanceSystem`.

`GameScreen` receives it through:

```java
world.getGuidanceSystem()
```

This allows the existing save flow:

```text
World
 ↓
SaveMapper
 ↓
SaveData
 ↓
JSON
```

to include guidance without creating a separate save mechanism.

---

## GuidanceSaveData

`GuidanceSaveData` stores the serializable representation of the system.

The saved data includes:

```text
currentStep
triggerSatisfied
messageAcknowledged
activeContextualStep
shownContextualSteps
```

Example:

```json
{
  "currentStep": "COLLECT_WOOD",
  "triggerSatisfied": false,
  "messageAcknowledged": true,
  "activeContextualStep": "HEALING",
  "shownContextualSteps": []
}
```

This state means:

```text
the player is currently on COLLECT_WOOD
the Wood requirement is not yet complete
the COLLECT_WOOD message was already acknowledged
the HEALING contextual message is active but not yet acknowledged
```

---

## Saving Enum Values

`GuideStep` values are stored using their enum names.

For example:

```java
GuideStep.COLLECT_WOOD.name()
```

produces:

```text
COLLECT_WOOD
```

The JSON therefore stores:

```json
"currentStep": "COLLECT_WOOD"
```

instead of storing the numeric position of the step.

A numeric index would be fragile because inserting another main step into the sequence could change the meaning of existing saved indices.

During loading:

```java
GuideStep.valueOf("COLLECT_WOOD")
```

reconstructs:

```java
GuideStep.COLLECT_WOOD
```

---

## Contextual Step Persistence

At runtime, completed contextual steps are stored as:

```java
EnumSet<GuideStep>
```

The save DTO stores simple string values.

Example runtime state:

```text
[HEALING]
```

is converted by `SaveMapper` into:

```json
[
  "HEALING"
]
```

During loading the strings are converted back into `GuideStep` values and added to a new `EnumSet`.

This keeps the gameplay model type-safe while keeping the save representation simple.

---

## Restoring Guidance State

`GuidanceSystem.applyPersistentState(...)` restores:

```text
current main step
main trigger state
main acknowledgement state
completed contextual steps
active contextual step
```

If `currentStep` is `null`, the main introductory sequence is considered complete.

The restored main step is resolved through the main sequence instead of directly restoring a raw numeric index.

This allows the runtime system to calculate its internal `currentStepIndex` from the saved `GuideStep`.

---

## Save Compatibility

Guidance data is treated as an additional part of the save rather than as a requirement for reconstructing the entire world.

Older saves may not contain guidance data.

If guidance data is missing, the guidance system may remain in its default state while the rest of the saved world is still restored.

Unknown or obsolete contextual step names are ignored during contextual restoration.

This prevents a minor guidance incompatibility from unnecessarily making the entire game save unusable.

---

## Day/Night Persistent State

The day/night cycle contains transient transition flags:

```text
justBecameDay
justBecameDusk
justBecameNight
```

These flags describe an event that occurred during the current update.

They are not persistent world state.

When `DayNightCycle.applyPersistentState(...)` restores a saved phase, all three transition flags are reset to:

```text
false
```

Loading an existing Dusk phase therefore does not incorrectly report that the game has just transitioned into Dusk.

---

## Scene2D Lifecycle

The guidance UI uses its own:

```java
Stage guidanceStage
```

with a `ScreenViewport`.

The stage viewport is updated from `GameScreen.resize(...)`.

The stage is disposed when `GameScreen` is disposed.

The global application `Skin` is borrowed by the guidance UI and is not disposed by `GuidanceUi` or `GameScreen`.

Ownership of the shared Skin remains with the application.

---

## Current Architecture

```text
Gameplay systems
    produce meaningful actions and state changes

GameScreen
    observes relevant gameplay changes
    converts them into GameEvent values
    coordinates guidance UI timing

GameEvent
    describes guidance-relevant gameplay events

GuideTrigger
    checks whether an event satisfies a step

GuideStep
    defines guidance message identity and trigger

GuidanceSystem
    owns main and contextual guidance progression
    owns persistent guidance state

GuidanceUi
    renders Ceca and the current message

LocalizationService
    resolves guidance message keys

World
    owns the persistent GuidanceSystem

SaveMapper
    converts GuidanceSystem state to and from GuidanceSaveData

GuidanceSaveData
    stores JSON-friendly guidance state
```

---

## Design Decisions

The current guidance implementation follows these rules:

```text
Guidance reacts to gameplay events, not physical keys.

Main guidance and contextual guidance are separate.

Gameplay continues while Ceca messages are visible.

Enter acknowledges the currently displayed message.

The tutorial does not control the day/night cycle.

The player may craft any successful recipe during the introductory crafting step.

After crafting, the player receives free time until dusk.

Contextual guidance does not advance the main sequence.

Acknowledged contextual messages do not repeat.

Unacknowledged active contextual messages survive save/load.

Guidance text is localized through resource bundles.

Persistent state is stored independently from Scene2D UI state.
```

---

## Future Extensions

The same system can later support guidance connected to additional game content without redesigning the current architecture.

Possible later guidance includes:

```text
first Boat interaction
first departure from the main island
first arrival on a mini-island
island-specific mechanics
important progression discoveries
Crimson Veil warnings
late-game story events
finale and ritual guidance
```

Additional contextual messages can reuse:

```text
activeContextualStep
shownContextualSteps
```

without introducing a separate boolean field for every new message.

The guidance content will therefore expand together with the gameplay systems that require it rather than being fully hardcoded in advance.
