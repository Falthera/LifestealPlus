package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.HeartManager;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.api.RevivalManager;
import dev.lifesteal.events.HeartCrystalUseEvent;
import dev.lifesteal.api.ItemManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {
    private final Lifesteal plugin;
    private final HeartManager heartManager;
    private final dev.lifesteal.api.ArchetypeManager archetypeManager;
    private final ItemManager itemManager;
    private final RevivalManager revivalManager;
    private final LifestealConfig config;
    
    public PlayerListener(@NotNull Lifesteal plugin, @NotNull HeartManager heartManager,
                          @NotNull dev.lifesteal.api.ArchetypeManager archetypeManager,
                          @NotNull ItemManager itemManager, @NotNull RevivalManager revivalManager,
                          @NotNull LifestealConfig config) {
        this.plugin = plugin; this.heartManager = heartManager; this.archetypeManager = archetypeManager;
        this.itemManager = itemManager; this.revivalManager = revivalManager; this.config = config;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        heartManager.onPlayerJoin(player);
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        heartManager.onPlayerQuit(event.getPlayer());
    }
    
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        archetypeManager.applyArchetypeEffects(event.getPlayer());
    }
    
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        archetypeManager.applyArchetypeEffects(event.getPlayer());
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!config.isWorldEnabled(victim.getWorld().getName())) return;
        
        Player killer = victim.getKiller();
        if (killer != null) {
            heartManager.stealHeart(killer.getUniqueId(), victim.getUniqueId());
            heartManager.incrementKills(killer.getUniqueId());
            playEpicKillVFX(killer, victim);
        }
    }
    
    private void playEpicKillVFX(@NotNull Player killer, @NotNull Player victim) {
        killer.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, killer.getLocation(), 100);
        killer.getWorld().spawnParticle(Particle.FIREWORK, killer.getLocation(), 50);
        killer.getWorld().spawnParticle(Particle.CRIT, killer.getLocation(), 80);
        
        killer.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, victim.getLocation(), 100);
        killer.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, victim.getLocation(), 50);
        
        killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 2.0f);
        killer.playSound(killer.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.5f);
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 2.0f, 0.5f);
        victim.playSound(victim.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null && event.getItem() != null) {
            if (itemManager.isHeartCrystal(event.getItem())) {
                event.setCancelled(true);
                var crystalEvent = new HeartCrystalUseEvent(player, 1);
                plugin.getServer().getPluginManager().callEvent(crystalEvent);
                if (!crystalEvent.isCancelled()) {
                    heartManager.addHearts(player.getUniqueId(), crystalEvent.getAmount());
                    player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                    playHeartCrystalUseVFX(player);
                }
            }
        }
    }
    
    private void playHeartCrystalUseVFX(@NotNull Player player) {
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 30, 0.5, 1, 0.5, 0.2);
        
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 2.0f);
    }
}
