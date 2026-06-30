package dev.lifesteal.archetypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class AquaticArchetype implements Listener {
    private final dev.lifesteal.Lifesteal plugin;
    
    public AquaticArchetype(@NotNull dev.lifesteal.Lifesteal plugin) { this.plugin = plugin; }
    public Listener getListener() { return this; }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isAquatic(player)) return;
        if (player.isInWater() || player.isInRain()) {
            if (player.getPotionEffect(PotionEffectType.DOLPHINS_GRACE) == null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 100, 0, true, false));
            }
        }
    }
    
    private boolean isAquatic(Player player) {
        var a = plugin.getArchetypeManager().getArchetype(player);
        return a != null && a.getId().equals("aquatic");
    }
}