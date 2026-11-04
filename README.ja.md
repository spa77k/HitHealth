# HitHealth

[English](README.md) | **日本語** | [简体中文](README.zh-CN.md) | [한국어](README.ko.md)

攻撃した相手の残りHPをアクションバーまたはボスバーに表示する PaperMC プラグインです。

```
ゾンビ ❤ 12.0/20.0
```

## 動作環境

- Minecraft サーバー: PaperMC 26.2・26.3（Paper API `1.21.4` 以降）
- Java: 21 以降
- 依存プラグイン: なし（任意で PlaceholderAPI 2.12.3 以降に対応。2.11.x は Paper 26.x で読み込めません）
- 統合版（Geyser 経由）のプレイヤーにも表示されます

## 機能

- Mob やプレイヤーを攻撃すると、攻撃者に相手の名前と残りHPが表示されます。
- 表示先をアクションバー・ボスバー・両方から選べます（`display.mode`）。ボスバーは、他のプラグインのアクションバー表示との競合を避けたい場合に便利です。
- 近接攻撃（剣など）だけでなく、飛び道具（弓、クロスボウ、トライデントなど）や飼いならしたペット（オオカミなど）による攻撃にも対応しています。
- 残りHPの割合に応じてHP表示の色が変わります（既定値: 50%以上は緑、25%以上は黄、25%未満は赤）。
- 名札などで名前を付けたMobはカスタム名、それ以外のMobは各プレイヤーのクライアント言語に応じた名前を表示します。
- 相手を倒したときは `0.0` と表示されます。生きている相手の端数は切り上げられるため、生存中に `0.0` と表示されることはありません。
- `<bar>` プレースホルダーでテキストのHPバーを表示できます。
- 与えたダメージの表示、表示時間、ワールド・エンティティ種別による除外を設定できます（下記参照）。
- PlaceholderAPI が導入されていれば、直近の攻撃内容をプレースホルダーで取得できます。
- 防具立てへの攻撃は無視されます。
- プラグインのメッセージは、各プレイヤーのクライアント言語に合わせて英語・日本語・簡体字中国語・韓国語で表示されます（それ以外の言語では英語）。

## 導入方法

1. Releases から `hithealth-1.0.0.jar` をダウンロードします。
2. サーバーの `plugins/` フォルダに配置します。
3. サーバーを再起動します。

## コマンド

| コマンド | 内容 | 権限 |
|---|---|---|
| `/hithealth` | 自分のHP表示のオン・オフを切り替える | `hithealth.toggle`（全員） |
| `/hithealth on` | 自分のHP表示をオンにする | `hithealth.toggle`（全員） |
| `/hithealth off` | 自分のHP表示をオフにする | `hithealth.toggle`（全員） |
| `/hithealth reload` | `config.yml` を再読み込みする | `hithealth.reload`（OP） |

オフにした設定はプレイヤーごとに `plugins/HitHealth/players.yml` に保存され、サーバー再起動後も保持されます。

## 権限

| 権限 | 内容 | 既定値 |
|---|---|---|
| `hithealth.use` | 攻撃した相手のHPが表示される | 全員 |
| `hithealth.toggle` | `/hithealth` で自分のHP表示を切り替えられる | 全員 |
| `hithealth.reload` | `/hithealth reload` で設定を再読み込みできる | OP |

## 設定（`plugins/HitHealth/config.yml`）

```yaml
# MiniMessage形式。<name>: 相手の名前、<hp>: 残りHP、<max>: 最大HP、<damage>: 直前のダメージ
# <color>〜</color>: 残りHPに応じた色、<bar>: HPバー
format: "<white><name></white> <color>❤ <hp>/<max></color>"
# format: "<white><name></white> <bar> <color><hp>/<max></color>"

thresholds:
  high: 0.5    # この割合以上は colors.high
  low: 0.25    # この割合以上は colors.mid、未満は colors.low
colors:
  high: "#55FF55"
  mid: "#FFFF55"
  low: "#FF5555"

decimals: 1          # HPの小数点以下の桁数（0〜2）
show-players: true   # プレイヤーを攻撃したときも表示する

bar:
  length: 10               # 区間の数（1〜40）
  full: "■"                # 埋まった区間の文字（空不可）
  empty: "□"               # 空の区間の文字（空でも可）
  empty-color: "#555555"   # 埋まった区間は <color> の色を使う

display:
  mode: actionbar          # actionbar / bossbar / both（大文字小文字を区別しない）
  duration-seconds: 0.0    # 0: 1回だけ、正の値: 指定時間まで毎秒更新（最大60秒）

bossbar:
  color-by-health: true    # thresholds.high / thresholds.low に応じて GREEN / YELLOW / RED
  color: RED               # color-by-health が false のときの色
  overlay: PROGRESS        # PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20
  hide-after-seconds: 3.0  # display.duration-seconds が 0 のときだけ使用

placeholderapi:
  target-timeout-seconds: 10  # プレースホルダーが直近の攻撃を保持する秒数（1〜600）

exclusions:
  enabled: true
  worlds: []           # 例: [world_nether, lobby]
  entity-types: []     # 例: [WITHER, ENDER_DRAGON]
```

