package dev.lifesteal.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface LifestealConfig {
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
    @Nullable ConfigurationSection getArchetypeConfig(@NotNull String archetypeId);
    @NotNull List<String> getDisabledWorlds();
    @NotNull List<String> getDisabledDimensions();
    boolean isWorldEnabled(@NotNull String worldName);
    boolean isDimensionEnabled(@NotNull String dimensionName);
    boolean isAntiOpAbuseEnabled();
    @NotNull String getAntiOpAbuseAnnounceTo();
    @NotNull List<String> getAntiOpAbuseExcludedCommands();
    @NotNull List<String> getAntiOpAbuseTrackedCommands();
    @NotNull String getAntiOpAbuseAnnouncementMessage();
    boolean isDiscordLeaderboardEnabled();
    @NotNull String getDiscordLeaderboardWebhookUrl();
    long getDiscordLeaderboardIntervalTicks();
    int getDiscordLeaderboardTop();
    @NotNull String getDiscordLeaderboardTitle();
    void reload();
    @NotNull FileConfiguration getBukkitConfig();
}
