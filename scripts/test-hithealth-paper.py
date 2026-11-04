#!/usr/bin/env python3
"""隔離Paperで、攻撃後のHP表示・色・オンオフの保存を検証する。

Paper本体と依存JARは paper-plugin-kit の server-data/ に置いておく
（PAPER_SERVER_DATA で別のフォルダも指定できる）。
先に `mvn -B package` を実行しておく（target/test-classes を使う）。
"""
from pathlib import Path
import os
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, os.environ.get("PAPER_PLUGIN_KIT", str(ROOT.parent / "paper-plugin-kit")))
from paper_smoke import PaperTest, check

WORDS = ("ok: ", "HitHealth", "HITHEALTH_PROBE", "Exception", "Error")


def main():
    test = PaperTest(ROOT, "hithealth", "paper-26.2-129.jar", port=25587,
                     properties=("max-players=1",))
    test.prepare()
    test.add_plugin(ROOT / "target/hithealth-1.0.0.jar", "HitHealth.jar")
    test.add_dependency("PlaceholderAPI.jar", required=False)
    test.make_probe(
        "HitHealthProbe.jar",
        "name: HitHealthProbe\nversion: 1\n"
        "main: dev.spa.hithealth.HitHealthProbe\n"
        "api-version: '1.21'\ndepend: [HitHealth]\n"
        "softdepend: [PlaceholderAPI]\n",
        "dev/spa/hithealth", "HitHealthProbe*.class",
    )

    output = test.run_until_marker(
        ("HITHEALTH_PROBE_PASS", "HITHEALTH_PROBE_FAIL"),
        timeout=240, stop_timeout=60, jvm_args=("-Xms512M", "-Xmx1G"), words=WORDS,
    )
    check("HITHEALTH_PROBE_PASS" in output, "攻撃後のHP表示・色・保存を確認できませんでした")

    test.save_runtime()
    print("PASS: 攻撃後のHP表示・色・オンオフの保存を確認しました。")


if __name__ == "__main__":
    main()
