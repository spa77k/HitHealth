# HitHealth への貢献

[English](CONTRIBUTING.md) | **日本語** | [简体中文](CONTRIBUTING.zh-CN.md) | [한국어](CONTRIBUTING.ko.md)

HitHealth に興味を持っていただきありがとうございます。不具合報告、翻訳、プルリクエストを歓迎します。

## 不具合の報告・機能の要望

- Issues ページのテンプレートを使ってください。
- Issue やプルリクエストは、英語・日本語・中国語・韓国語のどれで書いてもかまいません。
- 不具合の報告には、Paper のバージョン、Java のバージョン、Geyser 経由で参加したプレイヤーかどうか、再現手順を書いてください。
- セキュリティの問題は、公開の Issue に書かないでください。[SECURITY.ja.md](SECURITY.ja.md) を参照してください。

## プルリクエスト

1. 大きな変更や新機能は、先に Issue を立てて方向性を相談してください。
2. リポジトリをフォークし、`main` からブランチを作ってください。
3. 1つのプルリクエストには、1つの変更だけを含めてください。
4. ビルドが通ることを確かめてください。

   ```bash
   mvn -B package
   ```

5. HP の表示や `/hithealth` の動きが変わる変更では、Paper でのテストも実行してください（[ビルド](README.ja.md#ビルド) を参照）。

   ```bash
   python3 scripts/test-hithealth-paper.py
   ```

6. 利用者から見た動きが変わる場合は、`README.md` を更新してください。翻訳版 README の更新は必須ではありませんが、してもらえると助かります。

## 翻訳

コマンドのメッセージは `src/main/java/dev/spa/hithealth/Messages.java` にあります。言語を追加するときは、ここにメッセージを足し、README に対応言語として書き加えてください。

## ライセンス

貢献していただいた内容は、[MIT License](LICENSE) で提供されることに同意したものとみなします。
