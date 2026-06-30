package dev.lifesteal.gui;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.GUIManager;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.archetypes.Archetype;
import dev.lifesteal.events.ArchetypeSelectEvent;
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

public class GUIManagerImpl implements GUIManager, Listener {
    private static final Component ARCHETYPE_GUI_TITLE = Component.text("Select Archetype").color(NamedTextColor.DARK_PURPLE);
    private static final Component HEART_INFO_GUI_TITLE = Component.text("Heart Info").color(NamedTextColor.RED);
    
    private final Lifesteal plugin;
    private final LifestealConfig config;
    
    public GUIManagerImpl(@NotNull Lifesteal plugin, @NotNull LifestealConfig config) {
        this.plugin = plugin; this.config = config;
    }
    
    @Override
    public void openArchetypeSelectionGUI(@NotNull Player player) {
        Inventory inv = Bukkit.createInventory(new ArchetypeHolder(), 27, ARCHETYPE_GUI_TITLE);
        
        for (int i = 0; i < 27; i++) {
            ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = glass.getItemMeta();
            if (meta != null) meta.displayName(Component.text(" "));
            glass.setItemMeta(meta);
            inv.setItem(i, glass);
        }
        
        List<Archetype> archetypes = plugin.getArchetypeManager().getAllArchetypes();
        for (int i = 0; i < Math.min(archetypes.size(), 9); i++) {
            var a = archetypes.get(i);
            ItemStack item = new ItemStack(a.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(a.getName()).color(NamedTextColor.GOLD));
                meta.lore(a.getDescription().stream().map(s -> Component.text(s).color(NamedTextColor.GRAY)).collect(Collectors.toList()));
                item.setItemMeta(meta);
            }
            inv.setItem(10 + i, item);
        }
        
        player.openInventory(inv);
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle().equals(ARCHETYPE_GUI_TITLE)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
            List<Archetype> archetypes = plugin.getArchetypeManager().getAllArchetypes();
            int slot = event.getSlot();
            if (slot >= 10 && slot < 19) {
                int index = slot - 10;
                if (index < archetypes.size() && player.hasPermission("lifesteal.gui")) {
                    var selected = archetypes.get(index);
                    var selectEvent = new ArchetypeSelectEvent(player, selected);
                    plugin.getServer().getPluginManager().callEvent(selectEvent);
                    if (!selectEvent.isCancelled() && plugin.getArchetypeManager().canSelectArchetype(player)) {
                        plugin.getArchetypeManager().setArchetype(player, selected);
                        player.sendMessage(Component.text("You selected " + selected.getName() + "!").color(NamedTextColor.GREEN));
                        player.closeInventory();
                    }
                }
            }
        }
    }
    
    @Override
    public void openHeartInfoGUI(@NotNull Player player, @NotNull Player target) {
        Inventory inv = Bukkit.createInventory(new HeartInfoHolder(target.getUniqueId()), 9, HEART_INFO_GUI_TITLE);
        ItemStack heart = new ItemStack(Material.RED_DYE);
        ItemMeta meta = heart.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(target.getName() + "'s Hearts").color(NamedTextColor.RED));
            int hearts = plugin.getHeartManager().getHearts(target);
            meta.lore(List.of(Component.text("Hearts: " + hearts).color(NamedTextColor.WHITE),
                              Component.text("Max Hearts: " + plugin.getHeartManager().getMaxHearts()).color(NamedTextColor.WHITE)));
            heart.setItemMeta(meta);
        }
        inv.setItem(4, heart);
        player.openInventory(inv);
    }
    
    private static class ArchetypeHolder implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }
    
    private static class HeartInfoHolder implements InventoryHolder {
        private final java.util.UUID target;
        public HeartInfoHolder(java.util.UUID target) { this.target = target; }
        @Override public Inventory getInventory() { return null; }
    }
}
