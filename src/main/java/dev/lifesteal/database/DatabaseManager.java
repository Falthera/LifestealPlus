package dev.lifesteal.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.lifesteal.Lifesteal;
import dev.lifesteal.api.LifestealConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DatabaseManager {
    private final Lifesteal plugin;
    private final LifestealConfig config;
    private DataSource dataSource;
    private Executor executor;
    private String storageType;
    
    public DatabaseManager(@NotNull Lifesteal plugin) {
        this.plugin = plugin;
        this.config = plugin.getLifestealConfig();
    }
    
    public void initialize() {
        ConfigurationSection dbSection = config.getBukkitConfig().getConfigurationSection("database");
        if (dbSection == null) throw new IllegalStateException("Missing database config section");
        storageType = dbSection.getString("type", "sqlite").toLowerCase();
        executor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
        if ("sqlite".equalsIgnoreCase(storageType)) dataSource = initializeSQLite(dbSection);
        else if ("mysql".equalsIgnoreCase(storageType)) dataSource = initializeMySQL(dbSection);
        else throw new IllegalArgumentException("Unsupported database type: " + storageType);
        runMigrations();
    }
    
    private DataSource initializeSQLite(@NotNull ConfigurationSection section) {
        String file = section.getString("sqlite.file", "lifesteal.db");
        return dev.lifesteal.storage.SQLiteDataSource.create(((Plugin) plugin).getDataFolder().toPath().resolve(file).toString());
    }
    
    private DataSource initializeMySQL(@NotNull ConfigurationSection section) {
        String host = section.getString("mysql.host", "localhost");
        int port = section.getInt("mysql.port", 3306);
        String databaseName = section.getString("mysql.database", "lifesteal");
        String username = section.getString("mysql.username", "root");
        String password = section.getString("mysql.password", "");
        String poolSize = section.getString("mysql.pool-size", "10");
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?useSSL=false&characterEncoding=utf8");
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(Integer.parseInt(poolSize));
        hikariConfig.setPoolName("Lifesteal-Hikari");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return new HikariDataSource(hikariConfig);
    }
    
    private void runMigrations() {
        try (Connection conn = dataSource.getConnection()) {
            if ("sqlite".equalsIgnoreCase(storageType)) {
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS player_hearts (uuid TEXT PRIMARY KEY, hearts INTEGER NOT NULL DEFAULT 10);");
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS player_archetypes (uuid TEXT PRIMARY KEY, archetype TEXT NOT NULL);");
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS revivals (target_uuid TEXT PRIMARY KEY, revived INTEGER NOT NULL DEFAULT 0);");
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS player_kills (uuid TEXT PRIMARY KEY, kills INTEGER NOT NULL DEFAULT 0);");
            } else {
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS player_hearts (uuid VARCHAR(36) PRIMARY KEY, hearts INT NOT NULL DEFAULT 10);");
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS player_archetypes (uuid VARCHAR(36) PRIMARY KEY, archetype VARCHAR(64) NOT NULL);");
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS revivals (target_uuid VARCHAR(36) PRIMARY KEY, revived INT NOT NULL DEFAULT 0);");
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS player_kills (uuid VARCHAR(36) PRIMARY KEY, kills INT NOT NULL DEFAULT 0);");
            }
        } catch (SQLException e) {
            ((Plugin) plugin).getLogger().severe("Failed to run migrations: " + e.getMessage());
        }
    }
    
    public int loadHearts(@NotNull UUID uuid) {
        String sql = "SELECT hearts FROM player_hearts WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (var rs = ps.executeQuery()) { if (rs.next()) return rs.getInt("hearts"); }
        } catch (SQLException e) {
            ((Plugin) plugin).getLogger().warning("Failed to load hearts for " + uuid + ": " + e.getMessage());
        }
        return config.getDefaultHearts();
    }
    
    public void saveHearts(@NotNull UUID uuid, int hearts) {
        String sql = storageType.equalsIgnoreCase("sqlite") 
            ? "INSERT OR REPLACE INTO player_hearts (uuid, hearts) VALUES (?, ?)"
            : "INSERT INTO player_hearts (uuid, hearts) VALUES (?, ?) ON DUPLICATE KEY UPDATE hearts = VALUES(hearts)";
        try (Connection conn = dataSource.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, hearts);
            ps.executeUpdate();
        } catch (SQLException e) {
            ((Plugin) plugin).getLogger().warning("Failed to save hearts for " + uuid + ": " + e.getMessage());
        }
    }
    
    public String loadArchetype(@NotNull UUID uuid) {
        String sql = "SELECT archetype FROM player_archetypes WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (var rs = ps.executeQuery()) { if (rs.next()) return rs.getString("archetype"); }
        } catch (SQLException e) {
            ((Plugin) plugin).getLogger().warning("Failed to load archetype for " + uuid + ": " + e.getMessage());
        }
        return null;
    }
    
    public void saveArchetype(@NotNull UUID uuid, @NotNull String archetype) {
        String sql = storageType.equalsIgnoreCase("sqlite")
            ? "INSERT OR REPLACE INTO player_archetypes (uuid, archetype) VALUES (?, ?)"
            : "INSERT INTO player_archetypes (uuid, archetype) VALUES (?, ?) ON DUPLICATE KEY UPDATE archetype = VALUES(archetype)";
        try (Connection conn = dataSource.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, archetype);
            ps.executeUpdate();
        } catch (SQLException e) {
            ((Plugin) plugin).getLogger().warning("Failed to save archetype for " + uuid + ": " + e.getMessage());
        }
    }
    
    public boolean loadRevived(@NotNull UUID uuid) {
        String sql = "SELECT revived FROM revivals WHERE target_uuid = ?";
        try (Connection conn = dataSource.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (var rs = ps.executeQuery()) { if (rs.next()) return rs.getInt("revived") == 1; }
        } catch (SQLException e) {
            ((Plugin) plugin).getLogger().warning("Failed to load revive status for " + uuid + ": " + e.getMessage());
        }
        return false;
    }
    
    public void saveRevived(@NotNull UUID uuid, boolean revived) {
        String sql = storageType.equalsIgnoreCase("sqlite")
            ? "INSERT OR REPLACE INTO revivals (target_uuid, revived) VALUES (?, ?)"
            : "INSERT INTO revivals (target_uuid, revived) VALUES (?, ?) ON DUPLICATE KEY UPDATE revived = VALUES(revived)";
        try (Connection conn = dataSource.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, revived ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            ((Plugin) plugin).getLogger().warning("Failed to save revive status for " + uuid + ": " + e.getMessage());
        }
    }
    
    public int loadKills(@NotNull UUID uuid) {
        String sql = "SELECT kills FROM player_kills WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (var rs = ps.executeQuery()) { if (rs.next()) return rs.getInt("kills"); }
        } catch (SQLException e) {
            ((Plugin) plugin).getLogger().warning("Failed to load kills for " + uuid + ": " + e.getMessage());
        }
        return 0;
    }
    
    public void incrementKills(@NotNull UUID uuid, int amount) {
        String sql = storageType.equalsIgnoreCase("sqlite")
            ? "INSERT INTO player_kills (uuid, kills) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET kills = kills + ?"
            : "INSERT INTO player_kills (uuid, kills) VALUES (?, ?) ON DUPLICATE KEY UPDATE kills = kills + ?";
        try (Connection conn = dataSource.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, amount);
            ps.setInt(3, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            ((Plugin) plugin).getLogger().warning("Failed to increment kills for " + uuid + ": " + e.getMessage());
        }
    }
    
    public List<PlayerKillsRecord> getTopKillers(int limit) {
        List<PlayerKillsRecord> results = new ArrayList<>();
        String sql = "SELECT uuid, kills FROM player_kills ORDER BY kills DESC LIMIT ?";
        try (Connection conn = dataSource.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, Math.min(limit, 100)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new PlayerKillsRecord(UUID.fromString(rs.getString("uuid")), rs.getInt("kills")));
                }
            }
        } catch (SQLException e) {
            ((Plugin) plugin).getLogger().warning("Failed to load top killers: " + e.getMessage());
        }
        return results;
    }
    
    public record PlayerKillsRecord(@NotNull UUID uuid, int kills) {}
    
    public DataSource getDataSource() { return dataSource; }
    public Executor getExecutor() { return executor; }
    public void shutdown() {
        if (executor instanceof java.util.concurrent.ExecutorService es) es.shutdown();
        if (dataSource instanceof HikariDataSource h) h.close();
    }
}
