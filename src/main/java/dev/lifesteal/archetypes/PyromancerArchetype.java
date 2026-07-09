package dev.lifesteal.archetypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class PyromancerArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    
    public PyromancerArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isPyromancer(player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.LAVA || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
            || event.getCause() == EntityDamageEvent.DamageCause.FIRE) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isPyromancer(player)) return;
        
        if (isInLava(player)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, false));
            
            if (event.getFrom().getY() < event.getTo().getY()) {
                player.setVelocity(player.getVelocity().setY(0.4));
            }
        }
    }
    
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!isPyromancer(attacker)) return;
        event.getEntity().setFireTicks(40);
    }
    
    private boolean isPyromancer(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("pyromancer");
    }
    
    private boolean isInLava(Player player) {
        return player.getLocation().getBlock().getType() == Material.LAVA
            || player.getEyeLocation().getBlock().getType() == Material.LAVA;
    }
}
