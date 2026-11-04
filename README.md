# HitHealth

**English** | [日本語](README.ja.md) | [简体中文](README.zh-CN.md) | [한국어](README.ko.md)

A PaperMC plugin that displays the remaining HP of an attacked target on your action bar or boss bar.

```
Zombie ❤ 12.0/20.0
```

## Requirements

- Minecraft server: PaperMC 26.2 and 26.3 (Paper API `1.21.4` or later)
- Java: 21 or later
- Dependencies: None (optional: PlaceholderAPI 2.12.3 or later for the placeholders below; 2.11.x does not load on Paper 26.x)
- Bedrock Edition: Also works for Bedrock players connected via Geyser

## Features

- Displays the target's name and remaining HP on the action bar when you attack a mob or player.
- Can use a boss bar instead of, or in addition to, the action bar (`display.mode`). Boss bar mode avoids conflicts with other plugins that also write to the action bar.
- Supports melee attacks, projectiles (bows, crossbows, tridents), and attacks by tamed pets (such as wolves).
- HP text color changes dynamically based on the remaining HP ratio (by default: green at 50% or more, yellow at 25% or more, red below that).
- Shows custom names for named mobs, and localized mob names matching each player's client language for unnamed mobs.
- Displays `0.0` when the target is killed. The HP of living targets is rounded up so that living entities never show `0.0`.
- Optionally appends damage dealt by the latest hit, with a configurable MiniMessage format.
- Optionally renders a text HP bar with the `<bar>` placeholder.
- Optionally keeps the display refreshed for a configurable duration; each hit restarts it with the latest target.
- Supports configurable world and entity-type exclusions.
- Provides optional PlaceholderAPI placeholders for the latest hit.
- Armor stands are ignored.
- Plugin messages are displayed in each player's client language: English, Japanese, Simplified Chinese, or Korean (defaults to English for other languages).

## Installation

1. Download `hithealth-1.0.0.jar` from Releases.
2. Place the jar file into the server's `plugins/` directory.
3. Restart the server.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/hithealth` | Toggle your own HP display on or off | `hithealth.toggle` (everyone) |
| `/hithealth on` | Turn on your own HP display | `hithealth.toggle` (everyone) |
| `/hithealth off` | Turn off your own HP display | `hithealth.toggle` (everyone) |
| `/hithealth reload` | Reload `config.yml` | `hithealth.reload` (OP) |

The off state is saved per player in `plugins/HitHealth/players.yml` and persists across server restarts.

## Permissions

| Permission | Description | Default |
|---|---|---|
| `hithealth.use` | Displays HP when attacking targets | everyone |
| `hithealth.toggle` | Allows toggling your own display with `/hithealth` | everyone |
| `hithealth.reload` | Allows reloading configuration with `/hithealth reload` | OP |

## Configuration (`plugins/HitHealth/config.yml`)

```yaml
# MiniMessage placeholders: <name>, <hp>, <max>, <damage>, <color>...</color>, <bar>
format: "<white><name></white> <color>❤ <hp>/<max></color>"
# format: "<white><name></white> <bar> <color><hp>/<max></color>"

thresholds:
  high: 0.5    # at or above this ratio: colors.high
  low: 0.25    # at or above this ratio: colors.mid, below it: colors.low
colors:
  high: "#55FF55"
  mid: "#FFFF55"
  low: "#FF5555"

decimals: 1          # decimal places for HP (0-2)
show-players: true   # also show HP when attacking players

damage:
  enabled: false
  format: " <gray>(-<damage>)</gray>"  # appended when enabled

bar:
  length: 10               # segments (1-40)
  full: "■"                # filled character(s), non-empty
  empty: "□"               # empty character(s), may be empty
  empty-color: "#555555"   # filled segments use the <color> color

display:
  mode: actionbar          # actionbar, bossbar, or both (case-insensitive)
  duration-seconds: 0.0    # 0: send once; positive: refresh until expiry (max 60)

bossbar:
  color-by-health: true    # GREEN / YELLOW / RED from thresholds.high / thresholds.low
  color: RED               # used when color-by-health is false
  overlay: PROGRESS        # PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20
  hide-after-seconds: 3.0  # used only when display.duration-seconds is 0

placeholderapi:
  target-timeout-seconds: 10  # placeholders expire after this many seconds (1-600)

exclusions:
  enabled: true
  worlds: []                        # example: [world_nether, lobby]
  entity-types: []                  # example: [WITHER, ENDER_DRAGON]
