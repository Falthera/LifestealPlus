package dev.lifesteal.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PassiveEffects {
    void apply(@NotNull Player player);
    void applyEquipmentBonuses(@NotNull Player player, @NotNull ItemStack item);
}
