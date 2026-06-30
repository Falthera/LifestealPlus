package dev.lifesteal.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RevivalTotemUseEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private final Player reviver;
    private final Player target;

    public RevivalTotemUseEvent(@NotNull Player reviver, @NotNull Player target) {
        this.reviver = reviver;
        this.target = target;
    }
    @NotNull public Player getReviver() { return reviver; }
    @NotNull public Player getTarget() { return target; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}