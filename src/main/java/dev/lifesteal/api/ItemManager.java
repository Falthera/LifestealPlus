package dev.lifesteal.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemManager {
    @NotNull ItemStack getHeartCrystal(int amount);
    @NotNull ItemStack getHeartCrystal();
    @NotNull ItemStack getRevivalTotem();
    @NotNull ItemStack getTradingHeart();
    boolean isHeartCrystal(@NotNull ItemStack item);
    boolean isRevivalTotem(@NotNull ItemStack item);
    boolean isTradingHeart(@NotNull ItemStack item);
    void registerRecipes();
    void unregisterRecipes();
}