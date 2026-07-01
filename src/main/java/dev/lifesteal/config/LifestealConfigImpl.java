package dev.lifesteal.config;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.LifestealConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LifestealConfigImpl implements LifestealConfig {
    private final Lifesteal plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    
    public LifestealConfigImpl(@NotNull Lifesteal plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    private void loadMessages() {
        File file = new File(((Plugin) plugin).getDataFolder(), "messages.yml");
        if (!file.exists()) {
            ((Plugin) plugin).saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }
    
    @Override
    public void reload() {
        ((Plugin) plugin).reloadConfig();
        this.config = ((Plugin) plugin).getConfig();
        loadMessages();
    }
    
    @Override public int getDefaultHearts() { return config.getInt("settings.default-hearts", 10); }
    @Override public int getMaxHearts() { return config.getInt("settings.max-hearts", 20); }
    @Override public double getHeartStealAmount() { return config.getDouble("settings.heart-steal-amount", 1.0); }
    @Override public double getHeartCrystalAmount() { return config.getDouble("settings.heart-crystal-amount", 1.0); }
    @Override public long getHeartCrystalCooldownSeconds() { return config.getLong("settings.heart-crystal-cooldown-seconds", 0); }
    @Override public int getWithdrawHeartAmount() { return config.getInt("settings.withdraw-heart-amount", 1); }
    @Override @NotNull public String getBanReason() { return config.getString("settings.ban-reason", "&cYou ran out of hearts!"); }
    @Override public boolean isBroadcastEnabled() { return config.getBoolean("settings.broadcast-death.enabled", true); }
    @Override @NotNull public String getBroadcastMessage() { return config.getString("settings.broadcast-death.message", "<red><player> has run out of hearts!"); }
    @Override public long getReviveDelayTicks() { return config.getLong("settings.revival-delay-ticks", 40L); }
    @Override public int getReviveHeartsRestored() { return config.getInt("settings.revival-hearts-restored", 3); }
    @Override public boolean isRevivalBroadcastEnabled() { return config.getBoolean("settings.revival-broadcast.enabled", true); }
    @Override @NotNull public String getRevivalBroadcastMessage() { return config.getString("settings.revival-broadcast.message", "<green><player> has been revived by <reviver>!"); }
    @Override public boolean isPlaceholderAPIEnabled() { return config.getBoolean("integrations.placeholderapi", true); }
    @Override public boolean isVaultEnabled() { return config.getBoolean("integrations.vault", false); }
    @Override public boolean isAsyncDatabase() { return config.getBoolean("database.async", true); }
    @Override public boolean isHotReloadEnabled() { return config.getBoolean("settings.hot-reload", false); }
    @Override @Nullable public org.bukkit.configuration.ConfigurationSection getArchetypeConfig(@NotNull String archetypeId) { return config.getConfigurationSection("archetypes." + archetypeId); }
    @Override @NotNull public List<String> getDisabledWorlds() { List<String> w = config.getStringList("disabled-worlds"); return w != null ? w : new ArrayList<>(); }
    @Override @NotNull public List<String> getDisabledDimensions() { List<String> d = config.getStringList("disabled-dimensions"); return d != null ? d : new ArrayList<>(); }
    @Override public boolean isWorldEnabled(@NotNull String worldName) { return !getDisabledWorlds().contains(worldName); }
    @Override public boolean isDimensionEnabled(@NotNull String dimensionName) { return !getDisabledDimensions().contains(dimensionName); }
    @Override public boolean isAntiOpAbuseEnabled() { return config.getBoolean("anti-op-abuse.enabled", true); }
    @Override @NotNull public String getAntiOpAbuseAnnounceTo() { return config.getString("anti-op-abuse.announce-to", "all"); }
    @Override @NotNull public List<String> getAntiOpAbuseExcludedCommands() { var list = config.getStringList("anti-op-abuse.exclude-commands"); return list != null ? list : new ArrayList<>(); }
    @Override @NotNull public List<String> getAntiOpAbuseTrackedCommands() { var list = config.getStringList("anti-op-abuse.tracked-commands"); return list != null ? list : new ArrayList<>(); }
    @Override @NotNull public String getAntiOpAbuseAnnouncementMessage() { return messages.getString("anti-op-abuse.announcement", "<red>[AntiOpAbuse]</red> <gold><sender></gold> executed: <white><command></white>"); }
    @Override public boolean isDiscordLeaderboardEnabled() { return config.getBoolean("discord-leaderboard.enabled", false); }
    @Override @NotNull public String getDiscordLeaderboardWebhookUrl() { return config.getString("discord-leaderboard.webhook-url", ""); }
    @Override public long getDiscordLeaderboardIntervalTicks() { return config.getLong("discord-leaderboard.interval-ticks", 6000L); }
    @Override public int getDiscordLeaderboardTop() { return Math.min(10, Math.max(1, config.getInt("discord-leaderboard.top", 10))); }
    @Override @NotNull public String getDiscordLeaderboardTitle() { return config.getString("discord-leaderboard.title", "Lifesteal+ Top Killers"); }
    @Override @NotNull public FileConfiguration getBukkitConfig() { return config; }
    
    @NotNull
    public String getMessage(@NotNull String key, @NotNull String def) {
        return messages.getString(key, def);
    }
    
    @NotNull
    public net.kyori.adventure.text.Component getComponent(@NotNull String key, @NotNull net.kyori.adventure.text.Component def) {
        String raw = messages.getString(key);
        if (raw == null) return def;
        return miniMessage.deserialize(raw);
    }
}
