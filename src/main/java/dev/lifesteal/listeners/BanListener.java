package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.HeartManager;
import dev.lifesteal.api.LifestealConfig;
import org.bukkit.BanList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.jetbrains.annotations.NotNull;

public class BanListener implements Listener {
    private final Lifesteal plugin;
    private final HeartManager heartManager;
    private final LifestealConfig config;
    
    public BanListener(@NotNull Lifesteal plugin, @NotNull HeartManager heartManager, @NotNull LifestealConfig config) {
        this.plugin = plugin; this.heartManager = heartManager; this.config = config;
    }
    
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (event.getResult() == Result.ALLOWED) {
            int hearts = heartManager.getHearts(event.getPlayer().getUniqueId());
            if (hearts <= 0) {
                event.disallow(Result.KICK_BANNED, net.kyori.adventure.text.Component.text(config.getBanReason()));
            }
        }
    }
}
