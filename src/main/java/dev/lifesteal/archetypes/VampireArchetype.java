package dev.lifesteal.archetypes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class VampireArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    
    public VampireArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (!isVampire(attacker)) return;
        
        double damage = event.getFinalDamage();
        double heal = damage * 0.12 + java.util.concurrent.ThreadLocalRandom.current().nextDouble(0, 0.03);
        double maxHealth = attacker.getHealthScale();
        double newHealth = Math.min(maxHealth, attacker.getHealth() + heal);
        attacker.setHealth(newHealth);
        
        if (target.getHealth() <= 0 && event.getFinalDamage() >= target.getHealth()) {
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, true, false));
        }
    }
    
    private boolean isVampire(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("vampire");
    }
}