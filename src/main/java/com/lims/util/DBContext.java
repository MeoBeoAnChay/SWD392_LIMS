package com.lims.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe Database Context using HikariCP Connection Pool for Microsoft SQL Server,
 * with fallback to standard JDBC DriverManager if connection pool initialization fails.
 */
public class DBContext {

    private static final Logger LOGGER = Logger.getLogger(DBContext.class.getName());
    private static HikariDataSource dataSource;
    private static final Properties properties = new Properties();

    static {
        try (InputStream is = DBContext.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                properties.load(is);
            } else {
                LOGGER.warning("db.properties not found in classpath. Using defaults.");
            }

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            initDataSource();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize DBContext DataSource", e);
        }
    }

    private static synchronized void initDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        try {
            String server = properties.getProperty("db.serverName", "localhost");
            String port = properties.getProperty("db.portNumber", "1433");
            String database = properties.getProperty("db.databaseName", "LIMS_DB");
            String user = properties.getProperty("db.username", "sa");
            String pass = properties.getProperty("db.password", "123");
            String encrypt = properties.getProperty("db.encrypt", "false");
            String trustCert = properties.getProperty("db.trustServerCertificate", "true");

            String jdbcUrl = String.format(
                    "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s;trustServerCertificate=%s;",
                    server, port, database, encrypt, trustCert
            );

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(user);
            config.setPassword(pass);
            config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("hikari.maximumPoolSize", "15")));
            config.setMinimumIdle(Integer.parseInt(properties.getProperty("hikari.minimumIdle", "5")));
            config.setIdleTimeout(Long.parseLong(properties.getProperty("hikari.idleTimeout", "30000")));
            config.setConnectionTimeout(Long.parseLong(properties.getProperty("hikari.connectionTimeout", "20000")));
            config.setMaxLifetime(Long.parseLong(properties.getProperty("hikari.maxLifetime", "1800000")));
            config.setPoolName("LIMS-HikariPool");

            dataSource = new HikariDataSource(config);
            LOGGER.info("HikariCP DataSource successfully initialized for LIMS.");
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to setup HikariCP pool. Will use DriverManager fallback.", ex);
            dataSource = null;
        }
    }

    /**
     * Obtains a SQL connection from HikariCP pool or direct DriverManager.
     *
     * @return Connection object
     * @throws SQLException on database failure
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource != null && !dataSource.isClosed()) {
            return dataSource.getConnection();
        }

        // Fallback to direct JDBC connection
        String server = properties.getProperty("db.serverName", "localhost");
        String port = properties.getProperty("db.portNumber", "1433");
        String database = properties.getProperty("db.databaseName", "LIMS_DB");
        String user = properties.getProperty("db.username", "sa");
        String pass = properties.getProperty("db.password", "YourStrong@Pass123");
        String encrypt = properties.getProperty("db.encrypt", "false");
        String trustCert = properties.getProperty("db.trustServerCertificate", "true");

        String jdbcUrl = String.format(
                "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s;trustServerCertificate=%s;",
                server, port, database, encrypt, trustCert
        );
        return DriverManager.getConnection(jdbcUrl, user, pass);
    }

    /**
     * Safely closes connection, statement, and result set.
     */
    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Shuts down connection pool when application undeploys.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("HikariCP DataSource closed.");
        }
    }
}

