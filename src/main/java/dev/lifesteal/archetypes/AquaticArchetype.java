package dev.lifesteal.archetypes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class AquaticArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    
    public AquaticArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Effects are applied permanently by ArchetypeManagerImpl.applyArchetypeEffects on join/respawn
    }
    
    private boolean isAquatic(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("aquatic");
    }
}