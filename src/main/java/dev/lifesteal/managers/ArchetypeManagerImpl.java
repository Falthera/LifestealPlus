package dev.lifesteal.managers;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.ArchetypeManager;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.archetypes.Archetype;
import dev.lifesteal.archetypes.*;
import dev.lifesteal.database.DatabaseManager;
import dev.lifesteal.events.PlayerArchetypeSelectEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
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
    private BukkitTask actionBarTask;
    
    public ArchetypeManagerImpl(@NotNull Lifesteal plugin, @NotNull DatabaseManager database, @NotNull LifestealConfig config) {
        this.plugin = plugin; this.database = database; this.config = config;
        registerArchetypes();
        startActionBarTask();
    }
    
    private void startActionBarTask() {
        final MiniMessage mini = MiniMessage.miniMessage();
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                Archetype a = getArchetype(online);
                if (a == null) continue;
                StringBuilder sb = new StringBuilder();
                sb.append("<gold>").append(a.getName()).append("</gold><dark_gray> |</dark_gray> ");
                for (int i = 0; i < a.getPassives().size(); i++) {
                    if (i > 0) sb.append("<dark_gray>, </dark_gray>");
                    sb.append("<green>").append(a.getPassives().get(i)).append("</green>");
                }
                online.sendActionBar(mini.deserialize(sb.toString()));
            }
        }, 40L, 40L);
    }
    
    private void removeArchetypeEffects(@NotNull Player player) {
        for (PotionEffectType type : List.of(
            PotionEffectType.HASTE,
            PotionEffectType.SPEED,
            PotionEffectType.ABSORPTION,
            PotionEffectType.WATER_BREATHING,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.DOLPHINS_GRACE,
            PotionEffectType.HERO_OF_THE_VILLAGE
        )) {
            player.removePotionEffect(type);
        }
    }
    
    private void registerArchetypes() {
        List.of(
            new Archetype("miner", "Miner", List.of("+ Permanent Haste I", "+ Auto-smelts mined ores"), org.bukkit.Material.IRON_PICKAXE,
                List.of("Haste I", "Auto-smelt ores"), List.of("+ Efficiency III on pickaxes")),
            new Archetype("aquatic", "Aquatic", List.of("+ Permanent Water Breathing", "+ Respiration III, Aqua Affinity"), org.bukkit.Material.TRIDENT,
                List.of("Water Breathing", "Dolphin's Grace in water"), List.of("+ No water movement slowdown")),
            new Archetype("pyromancer", "Pyromancer", List.of("+ Permanent Fire Resistance", "+ Immune to fire/lava damage"), org.bukkit.Material.BLAZE_ROD,
                List.of("Fire Resistance", "Fire immune"), List.of("+ Fire Aspect I on melee")),
            new Archetype("windwalker", "Windwalker", List.of("+ Permanent Speed I", "+ Feather Falling IV", "+ 80% fall damage reduction"), org.bukkit.Material.FEATHER,
                List.of("Speed I", "Feather Falling IV", "Fall reduction"), List.of("+ Speed II on landing")),
            new Archetype("assassin", "Assassin", List.of("+ Permanent Speed I", "+ Sharpness III", "+ First hit +2 damage after 10s"), org.bukkit.Material.NETHERITE_SWORD,
                List.of("Speed I", "Sharpness III", "Opening bonus"), List.of("+ First-hit crit chance")),
            new Archetype("guardian", "Guardian", List.of("+ Permanent Absorption I", "+ Protection I"), org.bukkit.Material.SHIELD,
                List.of("Absorption I", "Protection I"), List.of("+ Faster shield raise", "+ Reduced knockback")),
            new Archetype("vampire", "Vampire", List.of("+ Permanent Speed I", "+ Looting I"), org.bukkit.Material.REDSTONE,
                List.of("Speed I", "Looting I"), List.of("+ Life steal 12-15%")),
            new Archetype("trader", "Trader", List.of("+ Permanent Hero of the Village I", "+ Mending I"), org.bukkit.Material.EMERALD,
                List.of("Hero of the Village I", "Mending I"), List.of("+ Villager discounts", "+ Bonus trades"))
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
        Player online = plugin.getServer().getPlayer(playerId);
        if (online != null && online.isOnline()) {
            removeArchetypeEffects(online);
        }
        cache.put(playerId, archetype);
        return CompletableFuture.runAsync(() -> {
            savePlayerData(playerId);
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player p = plugin.getServer().getPlayer(playerId);
                if (p != null && p.isOnline()) {
                    applyArchetypeEffects(p);
                }
            });
        }, database.getExecutor());
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
    public void onPlayerJoin(@NotNull Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            var archetypeId = database.loadArchetype(player.getUniqueId());
            Bukkit.getScheduler().getMainThreadExecutor(plugin).execute(() -> {
                Archetype existingArchetype = null;
                if (archetypeId != null && registeredArchetypes.containsKey(archetypeId)) {
                    existingArchetype = registeredArchetypes.get(archetypeId);
                    cache.put(player.getUniqueId(), existingArchetype);
                    player.sendMessage(net.kyori.adventure.text.Component.text("Welcome back, " + player.getName() + " (" + existingArchetype.getName() + ")").color(net.kyori.adventure.text.format.NamedTextColor.GOLD));
                    applyArchetypeEffects(player);
                } else {
                    Archetype random = getRandomArchetype();
                    setArchetype(player.getUniqueId(), random);
                    player.sendMessage(net.kyori.adventure.text.Component.text("Welcome, " + player.getName() + "! Your archetype is: " + random.getName() + "!").color(net.kyori.adventure.text.format.NamedTextColor.GOLD));
                    player.showTitle(net.kyori.adventure.title.Title.title(
                        net.kyori.adventure.text.Component.text("WELCOME TO LIFESTEAL+").color(net.kyori.adventure.text.format.NamedTextColor.RED),
                        net.kyori.adventure.text.Component.text("Your destiny awaits...").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW),
                        10, 50, 10));
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
                }
            });
        });
    }
    
    @Override
    public void loadPlayerData(@NotNull UUID playerId) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            var archetypeId = database.loadArchetype(playerId);
            Bukkit.getScheduler().getMainThreadExecutor(plugin).execute(() -> {
                if (archetypeId != null && registeredArchetypes.containsKey(archetypeId)) {
                    cache.put(playerId, registeredArchetypes.get(archetypeId));
                }
            });
        });
    }
    
    @Override
    public CompletableFuture<Void> savePlayerData(@NotNull UUID playerId) {
        Archetype a = cache.get(playerId);
        if (a == null) return CompletableFuture.completedFuture(null);
        return CompletableFuture.runAsync(() -> database.saveArchetype(playerId, a.getId()), database.getExecutor());
    }
    
    @Override
    public void reload() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            removeArchetypeEffects(online);
        }
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
        cache.clear();
        registeredArchetypes.clear();
        registerArchetypes();
        startActionBarTask();
        for (Player online : Bukkit.getOnlinePlayers()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                var archetypeId = database.loadArchetype(online.getUniqueId());
                Bukkit.getScheduler().getMainThreadExecutor(plugin).execute(() -> {
                    if (archetypeId != null && registeredArchetypes.containsKey(archetypeId)) {
                        cache.put(online.getUniqueId(), registeredArchetypes.get(archetypeId));
                        applyArchetypeEffects(online);
                    }
                });
            });
        }
    }
    
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
        
        removeArchetypeEffects(player);
        
        switch (a.getId()) {
            case "miner" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.HASTE, Integer.MAX_VALUE, 0, true, false));
                applyEnchant(player, org.bukkit.enchantments.Enchantment.DIG_SPEED, 3);
            }
            case "windwalker" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
                applyEnchant(player, org.bukkit.enchantments.Enchantment.PROTECTION_FALL, 4);
            }
            case "guardian" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 0, true, false));
                applyEnchantToArmor(player, org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            }
            case "aquatic" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false));
                applyEnchantToArmor(player, org.bukkit.enchantments.Enchantment.OXYGEN, 3);
                applyEnchantToArmor(player, org.bukkit.enchantments.Enchantment.WATER_WORKER, 1);
            }
            case "pyromancer" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, true));
                applyEnchant(player, org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 1);
            }
            case "assassin" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
                applyEnchant(player, org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 3);
            }
            case "vampire" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
                applyEnchant(player, org.bukkit.enchantments.Enchantment.LOOT_BONUS_MOBS, 1);
            }
            case "trader" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 0, true, false));
                applyEnchantToArmor(player, org.bukkit.enchantments.Enchantment.MENDING, 1);
            }
        }
    }
    
    private void applyEnchant(@NotNull Player player, @NotNull org.bukkit.enchantments.Enchantment enchant, int level) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main != null && main.getType() != org.bukkit.Material.AIR && isValidEnchantTarget(enchant, main)) {
            main.addEnchantment(enchant, level);
            player.getInventory().setItemInMainHand(main);
        }
    }
    
    private void applyEnchantToArmor(@NotNull Player player, @NotNull org.bukkit.enchantments.Enchantment enchant, int level) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        boolean changed = false;
        for (ItemStack piece : armor) {
            if (piece != null && piece.getType() != org.bukkit.Material.AIR && isValidEnchantTarget(enchant, piece)) {
                piece.addEnchantment(enchant, level);
                changed = true;
            }
        }
        if (changed) {
            player.getInventory().setArmorContents(armor);
        }
    }
    
    private boolean isValidEnchantTarget(@NotNull org.bukkit.enchantments.Enchantment enchant, @NotNull ItemStack item) {
        return enchant.canEnchantItem(item) && !item.containsEnchantment(enchant);
    }
    
    public boolean isArchetypeEnabled(String id) { return registeredArchetypes.containsKey(id); }
    public String getArchetypeName(String id) { return registeredArchetypes.getOrDefault(id, new Archetype(id, id, org.bukkit.Material.STONE)).getName(); }
}
