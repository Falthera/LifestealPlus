package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.managers.GracePeriodManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class GracePeriodListener implements Listener {
    private final Lifesteal plugin;
    private final GracePeriodManager gracePeriodManager;
    private final LifestealConfig config;

    public GracePeriodListener(@NotNull Lifesteal plugin, @NotNull GracePeriodManager gracePeriodManager, @NotNull LifestealConfig config) {
        this.plugin = plugin;
        this.gracePeriodManager = gracePeriodManager;
        this.config = config;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!config.isGracePeriodEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!gracePeriodManager.isInGracePeriod(player.getUniqueId())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!config.isGracePeriodEnabled()) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (gracePeriodManager.isInGracePeriod(victim.getUniqueId())) {
            event.setCancelled(true);
            attacker.sendMessage(net.kyori.adventure.text.Component.text(victim.getName() + " is in grace period!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
        }
        if (gracePeriodManager.isInGracePeriod(attacker.getUniqueId())) {
            event.setCancelled(true);
            attacker.sendMessage(net.kyori.adventure.text.Component.text("You are in grace period and cannot attack!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!config.isGracePeriodEnabled()) return;
        gracePeriodManager.loadGracePeriod(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        gracePeriodManager.clearCache(event.getPlayer().getUniqueId());
    }
}
