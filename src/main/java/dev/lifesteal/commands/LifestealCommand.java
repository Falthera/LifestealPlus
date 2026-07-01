package dev.lifesteal.commands;

import dev.lifesteal.api.ArchetypeManager;
import dev.lifesteal.api.HeartManager;
import dev.lifesteal.api.ItemManager;
import dev.lifesteal.api.Lifesteal;
import dev.lifesteal.api.LifestealConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class LifestealCommand implements CommandExecutor, TabCompleter {
    private final Lifesteal plugin;
    private final LifestealConfig config;
    
    public LifestealCommand(@NotNull Lifesteal plugin) {
        this.plugin = plugin;
        this.config = plugin.getLifestealConfig();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(sender);
            case "reload" -> cmdReload(sender);
            case "hearts" -> cmdHearts(sender, new String[0]);
            case "sethearts" -> cmdSetHearts(sender, Arrays.copyOfRange(args, 1, args.length));
            case "giveheart" -> cmdGiveHeart(sender, Arrays.copyOfRange(args, 1, args.length));
            case "giverevival" -> cmdGiveRevival(sender, Arrays.copyOfRange(args, 1, args.length));
            case "revive" -> cmdRevive(sender, Arrays.copyOfRange(args, 1, args.length));
            case "archetype" -> cmdArchetype(sender, Arrays.copyOfRange(args, 1, args.length));
            case "gui" -> cmdGUI(sender);
            case "withdraw" -> cmdWithdraw(sender, Arrays.copyOfRange(args, 1, args.length));
            case "leaderboard" -> cmdLeaderboard(sender, Arrays.copyOfRange(args, 1, args.length));
            case "version" -> cmdVersion(sender);
            default -> sendHelp(sender);
        }
        return true;
    }
    
    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text("Lifesteal+ Help:").color(NamedTextColor.GOLD));
        for (String s : List.of("/lifesteal help", "/lifesteal reload", "/lifesteal hearts <player>",
                                "/lifesteal sethearts <player> <amount>", "/lifesteal giveheart <player>",
                                "/lifesteal giverevival <player>", "/lifesteal revive <player>",
                                "/lifesteal archetype <player> <archetype>", "/lifesteal gui", "/lifesteal withdraw <amount>",
                                "/lifesteal leaderboard", "/lifesteal version")) {
            sender.sendMessage(Component.text(s).color(NamedTextColor.WHITE));
        }
    }
    
    private void cmdReload(@NotNull CommandSender sender) {
        if (!sender.hasPermission("lifesteal.reload")) {
            sender.sendMessage(Component.text("No permission.").color(NamedTextColor.RED));
            return;
        }
        plugin.getLifestealConfig().reload();
        plugin.getHeartManager().reloadConfig();
        plugin.getArchetypeManager().reload();
        ((dev.lifesteal.Lifesteal) plugin).getLeaderboardManager().reload();
        sender.sendMessage(Component.text("Configuration reloaded!").color(NamedTextColor.GREEN));
    }
    
    private void cmdHearts(@NotNull CommandSender sender, String[] args) {
        if (args.length < 1) { sender.sendMessage(Component.text("Usage: /lifesteal hearts <player>").color(NamedTextColor.RED)); return; }
        Player target = ((Plugin) plugin).getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(Component.text("Player not found").color(NamedTextColor.RED)); return; }
        sender.sendMessage(Component.text(target.getName() + " has " + plugin.getHeartManager().getHearts(target) + " hearts.").color(NamedTextColor.GREEN));
    }
    
    private void cmdSetHearts(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifesteal.hearts")) { sender.sendMessage(Component.text("No permission.").color(NamedTextColor.RED)); return; }
        if (args.length < 2) { sender.sendMessage(Component.text("Usage: /lifesteal sethearts <player> <amount>").color(NamedTextColor.RED)); return; }
        Player target = ((Plugin) plugin).getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(Component.text("Player not found").color(NamedTextColor.RED)); return; }
        int amount = Integer.parseInt(args[1]);
        plugin.getHeartManager().setHearts(target.getUniqueId(), amount);
        sender.sendMessage(Component.text("Set " + target.getName() + " hearts to " + amount).color(NamedTextColor.GREEN));
    }
    
    private void cmdGiveHeart(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifesteal.give")) { sender.sendMessage(Component.text("No permission.").color(NamedTextColor.RED)); return; }
        if (args.length < 1) { sender.sendMessage(Component.text("Usage: /lifesteal giveheart <player>").color(NamedTextColor.RED)); return; }
        Player target = ((Plugin) plugin).getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(Component.text("Player not found").color(NamedTextColor.RED)); return; }
        plugin.getHeartManager().addHearts(target.getUniqueId(), 1);
        sender.sendMessage(Component.text("Gave 1 heart to " + target.getName()).color(NamedTextColor.GREEN));
    }
    
    private void cmdGiveRevival(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifesteal.give")) { sender.sendMessage(Component.text("No permission.").color(NamedTextColor.RED)); return; }
        if (args.length < 1) { sender.sendMessage(Component.text("Usage: /lifesteal giverevival <player>").color(NamedTextColor.RED)); return; }
        Player target = ((Plugin) plugin).getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(Component.text("Player not found").color(NamedTextColor.RED)); return; }
        target.getInventory().addItem(plugin.getItemManager().getRevivalTotem());
        sender.sendMessage(Component.text("Gave Revival Totem to " + target.getName()).color(NamedTextColor.GREEN));
    }
    
    private void cmdRevive(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifesteal.revive")) { sender.sendMessage(Component.text("Use Revival Totem on a player's head to revive them!").color(NamedTextColor.YELLOW)); return; }
    }
    
    private void cmdLeaderboard(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifesteal.leaderboard")) {
            sender.sendMessage(Component.text("No permission.").color(NamedTextColor.RED));
            return;
        }
        if (!plugin.getLifestealConfig().isDiscordLeaderboardEnabled()) {
            sender.sendMessage(Component.text("Discord leaderboard is not enabled in config.").color(NamedTextColor.RED));
            return;
        }
        ((dev.lifesteal.Lifesteal) plugin).getLeaderboardManager().sendLeaderboard();
        sender.sendMessage(Component.text("Leaderboard sent to Discord!").color(NamedTextColor.GREEN));
    }
    
    private void cmdArchetype(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifesteal.archetype")) { sender.sendMessage(Component.text("No permission.").color(NamedTextColor.RED)); return; }
        if (args.length < 2) { sender.sendMessage(Component.text("Usage: /lifesteal archetype <player> <archetype>").color(NamedTextColor.RED)); return; }
        Player target = ((Plugin) plugin).getServer().getPlayer(args[0]);
        if (target == null) { sender.sendMessage(Component.text("Player not found").color(NamedTextColor.RED)); return; }
        var archetypes = plugin.getArchetypeManager().getAllArchetypes();
        var archetype = archetypes.stream().filter(a -> a.getId().equalsIgnoreCase(args[1])).findFirst().orElse(null);
        if (archetype == null) { sender.sendMessage(Component.text("Archetype not found").color(NamedTextColor.RED)); return; }
        plugin.getArchetypeManager().setArchetype(target, archetype);
        sender.sendMessage(Component.text("Set archetype of " + target.getName() + " to " + archetype.getName()).color(NamedTextColor.GREEN));
    }
    
    private void cmdGUI(@NotNull CommandSender sender) {
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED)); return; }
        if (!player.hasPermission("lifesteal.gui")) { player.sendMessage(Component.text("No permission.").color(NamedTextColor.RED)); return; }
        plugin.getGUIManager().openArchetypeSelectionGUI(player);
    }
    
    private void cmdWithdraw(@NotNull CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED)); return; }
        if (!player.hasPermission("lifesteal.withdraw")) { player.sendMessage(Component.text("No permission.").color(NamedTextColor.RED)); return; }
        if (args.length < 1) { player.sendMessage(Component.text("Usage: /lifesteal withdraw <amount>").color(NamedTextColor.RED)); return; }
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid amount").color(NamedTextColor.RED));
            return;
        }
        if (amount <= 0) { player.sendMessage(Component.text("Amount must be positive").color(NamedTextColor.RED)); return; }
        int currentHearts = plugin.getHeartManager().getHearts(player);
        int minHearts = plugin.getHeartManager().getDefaultHearts();
        int heartsToWithdraw = Math.min(amount, currentHearts - minHearts);
        if (heartsToWithdraw <= 0) {
            player.sendMessage(Component.text("You don't have extra hearts to withdraw").color(NamedTextColor.RED));
            return;
        }
        plugin.getHeartManager().removeHearts(player.getUniqueId(), heartsToWithdraw);
        player.getInventory().addItem(plugin.getItemManager().getHeartCrystal(heartsToWithdraw));
        player.sendMessage(Component.text("Withdrew " + heartsToWithdraw + " hearts").color(NamedTextColor.GREEN));
    }
    
    private void cmdVersion(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text("Lifesteal+ v" + ((Plugin) plugin).getDescription().getVersion()).color(NamedTextColor.GREEN));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("help", "reload", "hearts", "sethearts", "giveheart", "giverevival", "revive", "archetype", "gui", "withdraw", "leaderboard", "version");
        }
        if (args[0].equalsIgnoreCase("archetype") && args.length == 2) {
            return plugin.getArchetypeManager().getAllArchetypes().stream().map(a -> a.getId()).collect(Collectors.toList());
        }
        return List.of();
    }
}
