package dev.lifesteal.managers;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.database.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {
    private final Lifesteal plugin;
    private final LifestealConfig config;
    private final DatabaseManager database;
    private final Map<UUID, Long> combatTags = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> tagRemovalTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> trustCache = new ConcurrentHashMap<>();

    public CombatManager(@NotNull Lifesteal plugin, @NotNull DatabaseManager database, @NotNull LifestealConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.database = database;
    }

    public void tagPlayer(@NotNull UUID playerId) {
        if (!config.isCombatLogEnabled()) return;
        long durationMs = config.getCombatLogDurationSeconds() * 1000L;
        long now = System.currentTimeMillis();
        combatTags.put(playerId, now + durationMs);
        BukkitTask existing = tagRemovalTasks.remove(playerId);
        if (existing != null) existing.cancel();
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            combatTags.remove(playerId);
            tagRemovalTasks.remove(playerId);
        }, durationMs / 50L);
        tagRemovalTasks.put(playerId, task);
    }

    public boolean isInCombat(@NotNull UUID playerId) {
        Long expiry = combatTags.get(playerId);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            combatTags.remove(playerId);
            return false;
        }
        return true;
    }

    public void removeTag(@NotNull UUID playerId) {
        combatTags.remove(playerId);
        BukkitTask task = tagRemovalTasks.remove(playerId);
        if (task != null) task.cancel();
    }

    public void removeTagOnLogout(@NotNull UUID playerId) {
        removeTag(playerId);
    }

    public boolean isTrusted(@NotNull UUID attackerId, @NotNull UUID victimId) {
        if (!config.isTrustEnabled()) return false;
        Set<UUID> trusted = trustCache.get(victimId);
        if (trusted == null) {
            trusted = new HashSet<>(database.loadAllTrusted(victimId));
            trustCache.put(victimId, trusted);
        }
        return trusted.contains(attackerId);
    }

    public void addTrust(@NotNull UUID playerId, @NotNull UUID trustedId) {
        trustCache.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(trustedId);
        database.saveTrust(playerId, trustedId);
    }

    public void removeTrust(@NotNull UUID playerId, @NotNull UUID trustedId) {
        Set<UUID> trusted = trustCache.get(playerId);
        if (trusted != null) trusted.remove(trustedId);
        database.removeTrust(playerId, trustedId);
    }

    public void clearCache(@NotNull UUID playerId) {
        trustCache.remove(playerId);
        removeTag(playerId);
    }
}
