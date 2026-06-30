package dev.lifesteal.items;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.ItemManager;
import dev.lifesteal.api.LifestealConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemManagerImpl implements ItemManager {
    private final Lifesteal plugin;
    private final LifestealConfig config;
    private final NamespacedKey heartCrystalKey;
    private final NamespacedKey revivalTotemKey;
    
    public ItemManagerImpl(@NotNull Lifesteal plugin, @NotNull LifestealConfig config) {
        this.plugin = plugin; this.config = config;
        this.heartCrystalKey = new NamespacedKey(plugin, "heart_crystal");
        this.revivalTotemKey = new NamespacedKey(plugin, "revival_totem");
    }
    
    @Override
    @NotNull
    public ItemStack getHeartCrystal(int amount) {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA, amount);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Heart Crystal").color(NamedTextColor.RED));
        meta.lore(List.of(Component.text("Right-click to gain +1 permanent heart").color(NamedTextColor.GRAY)));
        meta.getPersistentDataContainer().set(heartCrystalKey, PersistentDataType.BYTE, (byte) 1);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
    
    @Override
    @NotNull
    public ItemStack getHeartCrystal() { return getHeartCrystal(1); }
    
    @Override
    @NotNull
    public ItemStack getRevivalTotem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        var meta = item.getItemMeta();
        meta.displayName(Component.text("Revival Totem").color(NamedTextColor.GOLD));
        meta.lore(List.of(Component.text("Right-click a player's head to revive them").color(NamedTextColor.GRAY)));
        meta.getPersistentDataContainer().set(revivalTotemKey, PersistentDataType.BYTE, (byte) 1);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
    
    @Override
    public boolean isHeartCrystal(@NotNull ItemStack item) {
        if (item.getType() != Material.HEART_OF_THE_SEA) return false;
        var meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(heartCrystalKey, PersistentDataType.BYTE);
    }
    
    @Override
    public boolean isRevivalTotem(@NotNull ItemStack item) {
        if (item.getType() != Material.PLAYER_HEAD) return false;
        var meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(revivalTotemKey, PersistentDataType.BYTE);
    }
    
    @Override
    public void registerRecipes() {
        // Recipes are registered by RecipeManagerImpl to avoid duplicates
    }
    
    @Override
    public void unregisterRecipes() {
        // Recipes are unregistered by RecipeManagerImpl to avoid duplicates
    }
}
