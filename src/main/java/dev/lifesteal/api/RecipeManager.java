package dev.lifesteal.api;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface RecipeManager {
    void registerAll();
    void unregisterAll();
    @NotNull Collection<Recipe> getRecipes();
}