package dev.lifesteal.managers;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.api.RecipeManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RecipeManagerImpl implements RecipeManager {
    private final Lifesteal plugin;
    private final LifestealConfig config;
    private final List<Recipe> recipes = new ArrayList<>();
    private final List<org.bukkit.NamespacedKey> recipeKeys = new ArrayList<>();
    
    public RecipeManagerImpl(@NotNull Lifesteal plugin, @NotNull LifestealConfig config) {
        this.plugin = plugin; this.config = config;
    }
    
    @Override
    public void registerAll() {
        NamespacedKey heartKey = new NamespacedKey(plugin, "heart_crystal");
        ShapedRecipe heartCrystal = new ShapedRecipe(heartKey, plugin.getItemManager().getHeartCrystal());
        heartCrystal.shape("NDN", "DKD", "NDN");
        heartCrystal.setIngredient('N', org.bukkit.Material.NETHERITE_SCRAP);
        heartCrystal.setIngredient('D', org.bukkit.Material.DIAMOND_BLOCK);
        heartCrystal.setIngredient('K', org.bukkit.Material.OMINOUS_TRIAL_KEY);
        plugin.getServer().addRecipe(heartCrystal);
        recipes.add(heartCrystal);
        recipeKeys.add(heartKey);
        
        NamespacedKey reviveKey = new NamespacedKey(plugin, "revival_totem");
        ShapedRecipe revivalTotem = new ShapedRecipe(reviveKey, plugin.getItemManager().getRevivalTotem());
        revivalTotem.shape("NDN", "DSD", "NDN");
        revivalTotem.setIngredient('N', org.bukkit.Material.NETHERITE_BLOCK);
        revivalTotem.setIngredient('D', org.bukkit.Material.DIAMOND_BLOCK);
        revivalTotem.setIngredient('S', org.bukkit.Material.NETHER_STAR);
        plugin.getServer().addRecipe(revivalTotem);
        recipes.add(revivalTotem);
        recipeKeys.add(reviveKey);
    }
    
    @Override
    public void unregisterAll() {
        for (int i = 0; i < recipes.size() && i < recipeKeys.size(); i++) {
            plugin.getServer().removeRecipe(recipeKeys.get(i));
        }
        recipes.clear();
        recipeKeys.clear();
    }
    
    @Override
    @NotNull
    public Collection<Recipe> getRecipes() { return Collections.unmodifiableCollection(recipes); }
}
