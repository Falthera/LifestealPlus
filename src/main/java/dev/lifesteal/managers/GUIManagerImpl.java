package dev.lifesteal.managers;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.GUIManager;
import dev.lifesteal.api.LifestealConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class GUIManagerImpl implements GUIManager {
    private final Lifesteal plugin;
    private final LifestealConfig config;
    
    public GUIManagerImpl(@NotNull Lifesteal plugin, @NotNull LifestealConfig config) {
        this.plugin = plugin; this.config = config;
    }
    
    @Override
    public void openArchetypeSelectionGUI(@NotNull Player player) {
        Inventory inv = Bukkit.createInventory(new ArchetypeHolder(), 27, Component.text("Select Archetype").color(NamedTextColor.DARK_PURPLE));
        
        for (int i = 0; i < 27; i++) {
            ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = glass.getItemMeta();
            if (meta != null) meta.displayName(Component.empty());
            glass.setItemMeta(meta);
            inv.setItem(i, glass);
        }
        
        List<dev.lifesteal.archetypes.Archetype> archetypes = plugin.getArchetypeManager().getAllArchetypes();
        for (int i = 0; i < Math.min(archetypes.size(), 9); i++) {
            var a = archetypes.get(i);
            ItemStack item = new ItemStack(a.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(a.getName()).color(NamedTextColor.GOLD));
                meta.lore(a.getDescription().stream().map(s -> Component.text(s)).collect(Collectors.toList()));
                item.setItemMeta(meta);
            }
            inv.setItem(10 + i, item);
        }
        
        player.openInventory(inv);
    }
    
    @Override
    public void openArchetypeManagementGUI(@NotNull Player player) {
        openArchetypeSelectionGUI(player);
    }
    
    @Override
    public void openHeartInfoGUI(@NotNull Player player, @NotNull Player target) {
        Inventory inv = Bukkit.createInventory(new HeartInfoHolder(target.getUniqueId()), 9, Component.text("Heart Info").color(NamedTextColor.RED));
        ItemStack heart = new ItemStack(Material.RED_DYE);
        ItemMeta meta = heart.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(target.getName() + "'s Hearts"));
            int hearts = plugin.getHeartManager().getHearts(target);
            meta.lore(List.of(Component.text("Hearts: " + hearts)));
            heart.setItemMeta(meta);
        }
        inv.setItem(4, heart);
        player.openInventory(inv);
    }
    
    private static class ArchetypeHolder implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }
    
    private static class HeartInfoHolder implements InventoryHolder {
        private final UUID target;
        public HeartInfoHolder(UUID target) { this.target = target; }
        @Override public Inventory getInventory() { return null; }
    }
}
