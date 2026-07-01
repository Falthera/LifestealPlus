package dev.lifesteal.hearts;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.HeartManager;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.database.DatabaseManager;
import dev.lifesteal.events.PlayerGainHeartEvent;
import dev.lifesteal.events.PlayerLoseHeartEvent;
import dev.lifesteal.events.PlayerPermanentDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.BanList;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HeartManagerImpl implements HeartManager {
    private final Lifesteal plugin;
    private final DatabaseManager database;
    private final LifestealConfig config;
    private final Map<UUID, Integer> heartCache = new ConcurrentHashMap<>();
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
        int clamped = Math.max(0, Math.min(maxHearts.get(), amount));
        heartCache.put(playerId, clamped);
        if (clamped == 0) return handleZeroHearts(playerId);
        return savePlayerData(playerId, true).thenRunAsync(() -> {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                updateMaxHealth(player, clamped);
            }
        }, plugin.getServer().getScheduler().getMainThreadExecutor(plugin));
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int victimHearts = heartCache.getOrDefault(victimId, defaultHearts.get());
            int killerHearts = heartCache.getOrDefault(killerId, defaultHearts.get());
            
            if (victimHearts <= 0) return;
            
            int newVictimHearts = Math.max(0, victimHearts - (int) stealAmount);
            heartCache.put(victimId, newVictimHearts);
            database.saveHearts(victimId, newVictimHearts);
            
            if (killerHearts < maxHearts.get()) {
                int newKillerHearts = Math.min(maxHearts.get(), killerHearts + (int) stealAmount);
                heartCache.put(killerId, newKillerHearts);
                database.saveHearts(killerId, newKillerHearts);
            }
            
            Bukkit.getScheduler().getMainThreadExecutor(plugin).execute(() -> {
                Player victimOnline = plugin.getServer().getPlayer(victimId);
                Player killerOnline = plugin.getServer().getPlayer(killerId);
                
                if (killerOnline != null && killerOnline.isOnline()) {
                    updatePlayerHealth(killerOnline);
                    plugin.getServer().getPluginManager().callEvent(new PlayerGainHeartEvent(killerOnline, (int) stealAmount, heartCache.getOrDefault(killerId, defaultHearts.get())));
                    killerOnline.playSound(killerOnline.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
                }
                if (victimOnline != null && victimOnline.isOnline()) {
                    updatePlayerHealth(victimOnline);
                    plugin.getServer().getPluginManager().callEvent(new PlayerLoseHeartEvent(victimOnline, (int) stealAmount, heartCache.getOrDefault(victimId, defaultHearts.get())));
                    victimOnline.playSound(victimOnline.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
                    if (newVictimHearts <= 0) {
                        plugin.getServer().getPluginManager().callEvent(new PlayerPermanentDeathEvent(victimOnline, 0));
                        plugin.getServer().getBanList(BanList.Type.NAME).addBan(victimOnline.getName(), config.getBanReason(), null, null);
                        victimOnline.kick(net.kyori.adventure.text.Component.text(config.getBanReason()));
                        if (config.isBroadcastEnabled()) {
                            plugin.getServer().broadcast(net.kyori.adventure.text.Component.text(config.getBroadcastMessage().replace("%player%", victimOnline.getName())));
                        }
                    }
                }
            });
        });
    }
    
    private void updatePlayerHealth(@NotNull Player player) {
        int hearts = heartCache.getOrDefault(player.getUniqueId(), defaultHearts.get());
        updateMaxHealth(player, hearts);
        double health = hearts * 2.0;
        player.setHealth(Math.min(health, player.getMaxHealth()));
    }
    
    private void updateMaxHealth(@NotNull Player player, int hearts) {
        player.setMaxHealth(hearts * 2.0);
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
        savePlayerData(player.getUniqueId(), false); 
        heartCache.remove(player.getUniqueId());
        killCache.remove(player.getUniqueId());
    }
    
    @Override
    public void loadPlayerData(@NotNull UUID playerId) {
        CompletableFuture.supplyAsync(() -> database.loadHearts(playerId), database.getExecutor())
            .thenAcceptAsync(hearts -> {
                heartCache.put(playerId, hearts);
                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    updateMaxHealth(player, hearts);
                    double health = Math.min(hearts * 2.0, player.getMaxHealth());
                    player.setHealth(health);
                }
            }, plugin.getServer().getScheduler().getMainThreadExecutor(plugin));
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
