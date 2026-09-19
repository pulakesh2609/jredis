package com.project.jredis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jredis")
public class ServerConfig {

    private int port = 6380;
    private int maxConnections = 50;
    private String persistenceDirectory = ".";
    private String snapshotFilename = "jredis.rdb";
    private int snapshotIntervalSeconds = 0; // 0 disables periodic auto-save

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getPersistenceDirectory() {
        return persistenceDirectory;
    }

    public void setPersistenceDirectory(String persistenceDirectory) {
        this.persistenceDirectory = persistenceDirectory;
    }

    public String getSnapshotFilename() {
        return snapshotFilename;
    }

    public void setSnapshotFilename(String snapshotFilename) {
        this.snapshotFilename = snapshotFilename;
    }

    public int getSnapshotIntervalSeconds() {
        return snapshotIntervalSeconds;
    }

    public void setSnapshotIntervalSeconds(int snapshotIntervalSeconds) {
        this.snapshotIntervalSeconds = snapshotIntervalSeconds;
    }
}