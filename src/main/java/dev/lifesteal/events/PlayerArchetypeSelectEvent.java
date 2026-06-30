package dev.lifesteal.events;

import dev.lifesteal.archetypes.Archetype;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerArchetypeSelectEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private Archetype archetype;

    public PlayerArchetypeSelectEvent(@NotNull Player player, @NotNull Archetype archetype) {
        this.player = player;
        this.archetype = archetype;
    }
    @NotNull public Player getPlayer() { return player; }
    @NotNull public Archetype getArchetype() { return archetype; }
    public void setArchetype(@NotNull Archetype archetype) { this.archetype = archetype; }
    @Override @NotNull public HandlerList getHandlers() { return HANDLERS; }
    @NotNull public static HandlerList getHandlerList() { return HANDLERS; }
}