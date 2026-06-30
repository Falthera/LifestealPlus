package dev.lifesteal.utils;

import dev.lifesteal.Lifesteal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageUtils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    public static void send(@NotNull CommandSender sender, @NotNull String message) {
        if (sender instanceof Player player) player.sendMessage(MINI_MESSAGE.deserialize(message));
        else sender.sendMessage(MINI_MESSAGE.deserialize(message));
    }
    
    public static void send(@NotNull CommandSender sender, @NotNull Component component) {
        if (sender instanceof Player player) player.sendMessage(component);
        else sender.sendMessage(component);
    }
    
    public static Component parse(@NotNull String message) {
        return MINI_MESSAGE.deserialize(message);
    }
}
