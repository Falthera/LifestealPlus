package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class BrewingListener implements Listener {
    private static final int BOOSTED_DURATION_TICKS = 9600;
    private static final int VANILLA_STRENGTH_I_DURATION_TICKS = 3600;
    private static final int VANILLA_STRENGTH_I_AMPLIFIER = 0;
    private static final int VANILLA_STRENGTH_II_AMPLIFIER = 1;
    private static final int VANILLA_STRENGTH_II_DURATION_TICKS = 1800;
    
    private final Lifesteal plugin;
    
    public BrewingListener(@NotNull Lifesteal plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBrew(BrewEvent event) {
        BrewerInventory inv = event.getContents();
        if (inv == null) return;
        
        if (!hasVanillaStrengthIInput(inv)) return;
        if (!hasGlowstoneDustInIngredients(inv)) return;
        
        ItemStack customResult = createStrengthIIPotion();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                replaceVanillaStrengthII(inv, customResult);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    private void replaceVanillaStrengthII(BrewerInventory inv, ItemStack customResult) {
        int size = inv.getSize();
        for (int i = 0; i < size; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (!(item.getItemMeta() instanceof PotionMeta meta)) continue;
            
            boolean isStrengthII = false;
            for (PotionEffect effect : meta.getAllEffects()) {
                if (effect.getType() == PotionEffectType.STRENGTH
                        && effect.getAmplifier() == VANILLA_STRENGTH_II_AMPLIFIER
                        && effect.getDuration() == VANILLA_STRENGTH_II_DURATION_TICKS) {
                    isStrengthII = true;
                    break;
                }
            }
            if (isStrengthII) {
                inv.setItem(i, customResult.clone());
            }
        }
    }
    
    private boolean hasVanillaStrengthIInput(BrewerInventory inv) {
        for (int i = 0; i < Math.min(4, inv.getSize()); i++) {
            ItemStack item = inv.getItem(i);
            if (!isVanillaStrengthI(item)) continue;
            return true;
        }
        return false;
    }
    
    private boolean hasGlowstoneDustInIngredients(BrewerInventory inv) {
        int size = Math.min(4, inv.getSize());
        for (int i = 0; i < size; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == Material.GLOWSTONE_DUST) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isVanillaStrengthI(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) return false;
        if (!(item.getItemMeta() instanceof PotionMeta meta)) return false;
        
        for (PotionEffect effect : meta.getAllEffects()) {
            if (effect.getType() == PotionEffectType.STRENGTH
                    && effect.getAmplifier() == VANILLA_STRENGTH_I_AMPLIFIER
                    && effect.getDuration() == VANILLA_STRENGTH_I_DURATION_TICKS) {
                return true;
            }
        }
        return false;
    }
    
    private ItemStack createStrengthIIPotion() {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.addCustomEffect(new PotionEffect(
            PotionEffectType.STRENGTH,
            BOOSTED_DURATION_TICKS,
            VANILLA_STRENGTH_II_AMPLIFIER,
            true,
            true,
            true
        ), true);
        meta.displayName(Component.text("Potion of Strength II").color(NamedTextColor.RED));
        item.setItemMeta(meta);
        return item;
    }
}
