package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

public class AssassinChaseListener implements Listener {
    private final Lifesteal plugin;

    public AssassinChaseListener(@NotNull Lifesteal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player killer)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        plugin.getEventManager().onAssassinKill(killer, victim);
    }
}
