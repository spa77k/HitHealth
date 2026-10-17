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
        assertEquals("1.21", descriptor.getAPIVersion());
    }

    @Test
    void descriptorRegistersOnlyTheSupportedCommandWithoutAliases() throws Exception {
        Map<String, Map<String, Object>> commands = descriptor().getCommands();
        assertEquals(Set.of("hithealth"), commands.keySet());
        assertFalse(commands.get("hithealth").containsKey("aliases"));
        assertEquals("hithealth.toggle", commands.get("hithealth").get("permission"));
        assertEquals("/hithealth [on|off]", commands.get("hithealth").get("usage"));
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
    void tabCompletionChecksOnlyTheSupportedReloadPermission() {
        List<String> checkedPermissions = new ArrayList<>();
        CommandSender sender = sender(Set.of("hithealth.reload"), checkedPermissions, new ArrayList<>());
        assertEquals(List.of("on", "off", "reload"), new HitHealthCommand(null)
                .onTabComplete(sender, null, "hithealth", new String[] {""}));
        assertEquals(List.of("hithealth.reload"), checkedPermissions);
    }

    @Test
    void tabCompletionWithoutReloadPermissionOmitsReload() {
        CommandSender sender = sender(Set.of(), new ArrayList<>(), new ArrayList<>());
        HitHealthCommand command = new HitHealthCommand(null);
        assertEquals(List.of("on", "off"), command.onTabComplete(sender, null, "hithealth", new String[] {""}));
        assertEquals(List.of(), command.onTabComplete(sender, null, "hithealth", new String[] {"r"}));
        assertEquals(List.of(), command.onTabComplete(sender, null, "hithealth", new String[] {"on", ""}));
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
    void allSupportedLocalesUseTheCurrentBrandAndCommand() {
        for (Locale locale : List.of(Locale.ENGLISH, Locale.JAPANESE, Locale.SIMPLIFIED_CHINESE, Locale.KOREAN)) {
            Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(),
                    new Class<?>[] {Player.class}, (proxy, method, args) -> {
                        if (method.getName().equals("locale")) return locale;
                        throw new UnsupportedOperationException(method.getName());
                    });
            assertTrue(Messages.RELOADED.text(player).contains("HitHealth"));
            assertTrue(Messages.USAGE.text(player, "hithealth").contains("/hithealth [on|off]"));
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

    private static List<String> plain(List<Component> messages) {
        return messages.stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();
    }
}
