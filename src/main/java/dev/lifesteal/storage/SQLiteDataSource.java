package dev.lifesteal.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class SQLiteDataSource {
    public static HikariDataSource create(@NotNull String filePath) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + filePath);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName("Lifesteal-SQLite");
        config.setMaximumPoolSize(4);
        return new HikariDataSource(config);
    }
}
