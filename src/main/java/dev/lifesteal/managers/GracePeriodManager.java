package dev.lifesteal.managers;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.database.DatabaseManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GracePeriodManager {
    private final Lifesteal plugin;
    private final LifestealConfig config;
    private final DatabaseManager database;
    private final Map<UUID, Long> graceEndTimes = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> updateTasks = new ConcurrentHashMap<>();

    public GracePeriodManager(@NotNull Lifesteal plugin, @NotNull DatabaseManager database, @NotNull LifestealConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.database = database;
    }

    public void startGracePeriod(@NotNull UUID playerId) {
        long durationMs = config.getGracePeriodDurationSeconds() * 1000L;
        long endTime = System.currentTimeMillis() + durationMs;
        graceEndTimes.put(playerId, endTime);
        database.saveGracePeriodEnd(playerId, endTime);
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            startBossBar(player, endTime);
            player.sendMessage(Component.text("Grace period started! You are immune for " + formatTime(durationMs) + ".").color(NamedTextColor.GREEN));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        }
    }

    public void endGracePeriod(@NotNull UUID playerId) {
        graceEndTimes.remove(playerId);
        database.clearGracePeriod(playerId);
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            removeBossBar(player);
            player.sendMessage(Component.text("Grace period has ended!").color(NamedTextColor.RED));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
        }
        BukkitTask task = updateTasks.remove(playerId);
        if (task != null) task.cancel();
    }

    public boolean isInGracePeriod(@NotNull UUID playerId) {
        Long endTime = graceEndTimes.get(playerId);
        if (endTime == null) return false;
        if (System.currentTimeMillis() > endTime) {
            graceEndTimes.remove(playerId);
            database.clearGracePeriod(playerId);
            removeBossBar(plugin.getServer().getPlayer(playerId));
            return false;
        }
        return true;
    }

    public long getRemainingSeconds(@NotNull UUID playerId) {
        Long endTime = graceEndTimes.get(playerId);
        if (endTime == null) return 0;
        long remaining = (endTime - System.currentTimeMillis()) / 1000L;
        return Math.max(0, remaining);
    }

    public void loadGracePeriod(@NotNull UUID playerId) {
        long endTime = database.loadGracePeriodEnd(playerId);
        if (endTime > System.currentTimeMillis()) {
            graceEndTimes.put(playerId, endTime);
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                startBossBar(player, endTime);
            }
        } else if (endTime > 0) {
            database.clearGracePeriod(playerId);
        }
    }

    public void clearCache(@NotNull UUID playerId) {
        graceEndTimes.remove(playerId);
        removeBossBar(plugin.getServer().getPlayer(playerId));
        BukkitTask task = updateTasks.remove(playerId);
        if (task != null) task.cancel();
    }

    private void startBossBar(@NotNull Player player, long endTime) {
        removeBossBar(player);
        BossBar.Color color = BossBar.Color.RED;
        BossBar bossBar = BossBar.bossBar(Component.text("Grace Period"), 1.0f, color, BossBar.Overlay.PROGRESS);
        player.showBossBar(bossBar);
        bossBars.put(player.getUniqueId(), bossBar);
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                clearCache(player.getUniqueId());
                return;
            }
            long now = System.currentTimeMillis();
            long remaining = endTime - now;
            if (remaining <= 0) {
                endGracePeriod(player.getUniqueId());
                return;
            }
            float progress = (float) remaining / (config.getGracePeriodDurationSeconds() * 1000L);
            bossBar.progress(progress);
            bossBar.name(Component.text(config.getGracePeriodBossBarTitle().replace("<time>", formatTime(remaining))).color(NamedTextColor.RED));
        }, 0L, 20L);
        updateTasks.put(player.getUniqueId(), task);
    }

    private void removeBossBar(@Nullable Player player) {
        if (player == null) return;
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) {
            player.hideBossBar(bar);
        }
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000L;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
