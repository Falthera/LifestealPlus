package dev.lifesteal.archetypes;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class AssassinArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    
    public AssassinArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (!isAssassin(attacker)) return;
        
        if (target.getLastDamageCause() == null || target.getLastDamageCause().getEntity() == null
            || !target.getLastDamageCause().getEntity().getUniqueId().equals(attacker.getUniqueId())) {
            double dmg = event.getFinalDamage();
            event.setDamage(dmg + 4.0);
            attacker.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 20, 0.5, 1.0, 0.5, 0.5);
        }
    }
    
    private boolean isAssassin(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("assassin");
    }
}