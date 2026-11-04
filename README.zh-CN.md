# HitHealth

[English](README.md) | [日本語](README.ja.md) | **简体中文** | [한국어](README.ko.md)

一款 PaperMC 插件，在动作栏或 Boss 栏上显示所攻击目标的剩余生命值。

```
僵尸 ❤ 12.0/20.0
```

## 运行环境

- Minecraft 服务器：PaperMC 26.2、26.3（Paper API `1.21.4` 及以上）
- Java：21 及以上
- 前置插件：无（可选支持 PlaceholderAPI 2.12.3 及以上；2.11.x 无法在 Paper 26.x 上加载）
- 基岩版兼容：通过 Geyser 接入的基岩版玩家同样可以正常显示

## 功能

- 攻击生物或玩家时，攻击者会看到目标的名称及剩余生命值。
- 可选择在动作栏、Boss 栏或两者同时显示（`display.mode`）。Boss 栏模式可避免与其他使用动作栏的插件互相覆盖。
- 支持近战攻击（如剑）、弹射物（弓、弩、三叉戟等）以及已驯服宠物（如狼）的攻击。
- 生命值颜色会根据剩余生命值比例动态变化（默认：50% 及以上为绿色，25% 及以上为黄色，低于 25% 为红色）。
- 已命名的生物显示其自定义名称，未命名的生物则按照各玩家客户端的游戏语言显示对应名称。
- 击杀目标时显示 `0.0`。存活目标的生命值向上取整，避免存活状态下显示为 `0.0`。
- 可使用 `<bar>` 占位符显示文本生命值条。
- 可配置伤害显示、显示时长，以及按世界或实体类型排除目标。
- 可选提供针对最近一次攻击的 PlaceholderAPI 占位符。
- 忽略盔甲架。
- 插件提示信息会根据玩家客户端语言显示英语、日语、简体中文或韩语（其他语言默认为英语）。

## 安装

1. 从 Releases 下载 `hithealth-1.0.0.jar`。
2. 将 JAR 文件放入服务器的 `plugins/` 目录中。
3. 重启服务器。

## 命令

| 命令 | 说明 | 权限 |
|---|---|---|
| `/hithealth` | 切换自己的生命值显示开关 | `hithealth.toggle`（所有人） |
| `/hithealth on` | 开启自己的生命值显示 | `hithealth.toggle`（所有人） |
| `/hithealth off` | 关闭自己的生命值显示 | `hithealth.toggle`（所有人） |
| `/hithealth reload` | 重新加载 `config.yml` | `hithealth.reload`（OP） |

每个玩家的关闭状态会保存在 `plugins/HitHealth/players.yml` 中，服务器重启后依然有效。

## 权限

| 权限 | 说明 | 默认 |
|---|---|---|
| `hithealth.use` | 攻击目标时显示其生命值 | 所有人 |
| `hithealth.toggle` | 允许使用 `/hithealth` 切换自己的生命值显示 | 所有人 |
| `hithealth.reload` | 允许使用 `/hithealth reload` 重新加载配置 | OP |

## 配置（`plugins/HitHealth/config.yml`）

