package dev.spa.hithealth;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import com.google.common.base.Functions;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 隔離Paperで、攻撃後のアクションバー文字列・色・倒したときの0表示・オンオフの保存を確かめる。
 * 実クライアントでの見た目（Java版・統合版）は別に確かめる。
 */
public final class HitHealthProbe extends JavaPlugin {

    private final List<Component> actionBars = new ArrayList<>();
    private final List<Component> messages = new ArrayList<>();
    private final UUID viewerId = UUID.randomUUID();
    private Locale viewerLocale = Locale.US;
    private Player viewer;
    private HitHealthPlugin hithealth;

    @Override
    public void onEnable() {
        hithealth = (HitHealthPlugin) Bukkit.getPluginManager().getPlugin("HitHealth");
        viewer = fakePlayer();
        Bukkit.getScheduler().runTaskLater(this, this::stepDamage, 40);
    }

    private void stepDamage() {
        try {
            World world = Bukkit.getWorlds().get(0);
            // プレイヤーがいないので、ゾンビを置くチャンクを読み込んだままにする
            world.getChunkAt(0, 0).addPluginChunkTicket(this);
            Zombie zombie = (Zombie) world.spawnEntity(new Location(world, 0, -60, 0), EntityType.ZOMBIE);
            zombie.setAI(false);
            zombie.setHealth(15);
            fire(zombie);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                try {
                    expect(actionBars.size() == 1, "攻撃後にアクションバーが1回出る");
                    expectBar(0, "15.0/20.0", TextColor.color(0x55FF55));
                    zombie.setHealth(4);
                    fire(zombie);
                    Zombie named = (Zombie) world.spawnEntity(new Location(world, 2, -60, 0), EntityType.ZOMBIE);
                    named.setAI(false);
                    named.customName(Component.text("ボス太郎"));
                    named.setHealth(0.03);
                    fire(named);
                    Zombie removed = (Zombie) world.spawnEntity(new Location(world, 4, -60, 0), EntityType.ZOMBIE);
                    removed.remove();
                    fire(removed);
                    Bukkit.getScheduler().runTaskLater(this, () -> stepToggle(zombie), 2);
                } catch (Throwable error) {
                    fail(error);
                }
            }, 2);
        } catch (Throwable error) {
            fail(error);
        }
    }

    private void stepToggle(Zombie zombie) {
        try {
            expect(actionBars.size() == 4, "4回の攻撃で4回出る: " + actionBars.size());
            expectBar(1, "4.0/20.0", TextColor.color(0xFF5555));
            expect(plain(actionBars.get(2)).startsWith("ボス太郎 "), "名前付きMobは付けた名前: " + plain(actionBars.get(2)));
            expect(plain(actionBars.get(2)).contains("0.1/20.0"), "端数は切り上げる: " + plain(actionBars.get(2)));
            expect(plain(actionBars.get(3)).contains("0.0/20.0"), "倒した相手は0: " + plain(actionBars.get(3)));

            HitHealthCommand command = new HitHealthCommand(hithealth);
            command.onCommand(viewer, null, "hithealth", new String[0]);
            expect(!hithealth.isEnabledFor(viewerId), "/hithealth でオフになる");
            expect(plain(messages.getLast()).equals("HP display for attacked targets is now off."),
                    "英語のクライアントには英語で返す: " + plain(messages.getLast()));
            String saved = Files.readString(hithealth.getDataFolder().toPath().resolve("players.yml"));
            expect(saved.contains(viewerId.toString()), "オフにした人が players.yml に残る");

            World world = zombie.getWorld();
            Zombie other = (Zombie) world.spawnEntity(new Location(world, 2, -60, 0), EntityType.ZOMBIE);
            fire(other);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                try {
                    expect(actionBars.size() == 4, "オフの間は出ない");
                    viewerLocale = Locale.JAPAN;
                    command.onCommand(viewer, null, "hithealth", new String[] {"on"});
                    expect(hithealth.isEnabledFor(viewerId), "/hithealth on でオンに戻る");
                    expect(plain(messages.getLast()).equals("攻撃した相手のHP表示をオンにしました。"),
                            "日本語のクライアントには日本語で返す: " + plain(messages.getLast()));
                    viewerLocale = Locale.SIMPLIFIED_CHINESE;
                    command.onCommand(viewer, null, "hithealth", new String[] {"bad"});
                    expect(plain(messages.getLast()).equals("用法：/hithealth [on|off]"),
                            "中国語のクライアントには中国語で返す: " + plain(messages.getLast()));
                    viewerLocale = Locale.KOREA;
                    command.onCommand(viewer, null, "hithealth", new String[] {"off"});
                    expect(plain(messages.getLast()).equals("공격한 대상의 HP 표시를 껐습니다."),
                            "韓国語のクライアントには韓国語で返す: " + plain(messages.getLast()));
                    getLogger().info("HITHEALTH_PROBE_PASS");
                } catch (Throwable error) {
                    fail(error);
                } finally {
                    Bukkit.shutdown();
                }
            }, 2);
        } catch (Throwable error) {
            fail(error);
            Bukkit.shutdown();
        }
    }

    private void fire(Entity target) {
        // 非推奨でないのはこの形だけ（ダメージは基礎値1、補正なし）
        Bukkit.getPluginManager().callEvent(new EntityDamageByEntityEvent(viewer, target,
                DamageCause.ENTITY_ATTACK, DamageSource.builder(DamageType.PLAYER_ATTACK).build(),
                new EnumMap<>(Map.of(DamageModifier.BASE, 1.0)),
                new EnumMap<>(Map.of(DamageModifier.BASE, Functions.<Double>identity())), false));
    }

    private void expectBar(int index, String text, TextColor color) {
        Component bar = actionBars.get(index);
        expect(plain(bar).contains(text), "表示に " + text + " が入る: " + plain(bar));
        boolean colored = bar.children().stream().anyMatch(child -> color.equals(child.color())
                && plain(child).contains(text));
        expect(colored, "HPの色が " + color.asHexString());
        getLogger().info("ok: " + plain(bar));
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static void expect(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void fail(Throwable error) {
        getLogger().log(Level.SEVERE, "HITHEALTH_PROBE_FAIL", error);
        Bukkit.shutdown();
    }

    private Player fakePlayer() {
        return (Player) Proxy.newProxyInstance(getClassLoader(), new Class<?>[] {Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "sendActionBar" -> {
                        actionBars.add((Component) args[0]);
                        yield null;
                    }
                    case "getUniqueId" -> viewerId;
                    case "getName" -> "Probe";
                    case "hasPermission", "isOnline" -> true;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "locale" -> viewerLocale;
                    case "sendMessage" -> {
                        if (args.length == 1 && args[0] instanceof Component message) {
                            messages.add(message);
                        }
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }
}
