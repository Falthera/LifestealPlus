package dev.lifesteal.managers;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.ArchetypeManager;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.archetypes.Archetype;
import dev.lifesteal.archetypes.*;
import dev.lifesteal.database.DatabaseManager;
import dev.lifesteal.events.PlayerArchetypeSelectEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;

public class ArchetypeManagerImpl implements ArchetypeManager {
    private final Lifesteal plugin;
    private final DatabaseManager database;
    private final LifestealConfig config;
    private final Map<UUID, Archetype> cache = new ConcurrentHashMap<>();
    private final Map<String, Archetype> registeredArchetypes = new LinkedHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();
    
    public ArchetypeManagerImpl(@NotNull Lifesteal plugin, @NotNull DatabaseManager database, @NotNull LifestealConfig config) {
        this.plugin = plugin; this.database = database; this.config = config;
        registerArchetypes();
    }
    
    private void registerArchetypes() {
        List.of(
            new Archetype("miner", "Miner", org.bukkit.Material.IRON_PICKAXE),
            new Archetype("aquatic", "Aquatic", org.bukkit.Material.TRIDENT),
            new Archetype("pyromancer", "Pyromancer", org.bukkit.Material.BLAZE_ROD),
            new Archetype("windwalker", "Windwalker", org.bukkit.Material.FEATHER),
            new Archetype("assassin", "Assassin", org.bukkit.Material.NETHERITE_SWORD),
            new Archetype("guardian", "Guardian", org.bukkit.Material.SHIELD),
            new Archetype("vampire", "Vampire", org.bukkit.Material.REDSTONE),
            new Archetype("trader", "Trader", org.bukkit.Material.EMERALD)
        ).forEach(a -> registeredArchetypes.put(a.getId(), a));
        
        RegisteredListeners();
    }
    
    private void RegisteredListeners() {
        registeredArchetypes.values().forEach(a -> {
            switch (a.getId()) {
                case "miner" -> listeners.add(new MinerArchetype(plugin).getListener());
                case "aquatic" -> listeners.add(new AquaticArchetype(plugin).getListener());
                case "pyromancer" -> listeners.add(new PyromancerArchetype(plugin).getListener());
                case "windwalker" -> listeners.add(new WindwalkerArchetype(plugin).getListener());
                case "assassin" -> listeners.add(new AssassinArchetype(plugin).getListener());
                case "guardian" -> listeners.add(new GuardianArchetype(plugin).getListener());
                case "vampire" -> listeners.add(new VampireArchetype(plugin).getListener());
                case "trader" -> listeners.add(new TraderArchetype(plugin).getListener());
            }
        });
        for (Listener l : listeners) Bukkit.getPluginManager().registerEvents(l, plugin);
    }
    
    @Override
    @Nullable
    public Archetype getArchetype(@NotNull UUID playerId) { return cache.get(playerId); }
    @Override
    @Nullable
    public Archetype getArchetype(@NotNull Player player) { return getArchetype(player.getUniqueId()); }
    
    @Override
    public CompletableFuture<Void> setArchetype(@NotNull UUID playerId, @NotNull Archetype archetype) {
        return CompletableFuture.runAsync(() -> {
            cache.put(playerId, archetype);
            savePlayerData(playerId);
        }, Bukkit.getScheduler().getMainThreadExecutor(plugin));
    }
    
    @Override
    public CompletableFuture<Void> setArchetype(@NotNull Player player, @NotNull Archetype archetype) { return setArchetype(player.getUniqueId(), archetype); }
    @Override public boolean hasArchetype(@NotNull UUID playerId) { return cache.containsKey(playerId); }
    @Override public boolean needsArchetypeSelection(@NotNull Player player) { return !hasArchetype(player.getUniqueId()); }
    @Override public boolean canSelectArchetype(@NotNull Player player) { return needsArchetypeSelection(player) && player.hasPermission("lifesteal.gui"); }
    
    @Override
    @NotNull
    public List<Archetype> getAllArchetypes() { return new ArrayList<>(registeredArchetypes.values()); }
    
    @Override
    @NotNull
    public Archetype getRandomArchetype() {
        List<Archetype> list = new ArrayList<>(registeredArchetypes.values());
        return list.get(new Random().nextInt(list.size()));
    }
    
    @Override
    public void onPlayerJoin(@NotNull Player player) { loadPlayerData(player.getUniqueId()); }
    
    @Override
    public void loadPlayerData(@NotNull UUID playerId) {
        CompletableFuture.supplyAsync(() -> database.loadArchetype(playerId), database.getExecutor())
            .thenAcceptAsync(archetypeId -> {
                if (archetypeId != null && registeredArchetypes.containsKey(archetypeId)) {
                    cache.put(playerId, registeredArchetypes.get(archetypeId));
                }
            }, Bukkit.getScheduler().getMainThreadExecutor(plugin));
    }
    
    @Override
    public CompletableFuture<Void> savePlayerData(@NotNull UUID playerId) {
        Archetype a = cache.get(playerId);
        if (a == null) return CompletableFuture.completedFuture(null);
        return CompletableFuture.runAsync(() -> database.saveArchetype(playerId, a.getId()), database.getExecutor());
    }
    
    @Override
    public void reload() { cache.clear(); registeredArchetypes.clear(); registerArchetypes(); }
    
    @Override
    public void loadAllOnline() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            loadPlayerData(p.getUniqueId());
        }
    }
    
    @Override
    public void applyArchetypeEffects(@NotNull Player player) {
        Archetype a = getArchetype(player);
        if (a == null) return;
        
        switch (a.getId()) {
            case "windwalker" -> {
                var speed = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
                if (speed == null || speed.getDuration() < 40) {
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
                }
            }
            case "guardian" -> {
                var abs = player.getPotionEffect(org.bukkit.potion.PotionEffectType.ABSORPTION);
                if (abs == null) {
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 0, true, false));
                }
            }
            case "aquatic" -> {
                var water = player.getPotionEffect(org.bukkit.potion.PotionEffectType.CONDUIT_POWER);
                if (water == null) {
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.CONDUIT_POWER, Integer.MAX_VALUE, 0, true, true));
                }
            }
            case "pyromancer" -> {
                var fire = player.getPotionEffect(org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE);
                if (fire == null) {
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, true));
                }
            }
        }
    }
    
    public boolean isArchetypeEnabled(String id) { return registeredArchetypes.containsKey(id); }
    public String getArchetypeName(String id) { return registeredArchetypes.getOrDefault(id, new Archetype(id, id, org.bukkit.Material.STONE)).getName(); }
}