package dev.lifesteal.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HeartCrystalUseEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private final Player player;
    private int amount;

    public HeartCrystalUseEvent(@NotNull Player player, int amount) {
        this.player = player;
        this.amount = amount;
    }
    @NotNull public Player getPlayer() { return player; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}