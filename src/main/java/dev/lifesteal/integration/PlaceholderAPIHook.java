package dev.lifesteal.integration;

import dev.lifesteal.Lifesteal;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final Lifesteal plugin;
    
    public PlaceholderAPIHook(@NotNull Lifesteal plugin) { this.plugin = plugin; }
    
    @Override
    public @NotNull String getIdentifier() { return "lifesteal"; }
    @Override
    public @NotNull String getAuthor() { return "Lifesteal+"; }
    @Override
    public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
    
    @Override
    public boolean canRegister() { return true; }
    @Override
    public boolean persist() { return true; }
    
    @Override
    public @Nullable String onPlaceholderRequest(org.bukkit.entity.Player player, @NotNull String params) {
        if (player == null) return "";
        return switch (params.toLowerCase()) {
            case "hearts" -> String.valueOf(plugin.getHeartManager().getHearts(player));
            case "maxhearts" -> String.valueOf(plugin.getHeartManager().getMaxHearts());
            case "dead" -> plugin.getHeartManager().isDead(player.getUniqueId()) ? "true" : "false";
            case "archetype" -> {
                var a = plugin.getArchetypeManager().getArchetype(player);
                yield a != null ? a.getName() : "None";
            }
            case "kills" -> "0"; // placeholder for future kill tracking
            case "deaths" -> "0"; // placeholder for future death tracking
            default -> null;
        };
    }
}
