package dev.lifesteal.hearts;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.HeartManager;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.database.DatabaseManager;
import org.bukkit.BanList;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HeartManagerImpl implements HeartManager {
    private final Lifesteal plugin;
    private final DatabaseManager database;
    private final LifestealConfig config;
    private final Map<UUID, Integer> heartCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastSteal = new ConcurrentHashMap<>();
    private final AtomicInteger defaultHearts = new AtomicInteger();
    private final AtomicInteger maxHearts = new AtomicInteger();
    private final double stealAmount;
    private final Map<UUID, Integer> killCache = new ConcurrentHashMap<>();
    
    public HeartManagerImpl(@NotNull Lifesteal plugin, @NotNull DatabaseManager database, @NotNull LifestealConfig config) {
        this.plugin = plugin; this.database = database; this.config = config;
        this.stealAmount = config.getHeartStealAmount();
        this.defaultHearts.set(config.getDefaultHearts());
        this.maxHearts.set(config.getMaxHearts());
    }
    
    @Override public int getMaxHearts() { return maxHearts.get(); }
    @Override public int getDefaultHearts() { return defaultHearts.get(); }
    @Override public double getHearts(@NotNull Player player) { return heartCache.getOrDefault(player.getUniqueId(), defaultHearts.get()); }
    @Override public int getHearts(@NotNull UUID playerId) { return heartCache.getOrDefault(playerId, defaultHearts.get()); }
    
    @Override
    public CompletableFuture<Void> setHearts(@NotNull UUID playerId, int amount) {
        return CompletableFuture.runAsync(() -> {
            int clamped = Math.max(0, Math.min(maxHearts.get(), amount));
            heartCache.put(playerId, clamped);
            savePlayerData(playerId, true);
        }, database.getExecutor());
    }
    
    @Override
    public CompletableFuture<Void> addHearts(@NotNull UUID playerId, int amount) {
        int current = getHearts(playerId);
        return setHearts(playerId, Math.min(maxHearts.get(), current + amount));
    }
    
    @Override
    public CompletableFuture<Void> removeHearts(@NotNull UUID playerId, int amount) {
        int current = getHearts(playerId);
        int newAmount = Math.max(0, current - amount);
        if (newAmount == 0 && current > 0) return handleZeroHearts(playerId);
        return setHearts(playerId, newAmount);
    }
    
    private CompletableFuture<Void> handleZeroHearts(@NotNull UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            int oldHearts = heartCache.getOrDefault(playerId, defaultHearts.get());
            heartCache.put(playerId, 0);
            savePlayerData(playerId, true);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Player online = plugin.getServer().getPlayer(playerId);
                if (online == null || !online.isOnline()) return;
                plugin.getServer().getBanList(org.bukkit.BanList.Type.NAME).addBan(online.getName(), config.getBanReason(), null, null);
                online.kick(net.kyori.adventure.text.Component.text(config.getBanReason()));
                if (config.isBroadcastEnabled()) {
                    plugin.getServer().broadcast(net.kyori.adventure.text.Component.text(config.getBroadcastMessage().replace("%player%", online.getName())));
                }
            });
        }, database.getExecutor());
    }
    
    @Override
    public void stealHeart(@NotNull UUID killerId, @NotNull UUID victimId) {
        long now = System.currentTimeMillis();
        Long last = lastSteal.get(killerId);
        if (last != null && now - last < 1000) return;
        lastSteal.put(killerId, now);
        
        int victimHearts = getHearts(victimId);
        if (victimHearts <= 0) return;
        int killerHearts = getHearts(killerId);
        if (killerHearts >= maxHearts.get()) return;
        
        setHeartsSync(victimId, Math.max(0, victimHearts - (int) stealAmount));
        addHeartsSync(killerId, 1);
        
        Player victimOnline = plugin.getServer().getPlayer(victimId);
        Player killerOnline = plugin.getServer().getPlayer(killerId);
        
        if (killerOnline != null && killerOnline.isOnline()) {
            killerOnline.playSound(killerOnline.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        }
        if (victimOnline != null && victimOnline.isOnline()) {
            victimOnline.playSound(victimOnline.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
        }
    }
    
    private void setHeartsSync(@NotNull UUID playerId, int amount) {
        int clamped = Math.max(0, Math.min(maxHearts.get(), amount));
        heartCache.put(playerId, clamped);
        try (Connection conn = ((dev.lifesteal.database.DatabaseManager) database).getDataSource().getConnection();
             var ps = conn.prepareStatement(storageType.equalsIgnoreCase("sqlite")
                 ? "INSERT OR REPLACE INTO player_hearts (uuid, hearts) VALUES (?, ?)"
                 : "INSERT INTO player_hearts (uuid, hearts) VALUES (?, ?) ON DUPLICATE KEY UPDATE hearts = VALUES(hearts)")) {
            ps.setString(1, playerId.toString());
            ps.setInt(2, clamped);
            ps.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save hearts for " + playerId + ": " + e.getMessage());
        }
    }
    
    private void addHeartsSync(@NotNull UUID playerId, int amount) {
        int current = getHearts(playerId);
        setHeartsSync(playerId, Math.min(maxHearts.get(), current + amount));
    }
    
    @Override public boolean hasReachedZeroHearts(@NotNull UUID playerId) { return isDead(playerId); }
    @Override public boolean isDead(@NotNull UUID playerId) { return heartCache.getOrDefault(playerId, defaultHearts.get()) <= 0; }
    
    @Override
    public void onPlayerDeath(@NotNull UUID playerId, @NotNull UUID killerId) {
        if (plugin.getServer().getBanList(BanList.Type.NAME).getBanEntry(plugin.getServer().getOfflinePlayer(playerId).getName()) != null) return;
        stealHeart(killerId, playerId);
    }
    
    @Override public void onPlayerJoin(@NotNull Player player) { 
        loadPlayerData(player.getUniqueId()); 
        loadKills(player.getUniqueId());
    }
    @Override public void onPlayerQuit(@NotNull Player player) { 
        savePlayerData(player.getUniqueId(), true); 
        heartCache.remove(player.getUniqueId());
        killCache.remove(player.getUniqueId());
    }
    
    @Override
    public void loadPlayerData(@NotNull UUID playerId) {
        CompletableFuture.supplyAsync(() -> database.loadHearts(playerId), database.getExecutor())
            .thenAcceptAsync(hearts -> heartCache.put(playerId, hearts), plugin.getServer().getScheduler().getMainThreadExecutor(plugin));
    }
    
    @Override
    public CompletableFuture<Void> savePlayerData(@NotNull UUID playerId, boolean async) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> database.saveHearts(playerId, heartCache.getOrDefault(playerId, defaultHearts.get())), database.getExecutor());
        if (!async) {
            try { future.join(); } catch (CompletionException e) { throw new RuntimeException(e); }
        }
        return async ? future : CompletableFuture.completedFuture(null);
    }
    
    public void loadAllOnline() { 
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            loadPlayerData(p.getUniqueId());
            loadKills(p.getUniqueId());
        }
    }
    public void reloadConfig() { defaultHearts.set(config.getDefaultHearts()); maxHearts.set(config.getMaxHearts()); }
    
    @Override
    public void incrementKills(@NotNull UUID playerId) {
        killCache.merge(playerId, 1, Integer::sum);
        CompletableFuture.runAsync(() -> database.incrementKills(playerId, 1), database.getExecutor());
    }
    
    @Override
    public int getKills(@NotNull UUID playerId) {
        return killCache.getOrDefault(playerId, 0);
    }
    
    @Override
    public void loadKills(@NotNull UUID playerId) {
        CompletableFuture.supplyAsync(() -> database.loadKills(playerId), database.getExecutor())
            .thenAcceptAsync(kills -> killCache.put(playerId, kills), plugin.getServer().getScheduler().getMainThreadExecutor(plugin));
    }
}
