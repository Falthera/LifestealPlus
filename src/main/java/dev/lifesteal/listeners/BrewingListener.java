package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class BrewingListener implements Listener {
    private static final int INPUT_SLOT = 0;
    private static final int INGREDIENT_SLOT_START = 1;
    private static final int INGREDIENT_SLOT_END = 3;
    private static final int BOOSTED_DURATION_TICKS = 9600;
    private static final int VANILLA_STRENGTH_I_DURATION_TICKS = 3600;
    private static final int VANILLA_STRENGTH_I_AMPLIFIER = 0;
    
    private final Lifesteal plugin;
    
    public BrewingListener(@NotNull Lifesteal plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBrew(BrewEvent event) {
        ItemStack[] contents = event.getContents();
        if (contents == null || contents.length <= INGREDIENT_SLOT_END) return;
        
        ItemStack input = contents[INPUT_SLOT];
        if (!isVanillaStrengthI(input)) return;
        if (!hasGlowstoneDustInIngredients(contents)) return;
        
        ItemStack customResult = createStrengthIIPotion();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                var inv = event.getInventory();
                if (inv == null) return;
                
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack item = inv.getItem(i);
                    if (item == null || item.getType() == Material.AIR) continue;
                    if (isPotionBottle(item) && isVanillaStrengthII(item)) {
                        inv.setItem(i, customResult.clone());
                    }
                }
            }
        }.runTaskLater(plugin, 1L);
    }
    
    private boolean hasGlowstoneDustInIngredients(ItemStack[] contents) {
        for (int i = INGREDIENT_SLOT_START; i <= INGREDIENT_SLOT_END; i++) {
            if (contents[i] != null && contents[i].getType() == Material.GLOWSTONE_DUST) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isVanillaStrengthI(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) return false;
        if (!(item.getItemMeta() instanceof PotionMeta meta)) return false;
        if (!meta.hasCustomEffect(PotionEffectType.STRENGTH)) return false;
        PotionEffect effect = meta.getCustomEffect(PotionEffectType.STRENGTH);
        return effect.getAmplifier() == VANILLA_STRENGTH_I_AMPLIFIER
            && effect.getDuration() == VANILLA_STRENGTH_I_DURATION_TICKS;
    }
    
    private boolean isVanillaStrengthII(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) return false;
        if (!(item.getItemMeta() instanceof PotionMeta meta)) return false;
        if (!meta.hasCustomEffect(PotionEffectType.STRENGTH)) return false;
        PotionEffect effect = meta.getCustomEffect(PotionEffectType.STRENGTH);
        return effect.getAmplifier() == 1
            && effect.getDuration() == 1800;
    }
    
    private boolean isPotionBottle(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return switch (item.getType()) {
            case POTION, SPLASH_POTION, LINGERING_POTION -> true;
            default -> false;
        };
    }
    
    private ItemStack createStrengthIIPotion() {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.addCustomEffect(new PotionEffect(
            PotionEffectType.STRENGTH,
            BOOSTED_DURATION_TICKS,
            1,
            true,
            true,
            true
        ), true);
        meta.displayName(Component.text("Potion of Strength II").color(NamedTextColor.RED));
        item.setItemMeta(meta);
        return item;
    }
}
