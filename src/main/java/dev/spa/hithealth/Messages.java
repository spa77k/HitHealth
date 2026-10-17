package dev.spa.hithealth;

import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Command messages in English, Japanese, Simplified Chinese and Korean, chosen by the player's client language. */
enum Messages {
    NO_PERMISSION(
            "You don't have permission.",
            "権限がありません。",
            "你没有权限。",
            "권한이 없습니다."),
    RELOADED(
            "HitHealth config reloaded.",
            "HitHealthの設定を読み直しました。",
            "已重新加载 HitHealth 配置。",
            "HitHealth 설정을 다시 불러왔습니다."),
    PLAYERS_ONLY(
            "Only players can use this command.",
            "プレイヤーだけが使えます。",
            "只有玩家可以使用此命令。",
            "플레이어만 사용할 수 있습니다."),
    USAGE(
            "Usage: /%s [on|off]",
            "使い方: /%s [on|off]",
            "用法：/%s [on|off]",
            "사용법: /%s [on|off]"),
    ENABLED(
            "HP display for attacked targets is now on.",
            "攻撃した相手のHP表示をオンにしました。",
            "已开启攻击目标的生命值显示。",
            "공격한 대상의 HP 표시를 켰습니다."),
    DISABLED(
            "HP display for attacked targets is now off.",
            "攻撃した相手のHP表示をオフにしました。",
            "已关闭攻击目标的生命值显示。",
            "공격한 대상의 HP 표시를 껐습니다.");

    private final String english;
    private final String japanese;
    private final String chinese;
    private final String korean;

    Messages(String english, String japanese, String chinese, String korean) {
        this.english = english;
        this.japanese = japanese;
        this.chinese = chinese;
        this.korean = korean;
    }

    String text(CommandSender sender, Object... args) {
        String language = sender instanceof Player player ? player.locale().getLanguage() : Locale.ENGLISH.getLanguage();
        String template = switch (language) {
            case "ja" -> japanese;
            case "zh" -> chinese;
            case "ko" -> korean;
            default -> english;
        };
        return args.length == 0 ? template : String.format(template, args);
    }
}
