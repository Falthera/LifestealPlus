package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.HeartManager;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.managers.CombatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class CombatListener implements Listener {
    private final Lifesteal plugin;
    private final CombatManager combatManager;
    private final HeartManager heartManager;
    private final LifestealConfig config;

    public CombatListener(@NotNull Lifesteal plugin, @NotNull CombatManager combatManager,
                          @NotNull HeartManager heartManager, @NotNull LifestealConfig config) {
        this.plugin = plugin;
        this.combatManager = combatManager;
        this.heartManager = heartManager;
        this.config = config;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!config.isWorldEnabled(victim.getWorld().getName())) return;
        if (combatManager.isTrusted(attacker.getUniqueId(), victim.getUniqueId())) return;
        if (combatManager.isTrusted(victim.getUniqueId(), attacker.getUniqueId())) return;
        combatManager.tagPlayer(attacker.getUniqueId());
        combatManager.tagPlayer(victim.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player.getUniqueId())) return;
        if (!config.isWorldEnabled(player.getWorld().getName())) return;
        combatManager.removeTagOnLogout(player.getUniqueId());
        heartManager.removeHearts(player.getUniqueId(), 1);
        player.sendMessage(Component.text("You combat-logged and lost a heart!").color(NamedTextColor.RED));
        if (player.isOnline()) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        combatManager.clearCache(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        combatManager.removeTagOnLogout(player.getUniqueId());
        Player killer = player.getKiller();
        if (killer != null) {
            combatManager.removeTagOnLogout(killer.getUniqueId());
        }
    }
}
