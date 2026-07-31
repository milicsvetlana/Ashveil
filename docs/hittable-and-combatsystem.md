## Hittable and Combat System

The combat and resource interaction logic is unified through the `Hittable`
interface and the `CombatSystem` class.

Previously, `Player` contained separate attack and harvesting methods and
directly processed enemy and resource lists. This caused the player class to
depend on concrete target types and duplicated targeting logic.

The new system treats enemies and resource objects as targets of the same
primary action.

### Hittable interface

`Hittable` represents any world object that can be contacted by the player's
primary action.

The interface provides:

- the target's center position;
- its `HitCategory`;
- a method for receiving a hit.

Current implementations are:

- `Enemy`, which uses the `ENTITY` category;
- `ResourceObject`, which obtains its category from `ResourceType`.

`Enemy` implements `Hittable` at the abstract base-class level. Therefore,
all current and future enemy types automatically support the combat system
without implementing the interface separately.

`ResourceObject` delegates hit handling to the existing `hit()` method and
uses the category defined by its resource type, such as `WOOD` or `STONE`.

### Hit categories

`HitCategory` groups targets according to how items interact with them.

Current categories are:

- `ENTITY`;
- `WOOD`;
- `STONE`.

These categories allow new target types to be added later without placing
target-specific conditions inside `CombatSystem`.

For example, future ore resources can use a new `ORE` category without
requiring the combat system to know a concrete ore class.

### Damage profiles

Each usable tool or weapon has a `DamageProfile`.

A damage profile contains:

- a base damage value;
- optional damage overrides for specific `HitCategory` values.

This allows the same item to deal different damage depending on the target.

For example:

- an Axe is effective against `WOOD`;
- a Pickaxe is effective against `STONE`;
- a Sword is effective against `ENTITY`;
- an unsuitable tool may deal zero damage.

A hit that deals zero damage is still considered physical contact and consumes
durability.

Empty hands and ordinary non-durable items use
`Config.PLAYER_BASE_DAMAGE`.

### Primary action flow

`World` creates one temporary `List<Hittable>` containing all current enemies
and resource objects.

The list is passed to:

`CombatSystem.performPrimaryAction(Player, List<Hittable>)`

The combat system then:

1. filters targets outside the configured range;
2. filters targets outside the player's forward cone;
3. sorts valid targets by distance from the player;
4. processes targets from nearest to farthest;
5. calculates damage according to the target category;
6. applies the hit;
7. consumes durability after each contacted target;
8. removes the active item if its durability reaches zero.

A single primary action may contact multiple valid targets.

When a durable item breaks during the action, the action stops immediately.
Remaining targets are not hit with an empty hand.

The item selected at the beginning of the action is used for the entire action.

### Distance and targeting

Target distance is compared by using squared distance:

`dx * dx + dy * dy`

The square root is not required because squared distances preserve the same
ordering as real distances.

The forward cone is checked using the dot product between:

- the normalized direction from the player to the target;
- the player's current facing direction.

The target is valid when the dot product is greater than or equal to
`Config.PLAYER_PRIMARY_ACTION_MIN_DOT`.

A larger minimum dot value creates a narrower forward cone.

### Cooldown decision

All primary actions use one cooldown stored in `Player`.

There are no separate attack and harvesting cooldowns.

Every action requires a new input press. Holding the primary-action button does
not repeatedly use tools or weapons.

This prevents input spamming and keeps action timing consistent regardless of
the selected item.

`World` checks whether the player can use the action and resets the cooldown
after the action is performed.

### Responsibility separation

`CombatSystem` is responsible for:

- target validation;
- cone and range checks;
- distance ordering;
- category-based damage;
- durability consumption;
- breaking and removing exhausted items.

`Player` is responsible for:

- position;
- facing;
- inventory ownership;
- selected hotbar slot;
- primary-action cooldown.

`World` is responsible for:

- collecting available targets;
- invoking the combat system;
- removing dead enemies;
- removing destroyed resources;
- creating resource drops.

Target death, resource removal and world-item spawning remain outside
`CombatSystem`, because they are world-state responsibilities rather than
combat calculations.
