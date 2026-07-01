package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.LifestealConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class EntityListener implements Listener {
    private final Lifesteal plugin;
    private final LifestealConfig config;
    
    public EntityListener(@NotNull Lifesteal plugin, @NotNull LifestealConfig config) {
        this.plugin = plugin; this.config = config;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        // Miner archetype is handled in MinerArchetype listener
    }
}
