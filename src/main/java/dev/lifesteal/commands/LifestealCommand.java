package dev.lifesteal.commands;

import dev.lifesteal.api.ArchetypeManager;
import dev.lifesteal.api.HeartManager;
import dev.lifesteal.api.ItemManager;
import dev.lifesteal.api.Lifesteal;
import dev.lifesteal.api.LifestealConfig;
import dev.lifesteal.managers.CombatManager;
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
    private final dev.lifesteal.api.ArchetypeManager archetypeManager;
    private final CombatManager combatManager;
    
    public LifestealCommand(@NotNull Lifesteal plugin) {
        this.plugin = plugin;
        this.config = plugin.getLifestealConfig();
        this.archetypeManager = plugin.getArchetypeManager();
        this.combatManager = plugin.getCombatManager();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("archetype")) {
            if (args.length == 0) {
                // Just show archetype info - no permission needed for self lookup
                cmdArchetypeInfo(sender);
                return true;
            }
            // Setting someone else's archetype requires admin permission
            cmdArchetype(sender, args);
            return true;
        }
        if (command.getName().equalsIgnoreCase("withdraw")) {
            cmdWithdraw(sender, args);
            return true;
        }
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
            case "archetype" -> {
                if (args.length == 1 && sender instanceof Player) {
                    cmdArchetypeInfo(sender);
                } else {
                    cmdArchetype(sender, Arrays.copyOfRange(args, 1, args.length));
                }
            }
            case "gui" -> cmdGUI(sender);
            case "withdraw" -> cmdWithdraw(sender, Arrays.copyOfRange(args, 1, args.length));
            case "leaderboard" -> cmdLeaderboard(sender, Arrays.copyOfRange(args, 1, args.length));
            case "version" -> cmdVersion(sender);
            case "trust" -> cmdTrust(sender, Arrays.copyOfRange(args, 1, args.length));
            case "untrust" -> cmdUntrust(sender, Arrays.copyOfRange(args, 1, args.length));
            case "trusts" -> cmdTrusts(sender);
            default -> sendHelp(sender);
        }
        return true;
    }
    
    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text("Lifesteal+ Help:").color(NamedTextColor.GOLD));
        for (String s : List.of("/hearts help", "/hearts reload", "/hearts hearts <player>",
                                "/hearts sethearts <player> <amount>", "/hearts giveheart <player>",
                                "/hearts giverevival <player>", "/hearts revive <player>",
                                "/hearts archetype <player> <archetype>", "/hearts gui", "/hearts withdraw <amount>",
                                "/hearts leaderboard", "/hearts version", "/hearts trust <player>",
                                "/hearts untrust <player>", "/hearts trusts")) {
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
        var target = ((Plugin) plugin).getServer().getOfflinePlayer(args[0]);
        if (target == null || target.getName() == null) { sender.sendMessage(Component.text("Player not found").color(NamedTextColor.RED)); return; }
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
    
    private void cmdArchetypeInfo(@NotNull CommandSender sender) {
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED)); return; }
        var archetype = archetypeManager.getArchetype(player);
        if (archetype == null) { sender.sendMessage(Component.text("No archetype selected").color(NamedTextColor.RED)); return; }
        sender.sendMessage(Component.text("Your archetype: " + archetype.getName()).color(NamedTextColor.GREEN));
    }
    
    private void cmdWithdraw(@NotNull CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED)); return; }
        if (!player.hasPermission("lifesteal.withdraw")) { player.sendMessage(Component.text("No permission.").color(NamedTextColor.RED)); return; }
        if (args.length < 1) { sender.sendMessage(Component.text("Usage: /withdraw <amount>").color(NamedTextColor.RED)); return; }
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid amount").color(NamedTextColor.RED));
            return;
        }
        if (amount <= 0) { sender.sendMessage(Component.text("Amount must be positive").color(NamedTextColor.RED)); return; }
        int currentHearts = (int) Math.floor(plugin.getHeartManager().getHearts(player));
        int minHearts = plugin.getHeartManager().getDefaultHearts();
        int heartsToWithdraw = Math.min(amount, Math.max(0, currentHearts - minHearts));
        if (heartsToWithdraw <= 0) {
            sender.sendMessage(Component.text("You have " + currentHearts + " hearts (minimum: " + minHearts + ")").color(NamedTextColor.RED));
            return;
        }
        plugin.getHeartManager().removeHearts(player.getUniqueId(), heartsToWithdraw);
        player.getInventory().addItem(plugin.getItemManager().getHeartCrystal(heartsToWithdraw));
        sender.sendMessage(Component.text("Withdrew " + heartsToWithdraw + " hearts").color(NamedTextColor.GREEN));
    }
    
    private void cmdVersion(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text("Lifesteal+ v" + ((Plugin) plugin).getDescription().getVersion()).color(NamedTextColor.GREEN));
    }
    
    private void cmdTrust(@NotNull CommandSender sender, String[] args) {
        if (!config.isTrustEnabled()) {
            sender.sendMessage(Component.text("Trust system is disabled on this server.").color(NamedTextColor.RED));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED));
            return;
        }
        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /hearts trust <player>").color(NamedTextColor.RED));
            return;
        }
        Player target = ((Plugin) plugin).getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Player not found").color(NamedTextColor.RED));
            return;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("You cannot trust yourself").color(NamedTextColor.RED));
            return;
        }
        plugin.getCombatManager().addTrust(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(Component.text("You now trust " + target.getName() + ". They will not steal your heart on kill.").color(NamedTextColor.GREEN));
    }
    
    private void cmdUntrust(@NotNull CommandSender sender, String[] args) {
        if (!config.isTrustEnabled()) {
            sender.sendMessage(Component.text("Trust system is disabled on this server.").color(NamedTextColor.RED));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED));
            return;
        }
        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /hearts untrust <player>").color(NamedTextColor.RED));
            return;
        }
        Player target = ((Plugin) plugin).getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Player not found").color(NamedTextColor.RED));
            return;
        }
        plugin.getCombatManager().removeTrust(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(Component.text("You no longer trust " + target.getName()).color(NamedTextColor.YELLOW));
    }
    
    private void cmdTrusts(@NotNull CommandSender sender) {
        if (!config.isTrustEnabled()) {
            sender.sendMessage(Component.text("Trust system is disabled on this server.").color(NamedTextColor.RED));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED));
            return;
        }
        List<UUID> trusted = ((dev.lifesteal.Lifesteal) plugin).getDatabaseManager().loadAllTrusted(player.getUniqueId());
        if (trusted.isEmpty()) {
            player.sendMessage(Component.text("You don't trust anyone.").color(NamedTextColor.YELLOW));
            return;
        }
        player.sendMessage(Component.text("You trust:").color(NamedTextColor.GREEN));
        for (UUID uuid : trusted) {
            String name = plugin.getServer().getOfflinePlayer(uuid).getName();
            player.sendMessage(Component.text("- " + (name != null ? name : uuid.toString())).color(NamedTextColor.WHITE));
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("archetype")) {
            if (args.length == 1) {
                return ((Plugin) plugin).getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            }
            if (args.length == 2) {
                return plugin.getArchetypeManager().getAllArchetypes().stream().map(a -> a.getId()).collect(Collectors.toList());
            }
            return List.of();
        }
        if (command.getName().equalsIgnoreCase("withdraw")) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("help", "reload", "hearts", "sethearts", "giveheart", "giverevival", "revive", "archetype", "gui", "withdraw", "leaderboard", "version", "trust", "untrust", "trusts");
        }
        if (args[0].equalsIgnoreCase("archetype") && args.length == 2) {
            return plugin.getArchetypeManager().getAllArchetypes().stream().map(a -> a.getId()).collect(Collectors.toList());
        }
        return List.of();
    }
}
