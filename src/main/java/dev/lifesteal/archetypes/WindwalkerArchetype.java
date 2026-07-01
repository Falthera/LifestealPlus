package dev.lifesteal.archetypes;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class WindwalkerArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    
    public WindwalkerArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isWindwalker(player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            double damage = event.getFinalDamage();
            event.setDamage(damage * 0.2);
            if (damage > 3.0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, true, false));
                Location loc = player.getLocation();
                player.getWorld().spawnParticle(Particle.CLOUD, loc, 20, 0.5, 0.1, 0.5, 0.1);
            }
        }
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isWindwalker(event.getPlayer())) return;
        var player = event.getPlayer();
        var speedEffect = player.getPotionEffect(PotionEffectType.SPEED);
        if (speedEffect == null || speedEffect.getType().getDuration() < 40) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 0, true, false));
        }
    }
    
    private boolean isWindwalker(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("windwalker");
    }
}