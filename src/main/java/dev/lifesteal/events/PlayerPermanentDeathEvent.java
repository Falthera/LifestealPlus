package dev.lifesteal.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerPermanentDeathEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final int finalHearts;

    public PlayerPermanentDeathEvent(@NotNull Player player, int finalHearts) {
        this.player = player;
        this.finalHearts = finalHearts;
    }
    @NotNull public Player getPlayer() { return player; }
    public int getFinalHearts() { return finalHearts; }
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}