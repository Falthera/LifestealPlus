package dev.lifesteal.archetypes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VampireArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    private final Random random = new Random();
    
    public VampireArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (!isVampire(attacker)) return;
        
        double damage = event.getFinalDamage();
        double heal = damage * 0.12 + random.nextDouble(0, 0.03);
        double maxHealth = attacker.getMaxHealth();
        double newHealth = Math.min(maxHealth, attacker.getHealth() + heal);
        attacker.setHealth(newHealth);
        
        if (target.getHealth() <= 0 && event.getFinalDamage() >= target.getHealth()) {
            attacker.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 60, 0, true, false));
        }
    }
    
    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;
        if (!isVampire(player)) return;
        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        event.getDrops().clear();
        for (ItemStack drop : drops) {
            int bonus = drop.getAmount();
            if (bonus > 0 && random.nextDouble() < 0.5) {
                drop.setAmount(drop.getAmount() + 1);
            }
            event.getDrops().add(drop);
        }
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon != null && weapon.getType() != org.bukkit.Material.AIR && !weapon.containsEnchantment(org.bukkit.enchantments.Enchantment.LOOTING)) {
            weapon.addEnchantment(org.bukkit.enchantments.Enchantment.LOOTING, 1);
            player.getInventory().setItemInMainHand(weapon);
        }
    }
    
    private boolean isVampire(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("vampire");
    }
}