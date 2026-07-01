package dev.lifesteal.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Lifesteal {
    @NotNull HeartManager getHeartManager();
    @NotNull ArchetypeManager getArchetypeManager();
    @NotNull ItemManager getItemManager();
    @NotNull RecipeManager getRecipeManager();
    @NotNull GUIManager getGUIManager();
    @NotNull RevivalManager getRevivalManager();
    @NotNull dev.lifesteal.managers.CombatManager getCombatManager();
    @NotNull dev.lifesteal.managers.GracePeriodManager getGracePeriodManager();
    @NotNull LifestealConfig getLifestealConfig();
    boolean isPlaceholderAPIHookEnabled();
    boolean isVaultHookEnabled();
    @Nullable Object getVaultEconomy();
}
