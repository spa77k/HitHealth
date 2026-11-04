package dev.spa.hithealth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

/** Public identity and command contract, without starting a Minecraft server. */
class HitHealthIdentityTest {

    @Test
    void filteredDescriptorUsesTheHitHealthIdentityAndMainClass() throws Exception {
        PluginDescriptionFile descriptor = descriptor();
        assertEquals("HitHealth", descriptor.getName());
        assertEquals("HitHealth", descriptor.getRawName());
        assertEquals("1.0.0", descriptor.getVersion());
        assertEquals(HitHealthPlugin.class.getName(), descriptor.getMain());
        assertEquals("dev.spa.hithealth.HitHealthPlugin", descriptor.getMain());
        assertTrue(JavaPlugin.class.isAssignableFrom(Class.forName(descriptor.getMain())));
        assertEquals("1.21.4", descriptor.getAPIVersion());
    }

    @Test
    void descriptorRegistersOnlyTheSupportedCommandWithoutAliases() throws Exception {
        Map<String, Map<String, Object>> commands = descriptor().getCommands();
        assertEquals(Set.of("hithealth"), commands.keySet());
        assertFalse(commands.get("hithealth").containsKey("aliases"));
        assertFalse(commands.get("hithealth").containsKey("permission"));
        assertEquals("/hithealth [on|off|reload]", commands.get("hithealth").get("usage"));
        assertTrue(descriptor().getProvides().isEmpty());
    }

    @Test
    void descriptorHasExactlyTheThreeSupportedPermissionsWithTheirDefaults() throws Exception {
        Map<String, Permission> permissions = descriptor().getPermissions().stream()
                .collect(Collectors.toMap(Permission::getName, permission -> permission));
        assertEquals(Set.of("hithealth.use", "hithealth.toggle", "hithealth.reload"), permissions.keySet());
        assertEquals(PermissionDefault.TRUE, permissions.get("hithealth.use").getDefault());
        assertEquals(PermissionDefault.TRUE, permissions.get("hithealth.toggle").getDefault());
        assertEquals(PermissionDefault.OP, permissions.get("hithealth.reload").getDefault());
        assertTrue(permissions.values().stream().allMatch(permission -> permission.getChildren().isEmpty()));
    }

    @Test
    void tabCompletionOffersOnlyTheOptionsTheSenderMayUse() {
        List<String> checkedPermissions = new ArrayList<>();
        CommandSender reloadOnly = sender(Set.of("hithealth.reload"), checkedPermissions, new ArrayList<>());
        assertEquals(List.of("reload"), new HitHealthCommand(null)
                .onTabComplete(reloadOnly, null, "hithealth", new String[] {""}));
        assertEquals(List.of("hithealth.toggle", "hithealth.reload"), checkedPermissions);
    }

    @Test
    void tabCompletionWithoutReloadPermissionOmitsReload() {
        CommandSender sender = sender(Set.of("hithealth.toggle"), new ArrayList<>(), new ArrayList<>());
        HitHealthCommand command = new HitHealthCommand(null);
        assertEquals(List.of("on", "off"), command.onTabComplete(sender, null, "hithealth", new String[] {""}));
        assertEquals(List.of(), command.onTabComplete(sender, null, "hithealth", new String[] {"r"}));
        assertEquals(List.of(), command.onTabComplete(sender, null, "hithealth", new String[] {"on", ""}));
    }

    @Test
    void tabCompletionWithoutAnyPermissionOmitsEverything() {
        CommandSender sender = sender(Set.of(), new ArrayList<>(), new ArrayList<>());
        assertEquals(List.of(), new HitHealthCommand(null)
                .onTabComplete(sender, null, "hithealth", new String[] {""}));
    }

    @Test
    void deniedReloadChecksTheSupportedPermissionAndNeverInvokesThePlugin() {
        List<String> checkedPermissions = new ArrayList<>();
        List<Component> messages = new ArrayList<>();
        CommandSender sender = sender(Set.of(), checkedPermissions, messages);
        assertTrue(new HitHealthCommand(null).onCommand(sender, null, "hithealth", new String[] {"reload"}));
        assertEquals(List.of("hithealth.reload"), checkedPermissions);
        assertEquals(List.of(Messages.NO_PERMISSION.text(sender)), plain(messages));
    }

