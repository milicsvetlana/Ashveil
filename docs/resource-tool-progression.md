# Resource and Tool Progression

Ashveil uses two tool tiers:

- Wooden
- Stone

The current tool types are:

- Wooden Axe and Stone Axe
- Wooden Pickaxe and Stone Pickaxe
- Wooden Hoe and Stone Hoe
- Wooden Sword and Stone Sword

Tool durability belongs to each individual `ItemStack`. Two tools of the same
type can therefore have different remaining durability.

## Resource rules

Trees can be damaged with an empty hand.

Rocks cannot be damaged with an empty hand or with unsuitable tools. A Pickaxe
is required to damage and destroy a Rock.

The first destroyed Tree guarantees the configured starting Wood amount. This
rule prevents the player from being blocked at the beginning of progression.

The first-tree reward is tracked through `ProgressionState`, rather than the
tutorial system. It therefore remains active even when the tutorial is skipped
or disabled.

Later Tree and Rock drops use the configured inclusive minimum–maximum range.

## Progression state

`ProgressionState` stores persistent gameplay progression flags.

It currently tracks whether the guaranteed first-tree reward has already been
claimed. Future island unlocks, unique rewards and major progression events may
be added when those systems are implemented.

Tutorial progress will later be managed separately by an event-driven
`TutorialManager`.

## Balance

Current HP, damage and durability values are temporary.

Final values will be decided after the game has proper sprites, feedback,
inventory information and a complete playable resource loop.
