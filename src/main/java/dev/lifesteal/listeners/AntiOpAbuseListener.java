package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.LifestealConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class AntiOpAbuseListener implements Listener {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final Lifesteal plugin;
    private final LifestealConfig config;
    private final Set<String> excludedCommands = new HashSet<>();
    private final Set<String> trackedCommands = new HashSet<>();
    
    public AntiOpAbuseListener(@NotNull Lifesteal plugin, @NotNull LifestealConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.excludedCommands.addAll(config.getAntiOpAbuseExcludedCommands());
        this.trackedCommands.addAll(config.getAntiOpAbuseTrackedCommands());
    }
    
    @EventHandler
    public void onPlayerCommand(@NotNull PlayerCommandPreprocessEvent event) {
        if (!config.isAntiOpAbuseEnabled()) return;
        Player player = event.getPlayer();
        if (!player.isOp()) return;
        
        String fullCommand = event.getMessage();
        String baseCommand = extractBaseCommand(fullCommand);
        if (!shouldTrack(baseCommand)) return;
        
        String senderName = player.getName();
        String message = config.getAntiOpAbuseAnnouncementMessage()
            .replace("<sender>", senderName)
            .replace("<command>", fullCommand);
        broadcast(MINI_MESSAGE.deserialize(message));
    }
    
    @EventHandler
    public void onServerCommand(@NotNull ServerCommandEvent event) {
        if (!config.isAntiOpAbuseEnabled()) return;
        String command = event.getCommand();
        if (command == null || command.isBlank()) return;
        
        String baseCommand = extractBaseCommand(command);
        if (!shouldTrack(baseCommand)) return;
        
        String message = config.getAntiOpAbuseAnnouncementMessage()
            .replace("<sender>", "CONSOLE")
            .replace("<command>", command);
        broadcast(MINI_MESSAGE.deserialize(message));
    }
    
    private boolean shouldTrack(@Nullable String baseCommand) {
        if (baseCommand == null) return false;
        String lower = baseCommand.toLowerCase(Locale.ROOT);
        if (excludedCommands.contains(lower)) return false;
        if (trackedCommands.isEmpty()) return true;
        return trackedCommands.contains(lower);
    }
    
    private void broadcast(@NotNull Component component) {
        String announceTo = config.getAntiOpAbuseAnnounceTo();
        if ("all".equalsIgnoreCase(announceTo)) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                p.sendMessage(component);
            }
            plugin.getServer().getConsoleSender().sendMessage(component);
        } else if ("ops".equalsIgnoreCase(announceTo)) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.isOp()) {
                    p.sendMessage(component);
                }
            }
            plugin.getServer().getConsoleSender().sendMessage(component);
        }
    }
    
    @NotNull
    private String extractBaseCommand(@NotNull String fullCommand) {
        String trimmed = fullCommand.trim();
        if (trimmed.startsWith("/")) trimmed = trimmed.substring(1);
        String[] parts = trimmed.split("\\s+");
        return parts.length > 0 ? parts[0] : "";
    }
}
