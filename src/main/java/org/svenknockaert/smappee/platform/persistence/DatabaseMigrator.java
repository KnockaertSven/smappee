package org.svenknockaert.smappee.platform.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public final class DatabaseMigrator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrator.class);

    private DatabaseMigrator() {
    }

    public static void migrate(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .load();
        flyway.migrate();
    }

    public static HikariDataSource createDataSourceFromEnv() {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        String dbName = System.getenv().getOrDefault("DB_NAME", "smappee");
        String user = System.getenv().getOrDefault("DB_USER", "smappee");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "smappee");

        String jdbcUrl = "jdbc:mysql://%s:%s/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
                .formatted(host, port, dbName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    public static void migrateFromEnv() {
        int maxRetries = 15;
        for (int i = 1; i <= maxRetries; i++) {
            try (HikariDataSource ds = createDataSourceFromEnv()) {
                log.info("Connected to database. Applying Flyway migrations...");
                migrate(ds);
                log.info("Flyway migrations applied successfully.");
                return;
            } catch (Exception e) {
                log.warn("Migration attempt {}/{} failed: {}", i, maxRetries, e.getMessage());
                if (i == maxRetries) {
                    throw new IllegalStateException("Failed to migrate database after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Migration interrupted", ie);
                }
            }
        }
    }
}
