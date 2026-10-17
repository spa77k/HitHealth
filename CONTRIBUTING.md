# Contributing to HitHealth

**English** | [日本語](CONTRIBUTING.ja.md) | [简体中文](CONTRIBUTING.zh-CN.md) | [한국어](CONTRIBUTING.ko.md)

Thanks for your interest in HitHealth. Bug reports, translations and pull requests are welcome.

## Reporting bugs and requesting features

- Use the issue templates on the Issues page.
- You can write issues and pull requests in English, Japanese, Chinese or Korean.
- For bugs, include your Paper version, Java version, whether the player joined through Geyser, and steps to reproduce.
- For security problems, do not open a public issue. See [SECURITY.md](SECURITY.md).

## Pull requests

1. For large changes or new features, open an issue first so we can agree on the direction.
2. Fork the repository and create a branch from `main`.
3. Keep each pull request focused on one change.
4. Make sure the build passes:

   ```bash
   mvn -B package
   ```

   This also runs the formatter, settings, damage, display-lifecycle and player-preference unit tests, which do not require a running Paper server.
   To run just the unit tests, use `mvn -B test`.

5. If your change affects how HP is shown or how `/hithealth` behaves, also run the Paper test (see [Build and tests](README.md#build-and-tests)):

   ```bash
   python3 scripts/test-hithealth-paper.py
   ```

6. If you change user-facing behavior, update `README.md`. Updating the translated READMEs is appreciated but not required.

## Translations

Command messages live in `src/main/java/dev/spa/hithealth/Messages.java`. To add a language, add its messages there and mention the locale in the README.

## License

By contributing, you agree that your contributions are licensed under the [MIT License](LICENSE).
