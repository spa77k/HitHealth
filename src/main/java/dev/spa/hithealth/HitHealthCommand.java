package dev.spa.hithealth;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

/** /hithealth [on|off|reload] */
public final class HitHealthCommand implements TabExecutor {

    private final HitHealthPlugin plugin;

    public HitHealthCommand(HitHealthPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String action = args.length == 0 ? "toggle" : args[0].toLowerCase();
        if (action.equals("reload")) {
            if (!sender.hasPermission("hithealth.reload")) {
                sender.sendMessage(Component.text(Messages.NO_PERMISSION.text(sender), NamedTextColor.RED));
                return true;
            }
            plugin.reloadSettings();
            sender.sendMessage(Component.text(Messages.RELOADED.text(sender), NamedTextColor.GREEN));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(Messages.PLAYERS_ONLY.text(sender), NamedTextColor.RED));
            return true;
        }
        if (!List.of("on", "off", "toggle").contains(action)) {
            sender.sendMessage(Component.text(Messages.USAGE.text(sender, label), NamedTextColor.RED));
            return true;
        }
        boolean enabled = action.equals("toggle") ? !plugin.isEnabledFor(player.getUniqueId()) : action.equals("on");
        plugin.setEnabledFor(player.getUniqueId(), enabled);
        player.sendMessage(enabled
                ? Component.text(Messages.ENABLED.text(player), NamedTextColor.GREEN)
                : Component.text(Messages.DISABLED.text(player), NamedTextColor.GRAY));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        List<String> options = sender.hasPermission("hithealth.reload")
                ? List.of("on", "off", "reload") : List.of("on", "off");
        return options.stream().filter(option -> option.startsWith(args[0].toLowerCase())).toList();
    }
}
