package dev.lifesteal.archetypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TraderArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    private final Random random = new Random();
    
    public TraderArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) return;
        Player player = event.getPlayer();
        if (!isTrader(player)) return;
        if (player.getPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE) == null) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 0, true, false));
        }
        for (MerchantRecipe recipe : villager.getRecipes()) {
            if (random.nextDouble() < 0.05) {
                ItemStack result = recipe.getResult();
                int amount = result.getAmount();
                if (random.nextBoolean()) {
                    result.setAmount(amount + 1);
                } else {
                    Material bonus = Material.EMERALD;
                    result.setAmount(bonus == Material.EMERALD ? 5 : 2);
                }
                villager.setRecipes(new ArrayList<>(villager.getRecipes()));
            }
        }
    }
    
    private boolean isTrader(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("trader");
    }
}