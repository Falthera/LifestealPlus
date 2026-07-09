package dev.lifesteal.archetypes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VampireArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    private final Random random = new Random();
    private static final int BLOOD_FRENZY_WINDOW_TICKS = 240;
    private static final int BLOOD_FRENZY_MAX_STACKS = 5;
    private static final double BASE_LIFESTEAL = 0.18;
    private static final double LIFESTEAL_PER_STACK = 0.03;
    private static final int DESPERATION_HEALTH_PERCENT = 35;
    private static final int DESPERATION_HEAL_TICKS = 2;
    private static final int DESPERATION_SPEED_TICKS = 100;
    private static final long DESPERATION_COOLDOWN_MILLIS = 25000L;
    private final Map<UUID, Map<UUID, Long>> recentAttackersByVictim = new ConcurrentHashMap<>();
    private final Map<UUID, Long> desperationCooldowns = new ConcurrentHashMap<>();
    
    public VampireArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player target)) return;
        
        boolean attackerIsVampire = isVampire(attacker);
        boolean targetIsVampire = isVampire(target);
        if (!attackerIsVampire && !targetIsVampire) return;
        
        if (targetIsVampire) {
            recordAttacker(target, attacker);
        }
        
        if (attackerIsVampire) {
            double damage = event.getFinalDamage();
            int bloodStacks = getBloodStacks(attacker);
            double lifesteal = BASE_LIFESTEAL + (bloodStacks * LIFESTEAL_PER_STACK);
            double heal = damage * lifesteal + random.nextDouble(0, 0.02);
            double maxHealth = attacker.getMaxHealth();
            double newHealth = Math.min(maxHealth, attacker.getHealth() + heal);
            attacker.setHealth(newHealth);
            
            if (target.getHealth() <= 0 && event.getFinalDamage() >= target.getHealth()) {
                attacker.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 60, 1, true, false));
            }
        }
        
        Player vampire = attackerIsVampire ? attacker : target;
        applyBloodFrenzy(vampire);
    }
    
    private void applyBloodFrenzy(Player vampire) {
        int stacks = getBloodStacks(vampire);
        if (stacks <= 0) return;
        
        int resistanceLevel = Math.min(stacks, 4);
        int speedLevel = Math.min(stacks - 1, 2);
        
        vampire.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, resistanceLevel, true, false));
        if (speedLevel > 0) {
            vampire.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speedLevel, true, false));
        }
    }
    
    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;
        if (!isVampire(player)) return;
        
        double currentHealth = player.getHealth();
        double maxHealth = player.getMaxHealth();
        double newHealth = Math.min(maxHealth, currentHealth + 2.0);
        player.setHealth(newHealth);
        
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
    
    private void recordAttacker(Player victim, Player attacker) {
        UUID victimId = victim.getUniqueId();
        UUID attackerId = attacker.getUniqueId();
        long now = System.currentTimeMillis();
        
        Map<UUID, Long> attackers = recentAttackersByVictim.computeIfAbsent(victimId, k -> new ConcurrentHashMap<>());
        attackers.put(attackerId, now);
        
        cleanupOldAttackers(victimId, now);
        
        double healthPercent = (victim.getHealth() / victim.getMaxHealth()) * 100.0;
        if (healthPercent <= DESPERATION_HEALTH_PERCENT) {
            Long cooldown = desperationCooldowns.get(victimId);
            if (cooldown == null || now - cooldown >= DESPERATION_COOLDOWN_MILLIS) {
                triggerDesperation(victim);
                desperationCooldowns.put(victimId, now);
            }
        }
    }
    
    private void triggerDesperation(Player player) {
        double currentHealth = player.getHealth();
        double maxHealth = player.getMaxHealth();
        double newHealth = Math.min(maxHealth, currentHealth + DESPERATION_HEAL_TICKS);
        player.setHealth(newHealth);
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DESPERATION_SPEED_TICKS, 1, true, false));
        player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
        player.sendMessage(net.kyori.adventure.text.Component.text("Blood Frenzy!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
    }
    
    private void cleanupOldAttackers(UUID victimId, long now) {
        Map<UUID, Long> attackers = recentAttackersByVictim.get(victimId);
        if (attackers == null) return;
        
        long cutoff = now - (BLOOD_FRENZY_WINDOW_TICKS * 50L);
        attackers.entrySet().removeIf(entry -> entry.getValue() < cutoff);
        
        if (attackers.isEmpty()) {
            recentAttackersByVictim.remove(victimId);
        }
    }
    
    int getBloodStacks(Player player) {
        Map<UUID, Long> attackers = recentAttackersByVictim.get(player.getUniqueId());
        if (attackers == null || attackers.isEmpty()) return 0;
        
        cleanupOldAttackers(player.getUniqueId(), System.currentTimeMillis());
        attackers = recentAttackersByVictim.get(player.getUniqueId());
        if (attackers == null) return 0;
        
        return Math.min(BLOOD_FRENZY_MAX_STACKS, attackers.size());
    }
    
    private boolean isVampire(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("vampire");
    }
}