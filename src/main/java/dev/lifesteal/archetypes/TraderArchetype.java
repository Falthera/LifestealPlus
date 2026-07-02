package dev.lifesteal.archetypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
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
        
        for (MerchantRecipe recipe : villager.getRecipes()) {
            if (random.nextDouble() < 0.05) {
                ItemStack result = recipe.getResult();
                int amount = result.getAmount();
                if (random.nextBoolean()) {
                    player.getInventory().addItem(result.asQuantity(Math.max(1, amount + 1)));
                } else {
                    player.getInventory().addItem(new ItemStack(Material.EMERALD, Math.max(1, amount + 4)));
                }
            }
        }
    }
    
    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;
        if (!isTrader(player)) return;
        repairRandomItem(player);
    }
    
    private void repairRandomItem(Player player) {
        List<ItemStack> damaged = new ArrayList<>();
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR && item.getDurability() > 0) {
                damaged.add(item);
            }
        }
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main.getType() != Material.AIR && main.getDurability() > 0) damaged.add(main);
        ItemStack off = player.getInventory().getItemInOffHand();
        if (off.getType() != Material.AIR && off.getDurability() > 0) damaged.add(off);
        
        if (!damaged.isEmpty()) {
            ItemStack toRepair = damaged.get(random.nextInt(damaged.size()));
            toRepair.setDurability((short) Math.max(0, toRepair.getDurability() - 1));
            if (toRepair.getDurability() == 0 && toRepair.getType().getMaxDurability() > 0) {
                toRepair.setDurability((short) toRepair.getType().getMaxDurability());
            }
        }
    }
    
    private boolean isTrader(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("trader");
    }
}