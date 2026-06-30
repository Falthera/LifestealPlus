package dev.lifesteal.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RevivalManager {
    boolean isRevived(@NotNull UUID playerId);
    boolean isReviving(@NotNull UUID reviverId);
    boolean canRevive(@NotNull Player reviver, @NotNull Player target);
    CompletableFuture<Boolean> revivePlayer(@NotNull Player reviver, @NotNull Player target);
    CompletableFuture<Boolean> reviveOfflinePlayer(@NotNull Player reviver, @NotNull UUID targetId);
}