package dev.lifesteal.archetypes;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class AquaticArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    
    public AquaticArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isAquatic(player)) return;
        boolean inWater = player.getLocation().getBlock().isLiquid() || player.isInWater();
        if (inWater) {
            var dolphin = player.getPotionEffect(PotionEffectType.DOLPHINS_GRACE);
            if (dolphin == null || dolphin.getDuration() < 40) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, true, false));
            }
        }
    }
    
    @EventHandler
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isAquatic(player)) return;
        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.DROWNING) {
            event.setCancelled(true);
        }
    }
    
    private boolean isAquatic(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("aquatic");
    }
}