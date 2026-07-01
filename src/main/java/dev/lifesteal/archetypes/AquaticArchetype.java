package dev.lifesteal.archetypes;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AquaticArchetype {
    private final dev.lifesteal.Lifesteal plugin;
    
    public AquaticArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    
    public org.bukkit.event.Listener getListener() { 
        return new org.bukkit.event.Listener() {}; 
    }
}