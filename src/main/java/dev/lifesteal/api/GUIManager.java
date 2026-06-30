package dev.lifesteal.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface GUIManager {
    void openArchetypeSelectionGUI(@NotNull Player player);
    void openArchetypeManagementGUI(@NotNull Player player);
    void openHeartInfoGUI(@NotNull Player player, @NotNull org.bukkit.entity.Player target);
}