    @Test
    void consoleCannotToggleAPlayerPreference() {
        List<Component> messages = new ArrayList<>();
        CommandSender sender = sender(Set.of(), new ArrayList<>(), messages);
        assertTrue(new HitHealthCommand(null).onCommand(sender, null, "hithealth", new String[] {"off"}));
        assertEquals(List.of(Messages.PLAYERS_ONLY.text(sender)), plain(messages));
    }

    @Test
    void playerWithoutTogglePermissionIsDeniedForOnOffAndToggle() {
        for (String action : List.of("on", "off", "toggle")) {
            List<String> checkedPermissions = new ArrayList<>();
            List<Component> messages = new ArrayList<>();
            Player player = player(Set.of(), checkedPermissions, messages);
            assertTrue(new HitHealthCommand(null).onCommand(player, null, "hithealth", new String[] {action}));
            assertTrue(checkedPermissions.contains("hithealth.toggle"));
            assertEquals(List.of(Messages.NO_PERMISSION.text(player)), plain(messages));
        }
    }

    @Test
    void usageShowsReloadOnlyWhenTheSenderMayReload() {
        List<String> withoutReloadChecked = new ArrayList<>();
        List<Component> withoutReloadMessages = new ArrayList<>();
        Player withoutReload = player(Set.of("hithealth.toggle"), withoutReloadChecked, withoutReloadMessages);
        assertTrue(new HitHealthCommand(null).onCommand(withoutReload, null, "hithealth", new String[] {"bad"}));
        assertEquals(List.of("Usage: /hithealth [on|off]"), plain(withoutReloadMessages));

        List<String> withReloadChecked = new ArrayList<>();
        List<Component> withReloadMessages = new ArrayList<>();
        Player withReload = player(Set.of("hithealth.toggle", "hithealth.reload"), withReloadChecked, withReloadMessages);
        assertTrue(new HitHealthCommand(null).onCommand(withReload, null, "hithealth", new String[] {"bad"}));
        assertEquals(List.of("Usage: /hithealth [on|off|reload]"), plain(withReloadMessages));
    }

    @Test
    void allSupportedLocalesUseTheCurrentBrandAndCommand() {
        for (Locale locale : List.of(Locale.ENGLISH, Locale.JAPANESE, Locale.SIMPLIFIED_CHINESE, Locale.KOREAN)) {
            Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(),
                    new Class<?>[] {Player.class}, (proxy, method, args) -> {
                        if (method.getName().equals("locale")) return locale;
                        throw new UnsupportedOperationException(method.getName());
                    });
            assertTrue(Messages.RELOADED.text(player).contains("HitHealth"));
            assertTrue(Messages.USAGE.text(player, "hithealth", "on|off").contains("/hithealth [on|off]"));
            assertTrue(Messages.USAGE.text(player, "hithealth", "on|off|reload").contains("/hithealth [on|off|reload]"));
        }
    }

    private static PluginDescriptionFile descriptor() throws Exception {
        try (InputStream resource = HitHealthIdentityTest.class.getResourceAsStream("/plugin.yml")) {
            assertNotNull(resource);
            return new PluginDescriptionFile(resource);
        }
    }

    private static CommandSender sender(Set<String> permissions, List<String> checked, List<Component> messages) {
        return (CommandSender) Proxy.newProxyInstance(CommandSender.class.getClassLoader(),
                new Class<?>[] {CommandSender.class}, (proxy, method, args) -> {
                    if (method.getName().equals("hasPermission")) {
                        checked.add((String) args[0]);
                        return permissions.contains(args[0]);
                    }
                    if (method.getName().equals("sendMessage") && args[0] instanceof Component message) {
                        messages.add(message);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static Player player(Set<String> permissions, List<String> checked, List<Component> messages) {
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(),
                new Class<?>[] {Player.class}, (proxy, method, args) -> {
                    if (method.getName().equals("hasPermission")) {
                        checked.add((String) args[0]);
                        return permissions.contains(args[0]);
                    }
                    if (method.getName().equals("sendMessage") && args.length == 1 && args[0] instanceof Component message) {
                        messages.add(message);
                        return null;
                    }
                    if (method.getName().equals("locale")) {
                        return Locale.ENGLISH;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static List<String> plain(List<Component> messages) {
        return messages.stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();
    }
}
