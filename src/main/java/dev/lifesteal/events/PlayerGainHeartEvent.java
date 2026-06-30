package dev.lifesteal.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerGainHeartEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final int amount;
    private final int newTotal;

    public PlayerGainHeartEvent(@NotNull Player player, int amount, int newTotal) {
        this.player = player;
        this.amount = amount;
        this.newTotal = newTotal;
    }
    @NotNull public Player getPlayer() { return player; }
    public int getAmount() { return amount; }
    public int getNewTotal() { return newTotal; }
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}