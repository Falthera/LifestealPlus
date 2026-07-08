package dev.lifesteal.listeners;

import dev.lifesteal.Lifesteal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

public class GuiCommandBlockerListener implements Listener {
    private static final String[] PLUGIN_ALIASES = {"hearts", "lifesteal", "ls", "archetype"};
    private static final String GUI_SUBCOMMAND = "gui";
    
    private final Lifesteal plugin;
    
    public GuiCommandBlockerListener(@NotNull Lifesteal plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase(java.util.Locale.ENGLISH).trim();
        
        if (!message.startsWith("/")) return;
        
        String withoutSlash = message.substring(1);
        String[] parts = withoutSlash.split("\\s+", 2);
        if (parts.length < 2) return;
        
        String baseCommand = parts[0];
        String subCommand = parts[1].toLowerCase(java.util.Locale.ENGLISH).trim();
        
        for (String alias : PLUGIN_ALIASES) {
            if (baseCommand.equals(alias) && subCommand.equals(GUI_SUBCOMMAND)) {
                event.setCancelled(true);
                player.kick(Component.text("The /" + baseCommand + " gui command has been removed.").color(NamedTextColor.RED));
                return;
            }
        }
    }
}
