package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.HeartManager;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.api.RevivalManager;
import dev.lifesteal.events.ArchetypeSelectEvent;
import dev.lifesteal.events.HeartCrystalUseEvent;
import dev.lifesteal.api.ItemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
        if (archetypeManager.needsArchetypeSelection(player)) {
            player.sendMessage(net.kyori.adventure.text.Component.text("Welcome! Please select an archetype with /lifesteal gui").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        heartManager.onPlayerQuit(event.getPlayer());
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer != null) {
            heartManager.onPlayerDeath(victim.getUniqueId(), killer.getUniqueId());
            heartManager.incrementKills(killer.getUniqueId());
        }
        if (heartManager.isDead(victim.getUniqueId())) {
            event.setDeathMessage(null);
        }
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
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 2.0f);
                    player.spawnParticle(org.bukkit.Particle.HEART, player.getLocation(), 20, 0.5, 1.0, 0.5, 0.1);
                }
            }
        }
    }
}
