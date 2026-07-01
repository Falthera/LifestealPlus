package dev.lifesteal.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface LifestealConfig {
    void reload();
    int getDefaultHearts();
    int getMaxHearts();
    double getHeartStealAmount();
    double getHeartCrystalAmount();
    long getHeartCrystalCooldownSeconds();
    @NotNull String getBanReason();
    boolean isBroadcastEnabled();
    @NotNull String getBroadcastMessage();
    long getReviveDelayTicks();
    int getReviveHeartsRestored();
    boolean isRevivalBroadcastEnabled();
    @NotNull String getRevivalBroadcastMessage();
    boolean isPlaceholderAPIEnabled();
    boolean isVaultEnabled();
    boolean isAsyncDatabase();
    boolean isHotReloadEnabled();
    @Nullable org.bukkit.configuration.ConfigurationSection getArchetypeConfig(@NotNull String archetypeId);
    @NotNull java.util.List<String> getDisabledWorlds();
    @NotNull java.util.List<String> getDisabledDimensions();
    boolean isWorldEnabled(@NotNull String worldName);
    boolean isDimensionEnabled(@NotNull String dimensionName);
    boolean isAntiOpAbuseEnabled();
    @NotNull String getAntiOpAbuseAnnounceTo();
    @NotNull java.util.List<String> getAntiOpAbuseExcludedCommands();
    @NotNull java.util.List<String> getAntiOpAbuseTrackedCommands();
    @NotNull String getAntiOpAbuseAnnouncementMessage();
    boolean isDiscordLeaderboardEnabled();
    @NotNull String getDiscordLeaderboardWebhookUrl();
    long getDiscordLeaderboardIntervalTicks();
    int getDiscordLeaderboardTop();
    @NotNull String getDiscordLeaderboardTitle();
    @NotNull org.bukkit.configuration.file.FileConfiguration getBukkitConfig();
}
