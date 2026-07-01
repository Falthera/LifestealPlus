package dev.lifesteal.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerRevivedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player revivedPlayer;
    private final Player reviver;
    private final int heartsRestored;

    public PlayerRevivedEvent(@NotNull Player revivedPlayer, @Nullable Player reviver, int heartsRestored) {
        this.revivedPlayer = revivedPlayer;
        this.reviver = reviver;
        this.heartsRestored = heartsRestored;
    }
    @NotNull public Player getRevivedPlayer() { return revivedPlayer; }
    @Nullable public Player getReviver() { return reviver; }
    public int getHeartsRestored() { return heartsRestored; }
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}