| 設定キー | 既定値 | 説明 |
|---|---|---|
| `format` | `"<white><name></white> <color>❤ <hp>/<max></color>"` | アクションバー／ボスバーの表示形式（MiniMessage形式）。利用可能なプレースホルダー: `<name>`, `<hp>`, `<max>`, `<damage>`, `<color>`, `<bar>` |
| `thresholds.high` | `0.5` | 高HP色のしきい値割合（50%以上） |
| `thresholds.low` | `0.25` | 中HP色のしきい値割合（25%以上、これ未満は低HP色） |
| `colors.high` | `"#55FF55"` | HP割合が `thresholds.high` 以上のときの色コード |
| `colors.mid` | `"#FFFF55"` | HP割合が `thresholds.low` 以上かつ `thresholds.high` 未満のときの色コード |
| `colors.low` | `"#FF5555"` | HP割合が `thresholds.low` 未満のときの色コード |
| `decimals` | `1` | HPとダメージの小数点以下の桁数（0〜2） |
| `show-players` | `true` | プレイヤーを攻撃したときもHPを表示するかどうか |
| `bar.length` | `10` | `<bar>` の区間の数（1〜40） |
| `bar.full` | `"■"` | 埋まった区間に使う空でない文字列 |
| `bar.empty` | `"□"` | 空の区間に使う文字列（空でも可） |
| `bar.empty-color` | `"#555555"` | 空の区間の色。埋まった区間は現在の `<color>` を使う |
| `display.mode` | `actionbar` | `actionbar`、`bossbar`、`both` のいずれか（大文字小文字を区別しない） |
| `display.duration-seconds` | `0.0` | 0は1回だけ送信し、クライアントの自然なフェードに任せます。正の値では毎秒更新し、時間が過ぎると消去します。最大60秒 |
| `bossbar.color-by-health` | `true` | `thresholds.high` / `thresholds.low` に応じて `GREEN` / `YELLOW` / `RED` を使う |
| `bossbar.color` | `RED` | `color-by-health` が `false` のときのボスバー色（`BossBar.Color` 名） |
| `bossbar.overlay` | `PROGRESS` | ボスバーの表示形式: `PROGRESS`, `NOTCHED_6`, `NOTCHED_10`, `NOTCHED_12`, `NOTCHED_20` |
| `bossbar.hide-after-seconds` | `3.0` | `display.duration-seconds` が `0` のときのボスバー表示時間（0より大きく60以下） |
| `placeholderapi.target-timeout-seconds` | `10` | PlaceholderAPI のプレースホルダーが直近の攻撃を保持する秒数（1〜600） |
| `exclusions.enabled` | `true` | 両方の除外リストを適用します。`false` にするとリストを残したまま無効化できます |
| `exclusions.worlds` | `[]` | 除外するワールド名（大文字小文字を区別しない）。空ならワールド除外なし |
| `exclusions.entity-types` | `[]` | 除外するBukkit `EntityType` 名（`ZOMBIE` など、大文字小文字を区別しない）。空なら種別除外なし |

### ボスバー表示

`display.mode` を `bossbar` にするとアクションバーの代わりに同じ文字をボスバーへ、`both` にすると両方へ表示します。値は大文字小文字を区別しません。不正な値は読み込み時に一度だけ警告し、`actionbar` に戻ります。

- ボスバーはプレイヤーごとに1つを再利用し、更新のたびに名前・進捗・色を変更します。進捗は残りHPの割合を0〜1に制限した値です。
- `bossbar.color-by-health: true` では、文字色と同じ `thresholds.high` / `thresholds.low` の境界で `GREEN` / `YELLOW` / `RED` を使います。`false` の場合は `bossbar.color` の値を使います。
- `bossbar.overlay` は `PROGRESS`、`NOTCHED_6`、`NOTCHED_10`、`NOTCHED_12`、`NOTCHED_20` のいずれかです。
- ボスバーは自然にフェードしないため、`display.duration-seconds` が `0` のときは `bossbar.hide-after-seconds`（0より大きく60以下）を表示時間として使います。`both` ではその間、アクションバーも毎秒再送されます。
- ボスバーは表示時間の終了、`/hithealth off`、退出、設定再読み込み、プラグイン停止時に非表示になります。モードの変更は次の攻撃から適用され、`/hithealth reload` は進行中の表示をすぐに解除します。
- ボスバーモードは、他のプラグインのアクションバー表示との競合を避けられます。`both` はアクションバーも使うため、他のプラグインに上書きされる場合があります。

