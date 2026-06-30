package dev.lifesteal.revivals;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.api.RevivalManager;
import dev.lifesteal.events.PlayerRevivedEvent;
import dev.lifesteal.events.RevivalTotemUseEvent;
import org.bukkit.BanList;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.particle.Particle;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RevivalManagerImpl implements RevivalManager {
    private final Lifesteal plugin;
    private final LifestealConfig config;
    private final Map<UUID, Boolean> revived = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Boolean>> pendingRevives = new ConcurrentHashMap<>();
    
    public RevivalManagerImpl(@NotNull Lifesteal plugin, @NotNull dev.lifesteal.database.DatabaseManager database,
                              @NotNull LifestealConfig config,
                              @NotNull dev.lifesteal.hearts.HeartManager heartManager,
                              @NotNull dev.lifesteal.api.ArchetypeManager archetypeManager) {
        this.plugin = plugin; this.config = config;
    }
    
    @Override
    public boolean isRevived(@NotNull UUID playerId) { return revived.getOrDefault(playerId, false); }
    
    @Override
    public boolean isReviving(@NotNull UUID reviverId) { return pendingRevives.containsKey(reviverId); }
    
    @Override
    public boolean canRevive(@NotNull Player reviver, @NotNull Player target) {
        if (reviver == target) return false;
        if (!plugin.getItemManager().isRevivalTotem(reviver.getInventory().getItemInMainHand())) return false;
        if (isRevived(target.getUniqueId())) return false;
        if (plugin.getHeartManager().getHearts(target) > 0) return false;
        return true;
    }
    
    @Override
    public CompletableFuture<Boolean> revivePlayer(@NotNull Player reviver, @NotNull Player target) {
        if (pendingRevives.containsKey(reviver.getUniqueId())) return CompletableFuture.completedFuture(false);
        if (!canRevive(reviver, target)) return CompletableFuture.completedFuture(false);
        
        var event = new RevivalTotemUseEvent(reviver, target);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return CompletableFuture.completedFuture(false);
        
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        pendingRevives.put(reviver.getUniqueId(), future);
        
        int heartsToRestore = config.getReviveHeartsRestored();
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getServer().getBanList(BanList.Type.NAME).pardon(target.getName());
            revived.put(target.getUniqueId(), true);
            plugin.getDatabaseManager().saveRevived(target.getUniqueId(), true);
            
            plugin.getHeartManager().addHearts(target.getUniqueId(), heartsToRestore);
            plugin.getArchetypeManager().loadPlayerData(target.getUniqueId());
            target.sendMessage(org.kyori.adventure.text.Component.text("You have been revived!").color(org.kyori.adventure.text.format.NamedTextColor.GREEN));
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
            target.getWorld().spawnParticle(Particle.HEART, target.getLocation(), 20, 0.5, 1.0, 0.5, 0.1);
            
            if (config.isRevivalBroadcastEnabled()) {
                var msg = config.getRevivalBroadcastMessage().replace("%player%", target.getName()).replace("%reviver%", reviver.getName());
                plugin.getServer().broadcast(org.kyori.adventure.text.Component.text(msg).color(org.kyori.adventure.text.format.NamedTextColor.GREEN));
            }
            
            ItemStack hand = reviver.getInventory().getItemInMainHand();
            if (hand != null && hand.getAmount() > 1) hand.setAmount(hand.getAmount() - 1);
            else if (hand != null) reviver.getInventory().setItemInMainHand(org.bukkit.Material.AIR != null ? new ItemStack(org.bukkit.Material.AIR) : null);
            
            var revivedEvent = new PlayerRevivedEvent(target, reviver, heartsToRestore);
            plugin.getServer().getPluginManager().callEvent(revivedEvent);
            
            future.complete(true);
            pendingRevives.remove(reviver.getUniqueId());
        }, config.getReviveDelayTicks());
        
        return future;
    }
    
    @Override
    public CompletableFuture<Boolean> reviveOfflinePlayer(@NotNull Player reviver, @NotNull UUID targetId) {
        return CompletableFuture.completedFuture(false);
    }
}
