package dev.lifesteal.events;

import dev.lifesteal.archetypes.Archetype;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArchetypeSelectEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private final Player player;
    private Archetype archetype;

    public ArchetypeSelectEvent(@NotNull Player player, @Nullable Archetype archetype) {
        this.player = player;
        this.archetype = archetype;
    }
    @NotNull public Player getPlayer() { return player; }
    @Nullable public Archetype getArchetype() { return archetype; }
    public void setArchetype(@NotNull Archetype archetype) { this.archetype = archetype; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}