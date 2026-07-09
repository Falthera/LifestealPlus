package dev.lifesteal.archetypes;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AssassinArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    private final Map<UUID, Long> lastDamaged = new ConcurrentHashMap<>();
    private final Map<UUID, Long> vanishCooldowns = new ConcurrentHashMap<>();
    private static final long VANISH_COOLDOWN_MILLIS = 25000L;
    private static final int VANISH_DURATION_TICKS = 100;
    
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
            event.setDamage(event.getFinalDamage() + 3.0);
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, true, false));
            attacker.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 30, 0.5, 1.0, 0.5, 0.5);
        } else if (attacker.isSneaking()) {
            event.setDamage(event.getFinalDamage() + 6.0);
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1, true, false));
            attacker.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 50, 0.8, 1.2, 0.8, 0.8);
        }
        
        lastDamaged.put(target.getUniqueId(), now);
    }
    
    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        Player player = event.getPlayer();
        if (!isAssassin(player)) return;
        
        long now = System.currentTimeMillis();
        Long cooldown = vanishCooldowns.get(player.getUniqueId());
        if (cooldown != null && now - cooldown < VANISH_COOLDOWN_MILLIS) {
            long remaining = (VANISH_COOLDOWN_MILLIS - (now - cooldown)) / 1000L;
            player.sendMessage(ChatColor.RED + "Vanish on cooldown! " + remaining + "s");
            return;
        }
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, VANISH_DURATION_TICKS, 0, true, false));
        player.sendMessage(ChatColor.DARK_PURPLE + "You vanish into the shadows...");
        vanishCooldowns.put(player.getUniqueId(), now);
    }
    
    private boolean isAssassin(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("assassin");
    }
}