package dev.lifesteal.archetypes;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AssassinArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    private final Map<UUID, Long> lastDamaged = new ConcurrentHashMap<>();
    
    public AssassinArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (!isAssassin(attacker)) return;
        
        long now = System.currentTimeMillis();
        Long last = lastDamaged.get(target.getUniqueId());
        
        if (last == null || now - last > 10000) {
            double dmg = event.getFinalDamage();
            event.setDamage(dmg + 4.0);
            attacker.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 20, 0.5, 1.0, 0.5, 0.5);
        }
        
        lastDamaged.put(target.getUniqueId(), now);
    }
    
    private boolean isAssassin(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("assassin");
    }
}