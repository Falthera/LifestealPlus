package dev.lifesteal.api;

import dev.lifesteal.archetypes.Archetype;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ArchetypeManager {
    @Nullable Archetype getArchetype(@NotNull UUID playerId);
    @Nullable Archetype getArchetype(@NotNull Player player);
    CompletableFuture<Void> setArchetype(@NotNull UUID playerId, @NotNull Archetype archetype);
    CompletableFuture<Void> setArchetype(@NotNull Player player, @NotNull Archetype archetype);
    boolean hasArchetype(@NotNull UUID playerId);
    boolean needsArchetypeSelection(@NotNull Player player);
    boolean canSelectArchetype(@NotNull Player player);
    @NotNull List<Archetype> getAllArchetypes();
    @NotNull Archetype getRandomArchetype();
    void onPlayerJoin(@NotNull Player player);
    void loadPlayerData(@NotNull UUID playerId);
    CompletableFuture<Void> savePlayerData(@NotNull UUID playerId);
    void reload();
    void loadAllOnline();
    void applyArchetypeEffects(@NotNull Player player);
}
