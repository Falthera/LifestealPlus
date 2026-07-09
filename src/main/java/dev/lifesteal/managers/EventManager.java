package dev.lifesteal.managers;

import dev.lifesteal.Lifesteal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {
    private final Lifesteal plugin;
    private final Map<UUID, Integer> assassinKills = new ConcurrentHashMap<>();
    private boolean active = false;
    private BukkitTask broadcastTask;
    private BukkitTask endTask;
    private BukkitTask effectTask;
    private BukkitTask targetRotationTask;
    private UUID currentTarget;
    private long startTime;
    private static final int DURATION_SECONDS = 1800;
    private static final int BROADCAST_INTERVAL_TICKS = 400;
    private static final int EFFECT_INTERVAL_TICKS = 100;
    private static final int TARGET_ROTATION_INTERVAL_TICKS = 6000;

    public EventManager(@NotNull Lifesteal plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() { return active; }

    public void startAssassinChase(@NotNull CommandSender sender) {
        if (active) {
            sender.sendMessage(Component.text("Assassin Chase is already active!").color(NamedTextColor.RED));
            return;
        }
        active = true;
        startTime = System.currentTimeMillis();
        assassinKills.clear();
        List<Player> online = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        if (online.isEmpty()) {
            sender.sendMessage(Component.text("No players online to target.").color(NamedTextColor.RED));
            active = false;
            return;
        }
        currentTarget = online.get(new Random().nextInt(online.size())).getUniqueId();
        plugin.getServer().broadcast(Component.text("[Assassin Chase] The hunt has begun! Locations will leak every 20 seconds. Target rotates every 5 minutes. Event lasts 30 minutes!").color(NamedTextColor.DARK_RED));
        plugin.getServer().broadcast(Component.text("[Assassin Chase] First target: " + plugin.getServer().getPlayer(currentTarget).getName()).color(NamedTextColor.GOLD));
        broadcastTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::broadcastTargetLocation, 0L, BROADCAST_INTERVAL_TICKS);
        effectTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::applyAssassinEffects, 0L, EFFECT_INTERVAL_TICKS);
        targetRotationTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::rotateTarget, 0L, TARGET_ROTATION_INTERVAL_TICKS);
        endTask = plugin.getServer().getScheduler().runTaskLater(plugin, this::endAssassinChase, DURATION_SECONDS * 20L);
    }

    public void stopAssassinChase() {
        if (!active) return;
        endAssassinChase();
    }

    private void endAssassinChase() {
        if (!active) return;
        active = false;
        if (broadcastTask != null) { broadcastTask.cancel(); broadcastTask = null; }
        if (endTask != null) { endTask.cancel(); endTask = null; }
        if (effectTask != null) { effectTask.cancel(); effectTask = null; }
        if (targetRotationTask != null) { targetRotationTask.cancel(); targetRotationTask = null; }
        String winner = null;
        int topKills = 0;
        for (Map.Entry<UUID, Integer> entry : assassinKills.entrySet()) {
            if (entry.getValue() > topKills) {
                topKills = entry.getValue();
                winner = plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
            }
        }
        plugin.getServer().broadcast(Component.text("[Assassin Chase] The event has ended!").color(NamedTextColor.DARK_RED));
        if (winner != null && topKills > 0) {
            Player winnerPlayer = plugin.getServer().getPlayer(winner);
            plugin.getServer().broadcast(Component.text("[Assassin Chase] WINNER: " + winner + " with " + topKills + " target kills!").color(NamedTextColor.GREEN));
            plugin.getServer().broadcast(Component.text("[Assassin Chase] " + winner + " has been crowned the ultimate assassin!").color(NamedTextColor.GOLD));
            if (winnerPlayer != null && winnerPlayer.isOnline()) {
                winnerPlayer.sendMessage(Component.text("[Assassin Chase] You are the WINNER! Reward: +3 Hearts!").color(NamedTextColor.GREEN));
                winnerPlayer.playSound(winnerPlayer.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                winnerPlayer.sendTitle(Component.text("ASSASSIN CHASE WINNER"), Component.text("+" + topKills + " kills | +3 Hearts"), 10, 80, 10));
                plugin.getHeartManager().addHearts(winnerPlayer.getUniqueId(), 3);
            }
        }
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.removePotionEffect(PotionEffectType.SPEED);
            online.removePotionEffect(PotionEffectType.STRENGTH);
            online.removePotionEffect(PotionEffectType.INVISIBILITY);
            online.removePotionEffect(PotionEffectType.GLOWING);
        }
    }

    private void broadcastTargetLocation() {
        if (!active || currentTarget == null) return;
        Player target = plugin.getServer().getPlayer(currentTarget);
        if (target == null || !target.isOnline()) {
            pickNewTarget();
            return;
        }
        plugin.getServer().broadcast(Component.text("[Assassin Chase] TARGET LEAKED: " + target.getName() + " @ " + formatLocation(target.getLocation())).color(NamedTextColor.RED));
    }

    private void pickNewTarget() {
        List<Player> online = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        if (online.isEmpty()) return;
        currentTarget = online.get(new Random().nextInt(online.size())).getUniqueId();
        plugin.getServer().broadcast(Component.text("[Assassin Chase] New target: " + plugin.getServer().getPlayer(currentTarget).getName()).color(NamedTextColor.GOLD));
    }

    private void rotateTarget() {
        if (!active) return;
        pickNewTarget();
        plugin.getServer().broadcast(Component.text("[Assassin Chase] Target rotated! New target: " + plugin.getServer().getPlayer(currentTarget).getName()).color(NamedTextColor.RED));
    }

    public void onAssassinKill(@NotNull Player killer, @NotNull Player victim) {
        if (!active) return;
        if (!isAssassin(killer)) return;
        if (!victim.getUniqueId().equals(currentTarget)) return;
        assassinKills.merge(killer.getUniqueId(), 1, Integer::sum);
        plugin.getServer().broadcast(Component.text("[Assassin Chase] " + killer.getName() + " eliminated the target!").color(NamedTextColor.YELLOW));
        pickNewTarget();
    }

    private void applyAssassinEffects() {
        if (!active) return;
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (isAssassin(online)) {
                if (online.getPotionEffect(PotionEffectType.SPEED) == null || online.getPotionEffect(PotionEffectType.SPEED).getDuration() < 40) {
                    online.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1, true, false));
                }
                if (online.getPotionEffect(PotionEffectType.STRENGTH) == null || online.getPotionEffect(PotionEffectType.STRENGTH).getDuration() < 40) {
                    online.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, 0, true, false));
                }
            }
        }
    }

    private boolean isAssassin(@NotNull Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("assassin");
    }

    private String formatLocation(@NotNull org.bukkit.Location loc) {
        return loc.getWorld().getName() + " | X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ();
    }
}