```yaml
# MiniMessage 格式。<name>：目标名称，<hp>：剩余生命值，<max>：最大生命值，<damage>：最近一次伤害
# <color>...</color>：按剩余生命值变化的颜色，<bar>：生命值条
format: "<white><name></white> <color>❤ <hp>/<max></color>"
# format: "<white><name></white> <bar> <color><hp>/<max></color>"

thresholds:
  high: 0.5    # 达到此比例及以上使用 colors.high
  low: 0.25    # 达到此比例及以上使用 colors.mid，低于此比例使用 colors.low
colors:
  high: "#55FF55"
  mid: "#FFFF55"
  low: "#FF5555"

decimals: 1          # 生命值的小数位数（0-2）
show-players: true   # 攻击玩家时也显示

bar:
  length: 10               # 段数（1-40）
  full: "■"                # 填充段字符（不能为空）
  empty: "□"               # 空白段字符（可以为空）
  empty-color: "#555555"   # 填充段使用 <color> 的颜色

display:
  mode: actionbar          # actionbar、bossbar 或 both（不区分大小写）
  duration-seconds: 0.0    # 0：只发送一次；正数：持续刷新直到到期（最大 60 秒）

bossbar:
  color-by-health: true    # 根据 thresholds.high / thresholds.low 使用 GREEN / YELLOW / RED
  color: RED               # color-by-health 为 false 时使用的颜色
  overlay: PROGRESS        # PROGRESS、NOTCHED_6、NOTCHED_10、NOTCHED_12、NOTCHED_20
  hide-after-seconds: 3.0  # 仅在 display.duration-seconds 为 0 时使用

placeholderapi:
  target-timeout-seconds: 10  # 占位符保留最近一次攻击的秒数（1-600）

exclusions:
  enabled: true
  worlds: []           # 例如: [world_nether, lobby]
  entity-types: []     # 例如: [WITHER, ENDER_DRAGON]
```

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `format` | `"<white><name></white> <color>❤ <hp>/<max></color>"` | 动作栏/Boss 栏文本格式（MiniMessage 格式）。支持的占位符：`<name>`、`<hp>`、`<max>`、`<damage>`、`<color>`、`<bar>` |
| `thresholds.high` | `0.5` | 高生命值颜色的比例阈值（50% 及以上） |
| `thresholds.low` | `0.25` | 中生命值颜色的比例阈值（25% 及以上，低于此值使用低生命值颜色） |
| `colors.high` | `"#55FF55"` | 生命值比例达到或超过 `thresholds.high` 时的颜色代码 |
| `colors.mid` | `"#FFFF55"` | 生命值比例在 `thresholds.low` 与 `thresholds.high` 之间时的颜色代码 |
| `colors.low` | `"#FF5555"` | 生命值比例低于 `thresholds.low` 时的颜色代码 |
| `decimals` | `1` | 生命值和伤害的小数位数（0-2） |
| `show-players` | `true` | 攻击其他玩家时是否也显示生命值 |
| `bar.length` | `10` | `<bar>` 的段数（1-40） |
| `bar.full` | `"■"` | 填充段使用的非空字符串 |
| `bar.empty` | `"□"` | 空白段使用的字符串，可以为空 |
| `bar.empty-color` | `"#555555"` | 空白段的十六进制颜色。填充段使用当前的 `<color>` |
| `display.mode` | `actionbar` | `actionbar`、`bossbar` 或 `both`，不区分大小写 |
| `display.duration-seconds` | `0.0` | 0 表示只发送一次，由客户端自然淡出。正数表示每秒刷新，到期后清除。最大 60 秒 |
| `bossbar.color-by-health` | `true` | 根据 `thresholds.high` / `thresholds.low` 使用 `GREEN` / `YELLOW` / `RED` |
| `bossbar.color` | `RED` | `color-by-health` 为 `false` 时使用的 Boss 栏颜色（`BossBar.Color` 名称） |
| `bossbar.overlay` | `PROGRESS` | Boss 栏样式：`PROGRESS`、`NOTCHED_6`、`NOTCHED_10`、`NOTCHED_12`、`NOTCHED_20` |
| `bossbar.hide-after-seconds` | `3.0` | `display.duration-seconds` 为 `0` 时的 Boss 栏显示时间（大于 0 且不超过 60） |
| `placeholderapi.target-timeout-seconds` | `10` | PlaceholderAPI 占位符保留最近一次攻击的秒数（1-600） |
| `exclusions.enabled` | `true` | 应用两个排除列表。设为 `false` 可在保留列表的同时停用筛选 |
| `exclusions.worlds` | `[]` | 要排除的目标世界名称，不区分大小写。空列表表示不排除世界 |
| `exclusions.entity-types` | `[]` | 要排除的 Bukkit `EntityType` 名称，如 `ZOMBIE`，不区分大小写。空列表表示不排除实体类型 |

### Boss 栏显示

将 `display.mode` 设为 `bossbar` 可在 Boss 栏显示与动作栏相同的文本，设为 `both` 则同时显示。值不区分大小写；无效值会在加载时警告一次并回退到 `actionbar`。

- Boss 栏按玩家复用同一条，在每次刷新时更新名称、进度和颜色。进度是限制在 0-1 的剩余生命值比例。
- `bossbar.color-by-health: true` 时，使用与文字颜色相同的 `thresholds.high` / `thresholds.low` 边界选择 `GREEN` / `YELLOW` / `RED`。设为 `false` 时使用 `bossbar.color`。
- `bossbar.overlay` 可选 `PROGRESS`、`NOTCHED_6`、`NOTCHED_10`、`NOTCHED_12`、`NOTCHED_20`。
- Boss 栏不会自行淡出。当 `display.duration-seconds` 为 `0` 时，bossbar 和 both 模式使用 `bossbar.hide-after-seconds`（大于 0 且不超过 60）作为显示时长。both 模式下动作栏也会在该时间内每秒重新发送。
- Boss 栏会在到期、`/hithealth off`、玩家退出、重载配置和停用插件时隐藏。模式变更从下一次攻击开始生效，`/hithealth reload` 会立即取消正在进行的显示。
- Boss 栏模式可避免与其他动作栏插件冲突。both 模式仍会使用动作栏，可能被其他插件覆盖。

