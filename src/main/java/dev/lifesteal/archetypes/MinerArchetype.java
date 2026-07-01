package dev.lifesteal.archetypes;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MinerArchetype implements org.bukkit.event.Listener {
    private final dev.lifesteal.Lifesteal plugin;
    
    public MinerArchetype(@NotNull dev.lifesteal.Lifesteal plugin) {
        this.plugin = plugin;
    }
    
    public org.bukkit.event.Listener getListener() { return this; }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isArchetypeActive(player)) return;
        Block block = event.getBlock();
        Material type = block.getType();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.containsEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH)) return;
        
        ItemStack smelted = null;
        if (type == Material.IRON_ORE || type == Material.DEEPSLATE_IRON_ORE) smelted = new ItemStack(Material.IRON_INGOT, 1);
        else if (type == Material.GOLD_ORE || type == Material.DEEPSLATE_GOLD_ORE) smelted = new ItemStack(Material.GOLD_INGOT, 1);
        else if (type == Material.COPPER_ORE || type == Material.DEEPSLATE_COPPER_ORE) smelted = new ItemStack(Material.COPPER_INGOT, 2);
        else if (type == Material.ANCIENT_DEBRIS) smelted = new ItemStack(Material.NETHERITE_SCRAP, 1);
        else if (type == Material.NETHER_QUARTZ_ORE) smelted = new ItemStack(Material.QUARTZ, 1);
        else if (type == Material.STONE && tool.getType() == Material.NETHERITE_PICKAXE && tool.containsEnchantment(org.bukkit.enchantments.Enchantment.FORTUNE)) {
            return;
        }
        
        if (smelted != null) {
            int fortune = 0;
            if (tool.containsEnchantment(org.bukkit.enchantments.Enchantment.FORTUNE)) {
                fortune = tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.FORTUNE);
            }
            int amount = smelted.getAmount() * (fortune > 0 ? (fortune + 1) : 1);
            smelted.setAmount(amount);
            event.setDropItems(false);
            block.getWorld().dropItemNaturally(block.getLocation(), smelted);
        }
    }
    
    private boolean isArchetypeActive(Player player) {
        var archetype = plugin.getArchetypeManager().getArchetype(player);
        return archetype != null && archetype.getId().equals("miner");
    }
}