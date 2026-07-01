package dev.lifesteal.archetypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        
        List<MerchantRecipe> newRecipes = new ArrayList<>();
        for (MerchantRecipe recipe : villager.getRecipes()) {
            if (random.nextDouble() < 0.05) {
                ItemStack result = recipe.getResult();
                int amount = result.getAmount();
                if (random.nextBoolean()) {
                    result = result.asQuantity(Math.max(1, amount + 1));
                } else {
                    ItemStack bonus = new ItemStack(Material.EMERALD, Math.max(1, amount + 4));
                    newRecipes.add(bonus);
                }
            }
            newRecipes.add(recipe);
        }
        if (!newRecipes.isEmpty()) {
            villager.setRecipes(newRecipes);
        }
    }
    
    private boolean isTrader(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("trader");
    }
}