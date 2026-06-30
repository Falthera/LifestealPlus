package dev.lifesteal.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Lifesteal {
    @NotNull HeartManager getHeartManager();
    @NotNull ArchetypeManager getArchetypeManager();
    @NotNull ItemManager getItemManager();
    @NotNull RecipeManager getRecipeManager();
    @NotNull GUIManager getGUIManager();
    @NotNull RevivalManager getRevivalManager();
    @NotNull LifestealConfig getConfig();
    boolean isPlaceholderAPIHookEnabled();
    boolean isVaultHookEnabled();
    @Nullable net.milkbowl.vault.economy.Economy getVaultEconomy();
}