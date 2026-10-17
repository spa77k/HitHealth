# HitHealth

**English** | [日本語](README.ja.md) | [简体中文](README.zh-CN.md) | [한국어](README.ko.md)

A PaperMC plugin that displays the remaining HP of an attacked target on your action bar.

```
Zombie ❤ 12.0/20.0
```

## Requirements

- Minecraft server: PaperMC 26.2 and 26.3 (Paper API `1.21.4` or later)
- Java: 21 or later
- Dependencies: None
- Bedrock Edition: Also works for Bedrock players connected via Geyser

## Features

- Displays the target's name and remaining HP on the action bar when you attack a mob or player.
- Supports melee attacks, projectiles (bows, crossbows, tridents), and attacks by tamed pets (such as wolves).
- HP text color changes dynamically based on the remaining HP ratio (by default: green at 50% or more, yellow at 25% or more, red below that).
- Shows custom names for named mobs, and localized mob names matching each player's client language for unnamed mobs.
- Displays `0.0` when the target is killed. The HP of living targets is rounded up so that living entities never show `0.0`.
- Optionally appends damage dealt by the latest hit, with a configurable MiniMessage format.
- Optionally keeps the display refreshed for a configurable duration; each hit restarts it with the latest target.
- Supports configurable world and entity-type exclusions.
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
# MiniMessage placeholders: <name>, <hp>, <max>, <damage>, <color>...</color>
format: "<white><name></white> <color>❤ <hp>/<max></color>"

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

display:
  duration-seconds: 0.0              # 0: send once; positive: refresh until expiry (max 60)

exclusions:
  enabled: true
  worlds: []                        # example: [world_nether, lobby]
  entity-types: []                  # example: [WITHER, ENDER_DRAGON]
```

| Key | Default | Description |
|---|---|---|
| `format` | `"<white><name></white> <color>❤ <hp>/<max></color>"` | Action bar text using MiniMessage. Supported placeholders: `<name>`, `<hp>`, `<max>`, `<damage>`, `<color>`. |
| `thresholds.high` | `0.5` | Ratio threshold for the high health color (at or above 50%). |
| `thresholds.low` | `0.25` | Ratio threshold for the medium health color (at or above 25%; below it uses low). |
| `colors.high` | `"#55FF55"` | Color hex code when remaining HP is at or above `thresholds.high`. |
| `colors.mid` | `"#FFFF55"` | Color hex code when remaining HP is between `thresholds.low` and `thresholds.high`. |
| `colors.low` | `"#FF5555"` | Color hex code when remaining HP is below `thresholds.low`. |
| `decimals` | `1` | Number of decimal places to display for HP and damage (between 0 and 2). |
| `show-players` | `true` | Whether to show HP when attacking other players. |
| `damage.enabled` | `false` | Append the configured damage suffix to the main format. |
| `damage.format` | `" <gray>(-<damage>)</gray>"` | MiniMessage suffix with the same placeholders as `format`; include any desired leading space. |
| `display.duration-seconds` | `0.0` | Zero sends once and lets the client fade naturally. A positive value refreshes once per second until expiry, then clears the display. Maximum 60 seconds. |
| `exclusions.enabled` | `true` | Apply both exclusion lists. Set to `false` to bypass the lists without deleting them. |
| `exclusions.worlds` | `[]` | Target world names to ignore, case-insensitively. Empty means no world exclusions. |
| `exclusions.entity-types` | `[]` | Bukkit `EntityType` names to ignore, case-insensitively, such as `ZOMBIE`, `WITHER` or `PLAYER`. Empty means no entity-type exclusions. |

### Damage dealt

Set `damage.enabled: true` to show, for example, `Zombie ❤ 12.0/20.0 (-8.0)`.
The number represents the latest event's final health damage after mitigation, excludes damage taken by absorption hearts, and is capped to the target's pre-hit health so overkill is not counted. Fully absorbed hits show zero damage.
This is an event-based damage value, not a before/after HP difference: healing, death protection, or other plugins may affect the target's resulting HP independently.

You can also place `<damage>` directly in the main `format`. That placeholder always resolves; `damage.enabled` controls only the appended suffix. This lets you choose the layout without displaying the damage twice.

### Display duration and exclusions

- The default duration, `0`, preserves the single-send behavior and schedules no repeating refreshes. The client controls its natural action bar fade.
- The initial display is sent one server tick after the damage event so the target's updated HP is available. A positive duration is measured from that first display.
- A positive duration, for example `3.0`, refreshes the current target's remaining HP once per second. Each eligible hit replaces the active display, records the new hit's damage, and restarts the duration. Damage stays tied to that hit while HP is refreshed.
- Positive fractional durations round up to the next server tick (normally 1/20 second). Invalid, negative, or non-finite values fall back to `0`; values above 60 are limited to 60. Server tick rate affects real-world timing.
- Turning the display off, leaving the server, reloading configuration, or disabling the plugin cancels pending/active displays. A reload applies a fresh configuration snapshot to subsequent hits.
- A target is excluded when either its world or its entity type matches a list. World matching uses the target's world, not the attacker's. Use Bukkit enum names such as `ENDERMAN`, not localized names or `minecraft:enderman`.
- `show-players: false` and the built-in armor-stand filter remain effective even when `exclusions.enabled: false`.
- Invalid list entries and entity-type names are ignored with a console warning. Unsupported boolean values fall back to the relevant default. Formatting values are also validated on load; invalid colors and threshold pairs use safe defaults.

Edit `plugins/HitHealth/config.yml`, then run `/hithealth reload` to apply changes. Other plugins that use the action bar can overwrite this display, and a timed display can overwrite theirs while it is active.

## Build and tests

With Java 21 or later and Maven installed:

```bash
mvn -B package
```

The build runs the unit tests and writes `target/hithealth-1.0.0.jar`. To run only the unit tests:

```bash
mvn -B test
```

An optional Paper integration probe is available in `scripts/test-hithealth-paper.py`. After building, place the test server JAR at `server-data/paper-26.2-129.jar` and run:

```bash
JAVA_BIN=/path/to/java python3 scripts/test-hithealth-paper.py
```

The script starts an isolated local server under `target/hithealth-paper-smoke` and writes `eula=true`; run it only if you accept the [Minecraft EULA](https://www.minecraft.net/eula). It requires the server JAR and its runtime dependencies and is separate from the Maven unit tests.

## License

[MIT License](LICENSE)
