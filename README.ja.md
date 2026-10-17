# HitHealth

[English](README.md) | **日本語** | [简体中文](README.zh-CN.md) | [한국어](README.ko.md)

攻撃した相手の残りHPをアクションバーに表示する PaperMC プラグインです。

```
ゾンビ ❤ 12.0/20.0
```

## 動作環境

- Minecraft サーバー: PaperMC 26.2・26.3（Paper API `1.21.4` 以降）
- Java: 21 以降
- 依存プラグイン: なし
- 統合版（Geyser 経由）のプレイヤーにも表示されます

## 機能

- Mob やプレイヤーを攻撃すると、攻撃者のアクションバーに相手の名前と残りHPが表示されます。
- 近接攻撃（剣など）だけでなく、飛び道具（弓、クロスボウ、トライデントなど）や飼いならしたペット（オオカミなど）による攻撃にも対応しています。
- 残りHPの割合に応じてHP表示の色が変わります（既定値: 50%以上は緑、25%以上は黄、25%未満は赤）。
- 名札などで名前を付けたMobはカスタム名、それ以外のMobは各プレイヤーのクライアント言語に応じた名前を表示します。
- 相手を倒したときは `0.0` と表示されます。生きている相手の端数は切り上げられるため、生存中に `0.0` と表示されることはありません。
- 与えたダメージの表示、表示時間、ワールド・エンティティ種別による除外を設定できます（下記参照）。
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
# MiniMessage形式。<name>: 相手の名前、<hp>: 残りHP、<max>: 最大HP、<color>〜</color>: 残りHPに応じた色
format: "<white><name></white> <color>❤ <hp>/<max></color>"

thresholds:
  high: 0.5    # この割合以上は colors.high
  low: 0.25    # この割合以上は colors.mid、未満は colors.low
colors:
  high: "#55FF55"
  mid: "#FFFF55"
  low: "#FF5555"

decimals: 1          # HPの小数点以下の桁数（0〜2）
show-players: true   # プレイヤーを攻撃したときも表示する
```

| 設定キー | 既定値 | 説明 |
|---|---|---|
| `format` | `"<white><name></white> <color>❤ <hp>/<max></color>"` | アクションバーの表示形式（MiniMessage形式）。利用可能なプレースホルダー: `<name>`, `<hp>`, `<max>`, `<color>` |
| `thresholds.high` | `0.5` | 高HP色のしきい値割合（50%以上） |
| `thresholds.low` | `0.25` | 中HP色のしきい値割合（25%以上、これ未満は低HP色） |
| `colors.high` | `"#55FF55"` | HP割合が `thresholds.high` 以上のときの色コード |
| `colors.mid` | `"#FFFF55"` | HP割合が `thresholds.low` 以上かつ `thresholds.high` 未満のときの色コード |
| `colors.low` | `"#FF5555"` | HP割合が `thresholds.low` 未満のときの色コード |
| `decimals` | `1` | HP表示の小数点以下の桁数（0〜2） |
| `show-players` | `true` | プレイヤーを攻撃したときもHPを表示するかどうか |

### ダメージ・表示時間・除外設定

設定ファイルの形式は変わりません。引き継ぐ場合は上記の手動コピーが必要です。以下を `config.yml` に追加して `/hithealth reload` を実行すると、新しい機能を設定できます。

```yaml
damage:
  enabled: false
  format: " <gray>(-<damage>)</gray>"
display:
  duration-seconds: 0.0
exclusions:
  enabled: true
  worlds: []           # 例: [world_nether, lobby]
  entity-types: []     # 例: [WITHER, ENDER_DRAGON]
```

- `damage.enabled: true` にすると、例えば `ゾンビ ❤ 12.0/20.0 (-8.0)` と表示されます。値はダメージ軽減後のイベント上のHPダメージで、吸収ハートが受けた分を除き、攻撃前のHPを上限にします。回復や不死のトーテム、他プラグインの処理によって、実際のHP差とは異なる場合があります。
- `damage.format` はメインの `format` の末尾に追加されます。`<damage>` はメインの `format` 内でも使えます。`damage.enabled` が切り替えるのは末尾の追加表示だけです。HPとダメージの小数点以下の桁数には `decimals` を使います。
- `display.duration-seconds: 0` は一度だけ送信し、クライアントの自然なフェードに任せます。正の値では毎秒HPを再表示し、指定時間が過ぎると消去します。攻撃するたびに表示対象とダメージが更新され、表示時間がリセットされます。
- 表示時間は最大60秒です。正の端数は1サーバーtick単位に切り上げます。不正な値・負の値・無限大などは0に戻し、60を超える値は60に制限します。サーバーの処理速度によって実時間は変わります。
- 表示オフ、退出、設定再読み込み、プラグイン停止時には、待機中・継続中の表示処理を停止します。
- 除外リストは、対象のワールド名またはBukkitの `EntityType` 名のいずれかが一致すると適用されます。大文字・小文字は区別しません。例: `ZOMBIE`、`WITHER`、`PLAYER`。`minecraft:zombie` や翻訳された名前は使用できません。不正な項目は警告して無視します。
- 空リストは何も除外しません。`exclusions.enabled: false` でリストを保持したまま除外を無効にできます。ただし `show-players: false` と防具立ての除外は引き続き適用されます。

他のプラグインもアクションバーを使っている場合は、互いの表示が上書きされることがあります。ビルドとテストの手順は [English README](README.md#build-and-tests) を参照してください。

## ライセンス

[MIT License](LICENSE)
