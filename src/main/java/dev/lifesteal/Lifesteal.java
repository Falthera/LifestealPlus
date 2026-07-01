package dev.lifesteal;

import dev.lifesteal.api.*;
import dev.lifesteal.commands.LifestealCommand;
import dev.lifesteal.config.LifestealConfigImpl;
import dev.lifesteal.database.DatabaseManager;
import dev.lifesteal.gui.GUIManagerImpl;
import dev.lifesteal.hearts.HeartManagerImpl;
import dev.lifesteal.items.ItemManagerImpl;
import dev.lifesteal.listeners.AntiOpAbuseListener;
import dev.lifesteal.managers.ArchetypeManagerImpl;
import dev.lifesteal.managers.LeaderboardManager;
import dev.lifesteal.managers.RecipeManagerImpl;
import dev.lifesteal.revivals.RevivalManagerImpl;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Lifesteal extends JavaPlugin implements dev.lifesteal.api.Lifesteal {
    private static Lifesteal instance;
    private LifestealConfigImpl config;
    private DatabaseManager databaseManager;
    private HeartManagerImpl heartManager;
    private ArchetypeManagerImpl archetypeManager;
    private ItemManagerImpl itemManager;
    private RecipeManagerImpl recipeManager;
    private GUIManagerImpl guiManager;
    private RevivalManagerImpl revivalManager;
    private LeaderboardManager leaderboardManager;
    private boolean placeholderAPIEnabled = false;
    private boolean vaultEnabled = false;
    private Object vaultEconomy;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.config = new LifestealConfigImpl(this);
        config.reload();
        this.databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        this.heartManager = new HeartManagerImpl(this, databaseManager, config);
        this.archetypeManager = new ArchetypeManagerImpl(this, databaseManager, config);
        this.itemManager = new ItemManagerImpl(this, config);
        this.recipeManager = new RecipeManagerImpl(this, config);
        this.guiManager = new GUIManagerImpl(this, config);
        this.revivalManager = new RevivalManagerImpl(this, databaseManager, config, heartManager, archetypeManager);
        heartManager.loadAllOnline();
        archetypeManager.loadAllOnline();
        this.leaderboardManager = new LeaderboardManager(this, databaseManager, config, archetypeManager);
        if (config.isPlaceholderAPIEnabled() && getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPIEnabled = true;
            new dev.lifesteal.integration.PlaceholderAPIHook(this);
        }
        if (config.isVaultEnabled() && getServer().getPluginManager().isPluginEnabled("Vault")) {
            try {
                var registration = getServer().getServicesManager().getRegistration(Class.forName("net.milkbowl.vault.economy.Economy"));
                if (registration != null) {
                    vaultEconomy = registration.getProvider();
                    vaultEnabled = true;
                }
            } catch (Exception ignored) {}
        }
        registerListeners();
        registerCommands();
        recipeManager.registerAll();
        getLogger().info("Lifesteal+ v" + getDescription().getVersion() + " enabled successfully!");
    }
    
    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new dev.lifesteal.listeners.PlayerListener(this, heartManager, archetypeManager, itemManager, revivalManager, config), this);
        pm.registerEvents(new dev.lifesteal.listeners.EntityListener(this, heartManager, archetypeManager, config), this);
        pm.registerEvents(new dev.lifesteal.listeners.InventoryListener(this, heartManager, itemManager, config), this);
        pm.registerEvents(new dev.lifesteal.listeners.BanListener(this, heartManager, config), this);
        pm.registerEvents(new dev.lifesteal.listeners.AntiOpAbuseListener(this, config), this);
        pm.registerEvents(guiManager, this);
    }
    
    private void registerCommands() {
        var cmd = getCommand("lifesteal");
        if (cmd != null) cmd.setExecutor(new LifestealCommand(this));
        cmd = getCommand("hearts");
        if (cmd != null) cmd.setExecutor(new LifestealCommand(this));
        cmd = getCommand("ls");
        if (cmd != null) cmd.setExecutor(new LifestealCommand(this));
    }
    
    @Override
    public void onDisable() {
        if (databaseManager != null) databaseManager.shutdown();
        if (recipeManager != null) recipeManager.unregisterAll();
    }
    
    @Override
    public void onLoad() {}
    @NotNull public static Lifesteal getInstance() { return instance; }
    @NotNull @Override public HeartManager getHeartManager() { return heartManager; }
    @NotNull @Override public ArchetypeManager getArchetypeManager() { return archetypeManager; }
    @NotNull @Override public ItemManager getItemManager() { return itemManager; }
    @NotNull @Override public RecipeManager getRecipeManager() { return recipeManager; }
    @NotNull @Override public GUIManager getGUIManager() { return guiManager; }
    @NotNull @Override public RevivalManager getRevivalManager() { return revivalManager; }
    @Override public boolean isPlaceholderAPIHookEnabled() { return placeholderAPIEnabled; }
    @Override public boolean isVaultHookEnabled() { return vaultEnabled; }
    @Override @Nullable public Object getVaultEconomy() { return vaultEconomy; }
    @NotNull @Override public LifestealConfig getLifestealConfig() { return config; }
    public dev.lifesteal.managers.LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
}
