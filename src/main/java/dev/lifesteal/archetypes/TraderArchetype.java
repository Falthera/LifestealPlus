package dev.lifesteal.archetypes;

import org.bukkit.ChatColor;
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
            if (random.nextDouble() < 0.15) {
                ItemStack result = recipe.getResult();
                int amount = result.getAmount();
                player.getInventory().addItem(result.asQuantity(Math.max(1, amount * 2)));
            }
            if (random.nextDouble() < 0.08) {
                player.getInventory().addItem(new ItemStack(Material.EMERALD, Math.max(1, random.nextInt(3) + 2)));
            }
        }
    }
    
    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;
        if (!isTrader(player)) return;
        repairAllItems(player);
        if (random.nextDouble() < 0.25) {
            event.getDrops().add(new ItemStack(Material.EMERALD, random.nextInt(3) + 1));
        }
    }
    
    private void repairAllItems(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        ItemStack[] armor = player.getInventory().getArmorContents();
        boolean changed = false;
        
        if (main != null && main.getType() != Material.AIR && main.getDurability() > 0) {
            main.setDurability((short) 0);
            changed = true;
        }
        if (off != null && off.getType() != Material.AIR && off.getDurability() > 0) {
            off.setDurability((short) 0);
            changed = true;
        }
        for (int i = 0; i < armor.length; i++) {
            ItemStack piece = armor[i];
            if (piece != null && piece.getType() != Material.AIR && piece.getDurability() > 0) {
                piece.setDurability((short) 0);
                armor[i] = piece;
                changed = true;
            }
        }
        
        if (changed) {
            player.getInventory().setItemInMainHand(main);
            player.getInventory().setItemInOffHand(off);
            player.getInventory().setArmorContents(armor);
        }
    }
    
    private boolean isTrader(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("trader");
    }
}