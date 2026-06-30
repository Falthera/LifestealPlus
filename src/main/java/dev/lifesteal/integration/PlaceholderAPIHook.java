package dev.lifesteal.integration;

import dev.lifesteal.Lifesteal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook {
    private final Lifesteal plugin;
    private Object expansion;

    public PlaceholderAPIHook(@NotNull Lifesteal plugin) {
        this.plugin = plugin;
        register();
    }

    public void register() {
        try {
            Class<?> expansionClass = Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion");
            expansion = java.lang.reflect.Proxy.newProxyInstance(
                expansionClass.getClassLoader(),
                new Class<?>[] { expansionClass },
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if (methodName.equals("getIdentifier")) return "lifesteal";
                    if (methodName.equals("getAuthor")) return "Lifesteal+";
                    if (methodName.equals("getVersion")) return plugin.getDescription().getVersion();
                    if (methodName.equals("canRegister")) return true;
                    if (methodName.equals("persist")) return true;
                    if (methodName.equals("onPlaceholderRequest")) {
                        if (args[0] == null) return "";
                        org.bukkit.entity.Player player = (org.bukkit.entity.Player) args[0];
                        String params = (String) args[1];
                        return switch (params.toLowerCase()) {
                            case "hearts" -> String.valueOf(plugin.getHeartManager().getHearts(player));
                            case "maxhearts" -> String.valueOf(plugin.getHeartManager().getMaxHearts());
                            case "dead" -> plugin.getHeartManager().isDead(player.getUniqueId()) ? "true" : "false";
                            case "archetype" -> {
                                var a = plugin.getArchetypeManager().getArchetype(player);
                                yield a != null ? a.getName() : "None";
                            }
                            case "kills" -> String.valueOf(plugin.getHeartManager().getKills(player.getUniqueId()));
                            case "deaths" -> "0";
                            default -> null;
                        };
                    }
                    return null;
                }
            );
            Object papi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
            if (papi != null) {
                java.lang.reflect.Method registerMethod = papi.getClass().getMethod("getExpansionManager");
                Object manager = registerMethod.invoke(papi);
                java.lang.reflect.Method register = manager.getClass().getMethod("register", expansionClass);
                register.invoke(manager, expansion);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook PlaceholderAPI: " + e.getMessage());
        }
    }

    public void unregister() {
        try {
            if (expansion != null) {
                Class<?> expansionClass = Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion");
                Object papi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
                if (papi != null) {
                    java.lang.reflect.Method registerMethod = papi.getClass().getMethod("getExpansionManager");
                    Object manager = registerMethod.invoke(papi);
                    java.lang.reflect.Method unregister = manager.getClass().getMethod("unregister", expansionClass);
                    unregister.invoke(manager, expansion);
                }
            }
        } catch (Exception ignored) {}
    }
}
