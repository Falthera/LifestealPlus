package dev.lifesteal.utils;

import dev.lifesteal.Lifesteal;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class DiscordWebhook {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private final String url;
    private final Lifesteal plugin;
    
    public DiscordWebhook(@NotNull Lifesteal plugin, @NotNull String url) {
        this.plugin = plugin;
        this.url = url;
    }
    
    public void sendLeaderboard(@NotNull String title, @NotNull List<LeaderboardEntry> entries) {
        if (url == null || url.isBlank() || entries.isEmpty()) return;
        
        StringBuilder json = new StringBuilder();
        json.append("{\"embeds\":[{");
        json.append("\"title\":\"").append(escapeJson(title)).append("\",");
        json.append("\"color\":16711680,");
        json.append("\"footer\":{\"text\":\"Lifesteal+ Leaderboard\"},");
        json.append("\"fields\":[");
        
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry e = entries.get(i);
            if (i > 0) json.append(",");
            json.append("{\"name\":\"#").append(i + 1).append(" ").append(escapeJson(e.playerName())).append("\",");
            json.append("\"value\":\"Kills: ").append(e.kills()).append(" | Archetype: ").append(escapeJson(e.archetype())).append("\",");
            json.append("\"inline\":false}");
        }
        
        json.append("]}]}");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        
        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int code = response.statusCode();
                    if (code >= 400) {
                        plugin.getLogger().warning("Discord webhook failed with HTTP " + code + ": " + response.body());
                    }
                })
                .exceptionally(ex -> {
                    if (ex.getMessage() != null && !ex.getMessage().contains("Connection closed")) {
                        plugin.getLogger().warning("Failed to send Discord webhook: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                    }
                    return null;
                });
    }
    
    private static String escapeJson(@NotNull String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    public record LeaderboardEntry(@NotNull String playerName, int kills, @NotNull String archetype) {}
}
