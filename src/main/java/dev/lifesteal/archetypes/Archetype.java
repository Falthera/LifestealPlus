package dev.lifesteal.archetypes;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Archetype {
    private final String id;
    private final String name;
    private List<String> description = Collections.emptyList();
    private final Material icon;
    private List<String> passives = Collections.emptyList();
    private List<String> equipmentBonuses = Collections.emptyList();
    
    public Archetype(@NotNull String id, @NotNull String name, @NotNull Material icon) {
        this.id = id; this.name = name; this.icon = icon;
    }
    
    public Archetype(@NotNull String id, @NotNull String name, @NotNull List<String> description, @NotNull Material icon,
                     @NotNull List<String> passives, @NotNull List<String> equipmentBonuses) {
        this.id = id; this.name = name; this.description = description;
        this.icon = icon; this.passives = passives; this.equipmentBonuses = equipmentBonuses;
    }
    
    @NotNull public String getId() { return id; }
    @NotNull public String getName() { return name; }
    @NotNull public List<String> getDescription() { return description; }
    @NotNull public Material getIcon() { return icon; }
    @NotNull public List<String> getPassives() { return passives; }
    @NotNull public List<String> getEquipmentBonuses() { return equipmentBonuses; }
    public void setDescription(@NotNull List<String> description) { this.description = description; }
}