### HPバー（`<bar>`）

`format` または `damage.format` の任意の位置に `<bar>` を追加できます。

```yaml
format: "<white><name></white> <bar> <color><hp>/<max></color>"
```

生存中の塗りつぶし数は `ceil(割合 * bar.length)` で、生きている対象は最低1区間が埋まります。倒した対象は塗りつぶしなしです。塗りつぶし部分は `<color>` と同じ色、空部分は `bar.empty-color` の色になります。`full`・`empty`・`empty-color` はコンポーネントとして挿入されるため、MiniMessageタグを注入できません。

### PlaceholderAPI

[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) が導入されている場合、識別子 `hithealth` の任意拡張を登録します。追加設定は不要で、PlaceholderAPI がなくてもプラグインは通常どおり動作します。サーバースレッド以外（非同期のスコアボードなど）から呼ばれた場合、HP は生きているエンティティではなく最後に表示した時点の値を返します。

| プレースホルダー | 説明 |
|---|---|
| `%hithealth_enabled%` | そのプレイヤーの表示設定（`true` / `false`） |
| `%hithealth_target_name%` | 直近の攻撃対象の名前（プレイヤーのクライアント言語に翻訳したプレーンテキスト） |
| `%hithealth_target_hp%` | 生存中の対象の残りHP（`<hp>` と同じ数値形式） |
| `%hithealth_target_max%` | 対象の最大HP（`<max>` と同じ数値形式） |
| `%hithealth_target_damage%` | 直近の表示対象へ与えたダメージ（`<damage>` と同じ数値形式） |
| `%hithealth_target_percent%` | 残りHPの整数パーセント（生存中は切り上げ、死亡時は `0`） |
| `%hithealth_target_bar%` | `<bar>` を色なしのプレーンテキストにしたもの |

対象系のプレースホルダーは、そのプレイヤーが最後に表示した攻撃を参照します。対象がない場合、プレイヤーがオフラインの場合、または攻撃から `placeholderapi.target-timeout-seconds` 秒を超えた場合は空文字列を返します。エンティティが消滅または死亡している場合のHPは `0` です。未知のプレースホルダーは `null` を返し、他の拡張に処理を任せます。

### ダメージ・表示時間・除外設定

`config.yml` の該当キーを変更して `/hithealth reload` を実行すると反映されます。

```yaml
damage:
  enabled: false
  format: " <gray>(-<damage>)</gray>"
```

- `damage.enabled: true` にすると、例えば `ゾンビ ❤ 12.0/20.0 (-8.0)` と表示されます。値はダメージ軽減後のイベント上のHPダメージで、吸収ハートが受けた分を除き、攻撃前のHPを上限にします。回復や不死のトーテム、他プラグインの処理によって、実際のHP差とは異なる場合があります。
- `damage.format` はメインの `format` の末尾に追加されます。`<damage>` はメインの `format` 内でも使えます。`damage.enabled` が切り替えるのは末尾の追加表示だけです。HPとダメージの小数点以下の桁数には `decimals` を使います。
- `display.duration-seconds: 0` は一度だけ送信し、クライアントの自然なフェードに任せます（`bossbar` / `both` では `bossbar.hide-after-seconds` を使用）。正の値では毎秒HPを再表示し、指定時間が過ぎると消去します。攻撃するたびに表示対象とダメージが更新され、表示時間がリセットされます。
- 表示時間は最大60秒です。正の端数は1サーバーtick単位に切り上げます。不正な値・負の値・無限大などは0に戻し、60を超える値は60に制限します。サーバーの処理速度によって実時間は変わります。
- 表示オフ、退出、設定再読み込み、プラグイン停止時には、待機中・継続中の表示処理を停止し、ボスバーも非表示にします。
- 除外リストは、対象のワールド名またはBukkitの `EntityType` 名のいずれかが一致すると適用されます。大文字・小文字は区別しません。例: `ZOMBIE`、`WITHER`、`PLAYER`。`minecraft:zombie` や翻訳された名前は使用できません。不正な項目は警告して無視します。
- 空リストは何も除外しません。`exclusions.enabled: false` でリストを保持したまま除外を無効にできます。ただし `show-players: false` と防具立ての除外は引き続き適用されます。

他のプラグインもアクションバーを使っている場合は、互いの表示が上書きされることがあります。その場合は `display.mode: bossbar` を使うと競合を避けられます。ビルドとテストの手順は [English README](README.md#build-and-tests) を参照してください。

## ライセンス

[MIT License](LICENSE)