```

| Key | Default | Description |
|---|---|---|
| `format` | `"<white><name></white> <color>❤ <hp>/<max></color>"` | Action bar/boss bar text using MiniMessage. Supported placeholders: `<name>`, `<hp>`, `<max>`, `<damage>`, `<color>`, `<bar>`. |
| `thresholds.high` | `0.5` | Ratio threshold for the high health color (at or above 50%). |
| `thresholds.low` | `0.25` | Ratio threshold for the medium health color (at or above 25%; below it uses low). |
| `colors.high` | `"#55FF55"` | Color hex code when remaining HP is at or above `thresholds.high`. |
| `colors.mid` | `"#FFFF55"` | Color hex code when remaining HP is between `thresholds.low` and `thresholds.high`. |
| `colors.low` | `"#FF5555"` | Color hex code when remaining HP is below `thresholds.low`. |
| `decimals` | `1` | Number of decimal places to display for HP and damage (between 0 and 2). |
| `show-players` | `true` | Whether to show HP when attacking other players. |
| `damage.enabled` | `false` | Append the configured damage suffix to the main format. |
| `damage.format` | `" <gray>(-<damage>)</gray>"` | MiniMessage suffix with the same placeholders as `format`; include any desired leading space. |
| `bar.length` | `10` | Number of segments in the `<bar>` placeholder (1-40). |
| `bar.full` | `"■"` | Non-empty string used for filled segments. |
| `bar.empty` | `"□"` | String used for empty segments; may be empty. |
| `bar.empty-color` | `"#555555"` | Hex color for empty segments. Filled segments use the current `<color>`. |
| `display.mode` | `actionbar` | `actionbar`, `bossbar`, or `both`, case-insensitively. |
| `display.duration-seconds` | `0.0` | Zero sends once and lets the client fade naturally. A positive value refreshes once per second until expiry, then clears the display. Maximum 60 seconds. |
| `bossbar.color-by-health` | `true` | Use `GREEN` / `YELLOW` / `RED` based on `thresholds.high` and `thresholds.low`. |
| `bossbar.color` | `RED` | Boss bar color when `color-by-health` is `false`. Uses `BossBar.Color` names. |
| `bossbar.overlay` | `PROGRESS` | Boss bar overlay: `PROGRESS`, `NOTCHED_6`, `NOTCHED_10`, `NOTCHED_12`, or `NOTCHED_20`. |
| `bossbar.hide-after-seconds` | `3.0` | Boss bar lifetime when `display.duration-seconds` is `0`. Must be greater than 0 and at most 60. |
| `placeholderapi.target-timeout-seconds` | `10` | How long the latest hit remains available to PlaceholderAPI placeholders (1-600 seconds). |
| `exclusions.enabled` | `true` | Apply both exclusion lists. Set to `false` to bypass the lists without deleting them. |
| `exclusions.worlds` | `[]` | Target world names to ignore, case-insensitively. Empty means no world exclusions. |
| `exclusions.entity-types` | `[]` | Bukkit `EntityType` names to ignore, case-insensitively, such as `ZOMBIE`, `WITHER` or `PLAYER`. Empty means no entity-type exclusions. |

### Damage dealt

Set `damage.enabled: true` to show, for example, `Zombie ❤ 12.0/20.0 (-8.0)`.
The number represents the latest event's final health damage after mitigation, excludes damage taken by absorption hearts, and is capped to the target's pre-hit health so overkill is not counted. Fully absorbed hits show zero damage.
This is an event-based damage value, not a before/after HP difference: healing, death protection, or other plugins may affect the target's resulting HP independently.

You can also place `<damage>` directly in the main `format`. That placeholder always resolves; `damage.enabled` controls only the appended suffix. This lets you choose the layout without displaying the damage twice.

### Boss bar display

Set `display.mode` to `bossbar` to show the same text on a boss bar instead of the action bar, or `both` to show both. Values are case-insensitive; an invalid value logs one warning and falls back to `actionbar`.

- The boss bar is reused per player and its name, progress, and color are updated on every refresh. Progress is the remaining HP ratio clamped to 0-1.
- With `bossbar.color-by-health: true`, the bar uses `GREEN`, `YELLOW`, or `RED` at the same `thresholds.high` / `thresholds.low` boundaries as the text color. Set it to `false` to use the `bossbar.color` value instead.
- `bossbar.overlay` accepts `PROGRESS`, `NOTCHED_6`, `NOTCHED_10`, `NOTCHED_12`, or `NOTCHED_20`.
- A boss bar does not fade by itself. When `display.duration-seconds` is `0`, boss bar and both modes use `bossbar.hide-after-seconds` (greater than 0 and at most 60) as the display duration. In `both` mode the action bar is also resent once per second for that period.
- Boss bars are hidden on expiry, `/hithealth off`, player quit, config reload, and plugin disable. Changing the mode applies on the next hit, or immediately after `/hithealth reload` cancels any active display.
- Boss bar mode avoids action-bar conflicts with other plugins. `both` mode still shares the action bar and can be overwritten by other action-bar plugins.

### HP bar placeholder

Add `<bar>` anywhere in `format` or `damage.format`, for example:

```yaml
format: "<white><name></white> <bar> <color><hp>/<max></color>"
```

The filled segment count is `ceil(ratio * bar.length)` while the target is alive, so a living target always shows at least one filled segment. A dead target shows no filled segments. Filled segments use the same color as `<color>`; empty segments use `bar.empty-color`. The configured `full`, `empty`, and `empty-color` values are inserted as a component, so they cannot inject MiniMessage tags.

### PlaceholderAPI

If [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) is installed, HitHealth registers an optional expansion with the `hithealth` identifier. No configuration is required, and the plugin still loads normally without PlaceholderAPI. When a placeholder is requested off the server thread (for example by an asynchronous scoreboard), HP values come from the latest display instead of the live entity.

| Placeholder | Description |
|---|---|
| `%hithealth_enabled%` | `true` or `false` for the player's own display toggle. |
| `%hithealth_target_name%` | Plain-text name of the latest hit target, localized for the player's client language. |
| `%hithealth_target_hp%` | Remaining HP of the live target, using the same formatting as `<hp>`. |
| `%hithealth_target_max%` | Max HP of the live target, using the same formatting as `<max>`. |
| `%hithealth_target_damage%` | Damage of the latest displayed hit, using the same formatting as `<damage>`. |
| `%hithealth_target_percent%` | Remaining HP as an integer percentage, rounded up while alive and `0` when dead. |
| `%hithealth_target_bar%` | The `<bar>` placeholder as plain text without colors. |

The target placeholders refer to the player's latest displayed hit and return an empty string when there is no target, when the player is offline, or when the hit is older than `placeholderapi.target-timeout-seconds`. HP is `0` if the entity is gone or dead. Unknown placeholders return null so other expansions can handle them.

### Display duration and exclusions

- The default duration, `0`, preserves the single-send behavior and schedules no repeating refreshes. The client controls its natural action bar fade. In `bossbar` or `both` mode, `0` means "use `bossbar.hide-after-seconds`" instead.
- The initial display is sent one server tick after the damage event so the target's updated HP is available. A positive duration is measured from that first display.
- A positive duration, for example `3.0`, refreshes the current target's remaining HP once per second. Each eligible hit replaces the active display, records the new hit's damage, and restarts the duration. Damage stays tied to that hit while HP is refreshed.
- Positive fractional durations round up to the next server tick (normally 1/20 second). Invalid, negative, or non-finite values fall back to `0`; values above 60 are limited to 60. Server tick rate affects real-world timing.
- Turning the display off, leaving the server, reloading configuration, or disabling the plugin cancels pending/active displays and hides any boss bar. A reload applies a fresh configuration snapshot to subsequent hits.
- A target is excluded when either its world or its entity type matches a list. World matching uses the target's world, not the attacker's. Use Bukkit enum names such as `ENDERMAN`, not localized names or `minecraft:enderman`.
- `show-players: false` and the built-in armor-stand filter remain effective even when `exclusions.enabled: false`.
- Invalid list entries and entity-type names are ignored with a console warning. Unsupported boolean values fall back to the relevant default. Formatting values are also validated on load; invalid colors, thresholds, bar settings, boss bar settings, and display modes use safe defaults.

Edit `plugins/HitHealth/config.yml`, then run `/hithealth reload` to apply changes. Other plugins that use the action bar can overwrite the action-bar display, and a timed display can overwrite theirs while it is active; use `display.mode: bossbar` to avoid that conflict.

## Build and tests

With Java 21 or later and Maven installed:

```bash
mvn -B package
```

The build runs the unit tests and writes `target/hithealth-1.0.0.jar`. To run only the unit tests:

```bash
mvn -B test
```

An optional Paper integration probe is available in `scripts/test-hithealth-paper.py`. After building, clone [paper-plugin-kit](https://github.com/spa77k/paper-plugin-kit) next to this repository, place the test server JAR at `paper-plugin-kit/server-data/paper-26.2-129.jar` (or point the `PAPER_SERVER_DATA` environment variable at another server-data folder) and run:

```bash
JAVA_BIN=/path/to/java python3 scripts/test-hithealth-paper.py
```

The script starts an isolated local server under `target/hithealth-paper-smoke` and writes `eula=true`; run it only if you accept the [Minecraft EULA](https://www.minecraft.net/eula). It requires the server JAR and its runtime dependencies and is separate from the Maven unit tests.

## License

[MIT License](LICENSE)
