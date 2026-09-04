package com.project.jredis;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerConfigTest {
    @Test
    void defaultPortIsSet() {
        ServerConfig config = new ServerConfig();
        assertEquals(6380, config.getPort());
    }
}