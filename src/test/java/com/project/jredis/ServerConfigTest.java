package com.project.jredis;

import com.project.jredis.config.ServerConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerConfigTest {

    @Test
    void defaultsAreSensible() {
        ServerConfig config = new ServerConfig();
        assertEquals(6380, config.getPort());
        assertEquals(50, config.getMaxConnections());
        assertEquals(".", config.getPersistenceDirectory());
        assertEquals("jredis.rdb", config.getSnapshotFilename());
        assertEquals(0, config.getSnapshotIntervalSeconds());
    }

    @Test
    void settersUpdateValues() {
        ServerConfig config = new ServerConfig();
        config.setPort(7000);
        config.setMaxConnections(100);
        assertEquals(7000, config.getPort());
        assertEquals(100, config.getMaxConnections());
    }
}