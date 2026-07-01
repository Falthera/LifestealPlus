package dev.lifesteal.archetypes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class GuardianArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    
    public GuardianArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isGuardian(player)) return;
        var abs = player.getPotionEffect(PotionEffectType.ABSORPTION);
        if (abs == null || abs.getDuration() < 40) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120, 0, true, false));
        }
    }
    
    @EventHandler
    public void onKnockback(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isGuardian(player)) return;
        event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(0.5));
    }
    
    private boolean isGuardian(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("guardian");
    }
}