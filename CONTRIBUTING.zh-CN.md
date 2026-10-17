# 为 HitHealth 做贡献

[English](CONTRIBUTING.md) | [日本語](CONTRIBUTING.ja.md) | **简体中文** | [한국어](CONTRIBUTING.ko.md)

感谢你对 HitHealth 的关注。欢迎提交问题报告、翻译和拉取请求（Pull Request）。

## 报告问题和提出功能建议

- 请使用 Issues 页面上的模板。
- Issue 和拉取请求可以用英语、日语、中文或韩语撰写。
- 报告问题时，请写明 Paper 版本、Java 版本、玩家是否通过 Geyser 加入，以及复现步骤。
- 安全问题请不要发到公开的 Issue 中，请参阅 [SECURITY.zh-CN.md](SECURITY.zh-CN.md)。

## 拉取请求

1. 较大的改动或新功能，请先开一个 Issue 讨论方向。
2. Fork 本仓库，并从 `main` 创建分支。
3. 每个拉取请求只包含一项改动。
4. 请确认构建能够通过：

   ```bash
   mvn -B package
   ```

5. 如果改动会影响 HP 的显示或 `/hithealth` 的行为，请同时运行 Paper 测试（参阅 [构建](README.zh-CN.md#构建)）：

   ```bash
   python3 scripts/test-hithealth-paper.py
   ```

6. 如果改动影响用户可见的行为，请更新 `README.md`。翻译版 README 的更新不是必需的，但我们很感谢你这样做。

## 翻译

命令消息位于 `src/main/java/dev/spa/hithealth/Messages.java`。要添加语言，请在其中添加消息，并在 README 中注明支持的语言。

## 许可证

提交贡献即表示你同意你的贡献以 [MIT License](LICENSE) 授权。
