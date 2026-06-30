package dev.lifesteal.recipes;

import dev.lifesteal.Lifesteal;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

public class RecipeRegistry {
    
    public static void registerHeartCrystal(@NotNull Lifesteal plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "heart_crystal");
        ShapedRecipe recipe = new ShapedRecipe(key, plugin.getItemManager().getHeartCrystal());
        recipe.shape("NDN", "DKD", "NDN");
        recipe.setIngredient('N', Material.NETHERITE_SCRAP);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('K', Material.OMINOUS_TRIAL_KEY);
        plugin.getServer().addRecipe(recipe);
    }
    
    public static void registerRevivalTotem(@NotNull Lifesteal plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "revival_totem");
        ShapedRecipe recipe = new ShapedRecipe(key, plugin.getItemManager().getRevivalTotem());
        recipe.shape("NDN", "DSD", "NDN");
        recipe.setIngredient('N', Material.NETHERITE_BLOCK);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('S', Material.NETHER_STAR);
        plugin.getServer().addRecipe(recipe);
    }
    
    public static void unregisterAll(@NotNull Lifesteal plugin) {
        plugin.getServer().removeRecipe(new NamespacedKey(plugin, "heart_crystal"));
        plugin.getServer().removeRecipe(new NamespacedKey(plugin, "revival_totem"));
    }
}