### 生命值条（`<bar>`）

可在 `format` 或 `damage.format` 的任意位置加入 `<bar>`，例如：

```yaml
format: "<white><name></white> <bar> <color><hp>/<max></color>"
```

目标存活时，填充段数为 `ceil(比例 * bar.length)`，因此存活目标至少显示 1 个填充段；目标死亡时不填充。填充段使用与 `<color>` 相同的颜色，空白段使用 `bar.empty-color`。`full`、`empty` 和 `empty-color` 会作为组件插入，不能注入 MiniMessage 标签。

### PlaceholderAPI

安装 [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) 后，HitHealth 会注册标识符为 `hithealth` 的可选扩展。无需额外配置；未安装 PlaceholderAPI 时插件也能正常加载。在服务器线程以外（例如异步计分板）请求时，HP 数值取自最近一次显示，而不是实时读取实体。

| 占位符 | 说明 |
|---|---|
| `%hithealth_enabled%` | 玩家自己的显示开关，`true` 或 `false` |
| `%hithealth_target_name%` | 最近攻击目标的纯文本名称，按玩家客户端语言翻译 |
| `%hithealth_target_hp%` | 存活目标的剩余生命值，格式与 `<hp>` 相同 |
| `%hithealth_target_max%` | 目标的最大生命值，格式与 `<max>` 相同 |
| `%hithealth_target_damage%` | 最近一次显示的攻击伤害，格式与 `<damage>` 相同 |
| `%hithealth_target_percent%` | 剩余生命值的整数百分比，存活时向上取整，死亡时为 `0` |
| `%hithealth_target_bar%` | 无颜色的纯文本 `<bar>` |

目标类占位符引用玩家最近一次显示的攻击。没有目标、玩家离线，或攻击时间超过 `placeholderapi.target-timeout-seconds` 秒时返回空字符串。实体消失或死亡时 HP 为 `0`。未知占位符返回 null，交由其他扩展处理。

### 伤害、显示时长与排除规则

修改 `config.yml` 中对应的键并执行 `/hithealth reload` 即可生效。

```yaml
damage:
  enabled: false
  format: " <gray>(-<damage>)</gray>"
```

- `damage.enabled: true` 会在原格式后追加伤害，例如 `僵尸 ❤ 12.0/20.0 (-8.0)`。伤害是事件中减伤后的最终生命值伤害，不含伤害吸收之心抵消的部分，并以攻击前的生命值为上限，不计溢出伤害。治疗、死亡保护或其他插件可能使实际生命值变化与该数值不同。
- `damage.format` 支持与主 `format` 相同的占位符。也可将 `<damage>` 直接放入主格式；`damage.enabled` 仅控制追加的后缀。生命值和伤害共用 `decimals`。
- `display.duration-seconds: 0` 表示单次发送，由客户端自然淡出（bossbar/both 模式改用 `bossbar.hide-after-seconds`）。正值表示每秒刷新目标生命值，到期后清除。每次有效攻击会替换目标和伤害值，并重新计时。
- 时长最多60秒，正的小数向上取整至服务器tick。无效值、负数及非有限值回退为0，超过60的值限制为60。实际时长受服务器tick速率影响。
- 关闭显示、退出服务器、重载配置或停用插件会取消等待中和持续中的显示，并隐藏 Boss 栏。
- 排除规则匹配目标的世界名称或Bukkit `EntityType` 名称，任一匹配即可，忽略大小写。请使用 `ZOMBIE` 等枚举名，而非本地化名称或 `minecraft:zombie`。无效项会被忽略并在控制台警告。
- 空列表不排除任何目标。`exclusions.enabled: false` 可保留列表但停用筛选；`show-players: false` 和盔甲架排除规则仍然有效。

其他插件使用动作栏时，显示可能相互覆盖；可使用 `display.mode: bossbar` 避免冲突。构建与测试步骤见 [English README](README.md#build-and-tests)。

## 许可证

[MIT License](LICENSE)
