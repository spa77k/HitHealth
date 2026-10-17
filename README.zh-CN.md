# HitHealth

[English](README.md) | [日本語](README.ja.md) | **简体中文** | [한국어](README.ko.md)

一款 PaperMC 插件，在动作栏上显示所攻击目标的剩余生命值。

```
僵尸 ❤ 12.0/20.0
```

## 运行环境

- Minecraft 服务器：PaperMC 26.2、26.3（Paper API `1.21.4` 及以上）
- Java：21 及以上
- 前置插件：无
- 基岩版兼容：通过 Geyser 接入的基岩版玩家同样可以正常显示

## 功能

- 攻击生物或玩家时，攻击者的动作栏上会显示目标的名称及剩余生命值。
- 支持近战攻击（如剑）、弹射物（弓、弩、三叉戟等）以及已驯服宠物（如狼）的攻击。
- 生命值颜色会根据剩余生命值比例动态变化（默认：50% 及以上为绿色，25% 及以上为黄色，低于 25% 为红色）。
- 已命名的生物显示其自定义名称，未命名的生物则按照各玩家客户端的游戏语言显示对应名称。
- 击杀目标时显示 `0.0`。存活目标的生命值向上取整，避免存活状态下显示为 `0.0`。
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
# MiniMessage 格式。<name>：目标名称，<hp>：剩余生命值，<max>：最大生命值，<color>...</color>：按剩余生命值变化的颜色
format: "<white><name></white> <color>❤ <hp>/<max></color>"

thresholds:
  high: 0.5    # 达到此比例及以上使用 colors.high
  low: 0.25    # 达到此比例及以上使用 colors.mid，低于此比例使用 colors.low
colors:
  high: "#55FF55"
  mid: "#FFFF55"
  low: "#FF5555"

decimals: 1          # 生命值的小数位数（0-2）
show-players: true   # 攻击玩家时也显示
```

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `format` | `"<white><name></white> <color>❤ <hp>/<max></color>"` | 动作栏文本格式（MiniMessage 格式）。支持的占位符：`<name>`、`<hp>`、`<max>`、`<color>` |
| `thresholds.high` | `0.5` | 高生命值颜色的比例阈值（50% 及以上） |
| `thresholds.low` | `0.25` | 中生命值颜色的比例阈值（25% 及以上，低于此值使用低生命值颜色） |
| `colors.high` | `"#55FF55"` | 生命值比例达到或超过 `thresholds.high` 时的颜色代码 |
| `colors.mid` | `"#FFFF55"` | 生命值比例在 `thresholds.low` 与 `thresholds.high` 之间时的颜色代码 |
| `colors.low` | `"#FF5555"` | 生命值比例低于 `thresholds.low` 时的颜色代码 |
| `decimals` | `1` | 显示生命值的小数位数（0-2） |
| `show-players` | `true` | 攻击其他玩家时是否也显示生命值 |

### 伤害、显示时长与排除规则

配置文件格式不变；如需保留配置，请先按上述步骤手动复制。将以下内容加入 `config.yml`，然后执行 `/hithealth reload` 可配置新功能。

```yaml
damage:
  enabled: false
  format: " <gray>(-<damage>)</gray>"
display:
  duration-seconds: 0.0
exclusions:
  enabled: true
  worlds: []           # 例如: [world_nether, lobby]
  entity-types: []     # 例如: [WITHER, ENDER_DRAGON]
```

- `damage.enabled: true` 会在原格式后追加伤害，例如 `僵尸 ❤ 12.0/20.0 (-8.0)`。伤害是事件中减伤后的最终生命值伤害，不含伤害吸收之心抵消的部分，并以攻击前的生命值为上限，不计溢出伤害。治疗、死亡保护或其他插件可能使实际生命值变化与该数值不同。
- `damage.format` 支持与主 `format` 相同的占位符。也可将 `<damage>` 直接放入主格式；`damage.enabled` 仅控制追加的后缀。生命值和伤害共用 `decimals`。
- `display.duration-seconds: 0` 表示单次发送，由客户端自然淡出。正值表示每秒刷新目标生命值，到期后清除。每次有效攻击会替换目标和伤害值，并重新计时。
- 时长最多60秒，正的小数向上取整至服务器tick。无效值、负数及非有限值回退为0，超过60的值限制为60。实际时长受服务器tick速率影响。
- 关闭显示、退出服务器、重载配置或停用插件会取消等待中和持续中的显示。
- 排除规则匹配目标的世界名称或Bukkit `EntityType` 名称，任一匹配即可，忽略大小写。请使用 `ZOMBIE` 等枚举名，而非本地化名称或 `minecraft:zombie`。无效项会被忽略并在控制台警告。
- 空列表不排除任何目标。`exclusions.enabled: false` 可保留列表但停用筛选；`show-players: false` 和盔甲架排除规则仍然有效。

其他插件使用动作栏时，显示可能相互覆盖。构建与测试步骤见 [English README](README.md#build-and-tests)。

## 许可证

[MIT License](LICENSE)
