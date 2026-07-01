package dev.lifesteal.managers;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.ArchetypeManager;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.database.DatabaseManager;
import dev.lifesteal.utils.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LeaderboardManager {
    private final Lifesteal plugin;
    private final LifestealConfig config;
    private final DatabaseManager database;
    private final ArchetypeManager archetypeManager;
    private DiscordWebhook webhook;
    private int taskId = -1;

    public LeaderboardManager(@NotNull Lifesteal plugin, @NotNull DatabaseManager database,
                              @NotNull LifestealConfig config, @NotNull ArchetypeManager archetypeManager) {
        this.plugin = plugin;
        this.database = database;
        this.config = config;
        this.archetypeManager = archetypeManager;
        if (config.isDiscordLeaderboardEnabled() && !config.getDiscordLeaderboardWebhookUrl().isBlank()) {
            this.webhook = new DiscordWebhook(plugin, config.getDiscordLeaderboardWebhookUrl());
            startScheduler();
        }
    }

    private void startScheduler() {
        long interval = config.getDiscordLeaderboardIntervalTicks();
        if (interval <= 0) return;
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin) plugin, this::sendLeaderboard, interval, interval).getTaskId();
    }

    public void reload() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        if (config.isDiscordLeaderboardEnabled() && !config.getDiscordLeaderboardWebhookUrl().isBlank()) {
            this.webhook = new DiscordWebhook(plugin, config.getDiscordLeaderboardWebhookUrl());
            startScheduler();
        } else {
            this.webhook = null;
        }
    }

    public void sendLeaderboard() {
        if (webhook == null) return;
        int top = config.getDiscordLeaderboardTop();
        List<DatabaseManager.PlayerKillsRecord> records = database.getTopKillers(top);

        List<DiscordWebhook.LeaderboardEntry> entries = new ArrayList<>();
        for (DatabaseManager.PlayerKillsRecord record : records) {
            String name = resolveName(record.uuid());
            String archetype = resolveArchetype(record.uuid());
            entries.add(new DiscordWebhook.LeaderboardEntry(name, record.kills(), archetype));
        }

        webhook.sendLeaderboard(config.getDiscordLeaderboardTitle(), entries);
    }

    @NotNull
    private String resolveName(@NotNull UUID uuid) {
        Player online = ((Plugin) plugin).getServer().getPlayer(uuid);
        if (online != null) return online.getName();
        OfflinePlayer offline = plugin.getServer().getOfflinePlayer(uuid);
        if (offline.getName() != null) return offline.getName();
        return uuid.toString().substring(0, 8);
    }

    @NotNull
    private String resolveArchetype(@NotNull UUID uuid) {
        var a = archetypeManager.getArchetype(uuid);
        return a != null ? a.getName() : "None";
    }
}
