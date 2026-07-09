package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.HeartManager;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.api.RevivalManager;
import dev.lifesteal.api.ItemManager;
import dev.lifesteal.events.HeartCrystalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.BanList;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final Lifesteal plugin;
    private final HeartManager heartManager;
    private final dev.lifesteal.api.ArchetypeManager archetypeManager;
    private final ItemManager itemManager;
    private final RevivalManager revivalManager;
    private final LifestealConfig config;
    private final java.util.Map<UUID, Long> heartCrystalCooldowns = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<UUID, Long> lastDeathTimes = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long DEATH_DEBOUNCE_MILLIS = 5000L;
    
    public PlayerListener(@NotNull Lifesteal plugin, @NotNull HeartManager heartManager,
                          @NotNull dev.lifesteal.api.ArchetypeManager archetypeManager,
                          @NotNull ItemManager itemManager, @NotNull RevivalManager revivalManager,
                          @NotNull LifestealConfig config) {
        this.plugin = plugin; this.heartManager = heartManager; this.archetypeManager = archetypeManager;
        this.itemManager = itemManager; this.revivalManager = revivalManager; this.config = config;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        heartManager.onPlayerJoin(player);
        archetypeManager.onPlayerJoin(player);
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        heartManager.onPlayerQuit(event.getPlayer());
    }
    
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getServer().getBanList(BanList.Type.NAME).getBanEntry(player.getName()) != null) {
            player.kick(net.kyori.adventure.text.Component.text(config.getBanReason()));
            return;
        }
        
        archetypeManager.applyArchetypeEffects(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int hearts = heartManager.getHearts(player.getUniqueId());
            if (hearts <= 0) {
                hearts = 1;
            }
            player.setMaxHealth(hearts * 2.0);
            player.setHealth(hearts * 2.0);
        }, 2L);
    }
    
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        archetypeManager.applyArchetypeEffects(event.getPlayer());
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!config.isWorldEnabled(victim.getWorld().getName())) return;
        
        UUID victimId = victim.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastDeath = lastDeathTimes.get(victimId);
        if (lastDeath != null && now - lastDeath < DEATH_DEBOUNCE_MILLIS) {
            plugin.getLogger().warning("[DeathDebounce] Ignored duplicate death event for " + victim.getName() + " within " + DEATH_DEBOUNCE_MILLIS + "ms");
            return;
        }
        lastDeathTimes.put(victimId, now);
        
        Player killer = victim.getKiller();
        if (killer != null) {
            if (killer.isInvisible()) {
                obfuscateKillerInDeathMessage(event, killer);
            }
            
            if (config.isTrustEnabled() && plugin.getCombatManager().isTrusted(killer.getUniqueId(), victim.getUniqueId())) {
                killer.sendMessage(net.kyori.adventure.text.Component.text("You trust " + victim.getName() + ", no heart stolen.").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
                return;
            }
        heartManager.stealHeart(killer.getUniqueId(), victim.getUniqueId());
        heartManager.incrementKills(killer.getUniqueId());
        playEpicKillVFX(killer, victim);
    } else {
        heartManager.removeHearts(victim.getUniqueId(), 1);
        event.getDrops().add(itemManager.getHeartCrystal(1));
    }
    }
    
    private void playEpicKillVFX(@NotNull Player killer, @NotNull Player victim) {
        killer.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, killer.getLocation(), 100);
        killer.getWorld().spawnParticle(Particle.FIREWORK, killer.getLocation(), 50);
        killer.getWorld().spawnParticle(Particle.CRIT, killer.getLocation(), 80);
        
        killer.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, victim.getLocation(), 100);
        killer.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, victim.getLocation(), 50);
        
        killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 2.0f);
        killer.playSound(killer.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.5f);
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
        victim.playSound(victim.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
    }
    
    private void obfuscateKillerInDeathMessage(@NotNull PlayerDeathEvent event, @NotNull Player killer) {
        net.kyori.adventure.text.Component deathMessage = event.deathMessage();
        if (deathMessage == null) return;
        
        String killerName = killer.getName();
        String serialized = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(deathMessage);
        int lastIndex = serialized.lastIndexOf(killerName);
        if (lastIndex < 0) return;
        
        String prefix = serialized.substring(0, lastIndex);
        String obfuscated = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(
            net.kyori.adventure.text.Component.text(killerName)
                .decorate(net.kyori.adventure.text.format.TextDecoration.OBFUSCATED)
        );
        String suffix = serialized.substring(lastIndex + killerName.length());
        
        event.deathMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(prefix + obfuscated + suffix));
    }
    
    private void applyArchetypeEnchant(@NotNull Player player, @NotNull dev.lifesteal.archetypes.Archetype archetype) {
        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == org.bukkit.Material.AIR) {
            player.sendMessage(net.kyori.adventure.text.Component.text("Hold an item to enchant.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return;
        }
        
        String id = archetype.getId();
        switch (id) {
            case "miner" -> applyEnchantIfMatch(player, item, Enchantment.EFFICIENCY, 3);
            case "windwalker" -> applyEnchantToArmorIfMatch(player, Enchantment.FEATHER_FALLING, 4);
            case "guardian" -> applyEnchantToArmorIfMatch(player, Enchantment.PROTECTION, 1);
            case "aquatic" -> {
                boolean applied = applyEnchantIfMatch(player, item, Enchantment.RESPIRATION, 3)
                    || applyEnchantIfMatch(player, item, Enchantment.AQUA_AFFINITY, 1);
                if (!applied) {
                    player.sendMessage(net.kyori.adventure.text.Component.text("Hold any item to enchant.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                }
            }
            case "pyromancer" -> applyEnchantIfMatch(player, item, Enchantment.FIRE_ASPECT, 1);
            case "assassin" -> applyEnchantIfMatch(player, item, Enchantment.SHARPNESS, 3);
            case "vampire" -> applyEnchantIfMatch(player, item, Enchantment.LOOTING, 1);
            case "trader" -> applyEnchantToArmorIfMatch(player, Enchantment.MENDING, 1);
            default -> player.sendMessage(net.kyori.adventure.text.Component.text("Your archetype has no enchants to apply.").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        }
    }
    
    private boolean applyEnchantIfMatch(@NotNull Player player, @NotNull org.bukkit.inventory.ItemStack item, @NotNull Enchantment enchant, int level) {
         var meta = item.getItemMeta();
         if (meta == null) {
             player.sendMessage(net.kyori.adventure.text.Component.text("Could not apply enchant to this item.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
             return false;
         }
         if (item.containsEnchantment(enchant)) {
             int existingLevel = item.getEnchantmentLevel(enchant);
             if (existingLevel >= level) {
                 player.sendMessage(net.kyori.adventure.text.Component.text("Item already has this enchant.").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
                 return false;
             }
             meta.removeEnchant(enchant);
         }
         for (var entry : item.getEnchantments().entrySet()) {
             Enchantment existing = entry.getKey();
             if (existing.conflictsWith(enchant)) {
                 meta.removeEnchant(existing);
             }
         }
        try {
            meta.addEnchant(enchant, level, true);
            item.setItemMeta(meta);
            player.getInventory().setItemInMainHand(item);
            player.sendMessage(net.kyori.adventure.text.Component.text("Enchant applied!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
            return true;
        } catch (Exception e) {
            player.sendMessage(net.kyori.adventure.text.Component.text("Could not apply enchant to this item.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return false;
        }
    }
    
    private boolean applyEnchantToArmorIfMatch(@NotNull Player player, @NotNull Enchantment enchant, int level) {
        org.bukkit.inventory.ItemStack[] armor = player.getInventory().getArmorContents();
        boolean hasValid = false;
        boolean changed = false;
        for (int i = 0; i < armor.length; i++) {
            org.bukkit.inventory.ItemStack piece = armor[i];
            if (piece == null || piece.getType() == org.bukkit.Material.AIR) continue;
            hasValid = true;
            if (piece.containsEnchantment(enchant)) continue;
            var meta = piece.getItemMeta();
            if (meta == null) continue;
            for (var entry : piece.getEnchantments().entrySet()) {
                org.bukkit.enchantments.Enchantment existing = entry.getKey();
                if (existing.conflictsWith(enchant)) {
                    meta.removeEnchant(existing);
                }
            }
            try {
                meta.addEnchant(enchant, level, true);
                piece.setItemMeta(meta);
                armor[i] = piece;
                changed = true;
            } catch (Exception ignored) {}
        }
        if (!hasValid) {
            player.sendMessage(net.kyori.adventure.text.Component.text("Wear armor to enchant.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return false;
        }
        if (changed) {
            player.getInventory().setArmorContents(armor);
            player.sendMessage(net.kyori.adventure.text.Component.text("Enchant applied to armor!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        } else {
            player.sendMessage(net.kyori.adventure.text.Component.text("Armor already has this enchant.").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        }
        return true;
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() == null) return;
        
        if (itemManager.isHeartCrystal(event.getItem())) {
            event.setCancelled(true);
            long cooldownMs = config.getHeartCrystalCooldownSeconds() * 1000L;
            if (cooldownMs > 0) {
                long lastUse = heartCrystalCooldowns.getOrDefault(player.getUniqueId(), 0L);
                long now = System.currentTimeMillis();
                if (now - lastUse < cooldownMs) {
                    long remaining = (cooldownMs - (now - lastUse)) / 1000L;
                    player.sendMessage(net.kyori.adventure.text.Component.text("Cooldown! Wait " + remaining + "s").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                    return;
                }
            }
            int currentHearts = (int) Math.floor(heartManager.getHearts(player.getUniqueId()));
            if (currentHearts >= heartManager.getMaxHearts()) {
                player.sendMessage(net.kyori.adventure.text.Component.text("You already have max hearts!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }
            int amount = Math.min(event.getItem().getAmount(), 1);
            double totalHearts = amount * config.getHeartCrystalAmount();
            var crystalEvent = new HeartCrystalUseEvent(player, (int) totalHearts);
            plugin.getServer().getPluginManager().callEvent(crystalEvent);
            if (crystalEvent.isCancelled()) return;
            heartManager.addHearts(player.getUniqueId(), (int) totalHearts);
            event.getItem().setAmount(event.getItem().getAmount() - amount);
            player.sendMessage(net.kyori.adventure.text.Component.text("Gained +" + (int) totalHearts + " heart!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
            if (cooldownMs > 0) heartCrystalCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        } else if (itemManager.isTradingHeart(event.getItem())) {
            event.setCancelled(true);
            int amount = Math.min(event.getItem().getAmount(), 1);
            event.getItem().setAmount(event.getItem().getAmount() - amount);
            var newArchetype = archetypeManager.getRandomArchetype();
            archetypeManager.setArchetype(player, newArchetype);
            player.sendMessage(net.kyori.adventure.text.Component.text("Your archetype has been changed to: " + newArchetype.getName() + "!").color(net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }
    
    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) return;
        if (!itemManager.isRevivalTotem(event.getPlayer().getInventory().getItemInMainHand())) return;
        event.setCancelled(true);
        var future = plugin.getRevivalManager().revivePlayer(event.getPlayer(), target);
        future.thenAccept(success -> {
            if (!success) {
                event.getPlayer().sendMessage(net.kyori.adventure.text.Component.text("Could not revive this player").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            }
        });
    }
    
    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        Player player = event.getPlayer();
        var archetype = archetypeManager.getArchetype(player);
        if (archetype == null) return;
        applyArchetypeEnchant(player, archetype);
    }
}
