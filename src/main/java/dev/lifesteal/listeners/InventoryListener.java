package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.HeartManager;
import dev.lifesteal.api.LifestealConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class InventoryListener implements Listener {
    private final Lifesteal plugin;
    private final HeartManager heartManager;
    private final dev.lifesteal.api.ItemManager itemManager;
    private final LifestealConfig config;
    
    public InventoryListener(@NotNull Lifesteal plugin, @NotNull HeartManager heartManager,
                             @NotNull dev.lifesteal.api.ItemManager itemManager,
                             @NotNull LifestealConfig config) {
        this.plugin = plugin; this.heartManager = heartManager; this.itemManager = itemManager; this.config = config;
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        // Archetype selection GUI handled by Bukkit event listeners in GUIManager
    }
}
