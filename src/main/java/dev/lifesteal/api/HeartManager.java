package dev.lifesteal.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface HeartManager {
    int getHearts(@NotNull UUID playerId);
    int getMaxHearts();
    int getDefaultHearts();
    double getHearts(@NotNull Player player);
    CompletableFuture<Void> setHearts(@NotNull UUID playerId, int amount);
    CompletableFuture<Void> addHearts(@NotNull UUID playerId, int amount);
    CompletableFuture<Void> removeHearts(@NotNull UUID playerId, int amount);
    void stealHeart(@NotNull UUID killerId, @NotNull UUID victimId);
    boolean hasReachedZeroHearts(@NotNull UUID playerId);
    boolean isDead(@NotNull UUID playerId);
    void onPlayerDeath(@NotNull UUID playerId, @NotNull UUID killerId);
    void onPlayerJoin(@NotNull Player player);
    void onPlayerQuit(@NotNull Player player);
    void loadPlayerData(@NotNull UUID playerId);
    CompletableFuture<Void> savePlayerData(@NotNull UUID playerId, boolean async);
    void loadAllOnline();
    void reloadConfig();
    void incrementKills(@NotNull UUID playerId);
    int getKills(@NotNull UUID playerId);
    void loadKills(@NotNull UUID playerId);
}