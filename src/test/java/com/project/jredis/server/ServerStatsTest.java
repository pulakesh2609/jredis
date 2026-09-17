package com.project.jredis.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServerStatsTest {

    @Test
    void tracksConnectedClientsAccurately() {
        ServerStats stats = new ServerStats();
        assertEquals(0, stats.getConnectedClients());

        stats.clientConnected();
        stats.clientConnected();
        assertEquals(2, stats.getConnectedClients());

        stats.clientDisconnected();
        assertEquals(1, stats.getConnectedClients());
    }

    @Test
    void tracksTotalCommandsProcessed() {
        ServerStats stats = new ServerStats();
        assertEquals(0, stats.getTotalCommandsProcessed());

        stats.commandProcessed();
        stats.commandProcessed();
        stats.commandProcessed();
        assertEquals(3, stats.getTotalCommandsProcessed());
    }

    @Test
    void uptimeIsNonNegativeAndIncreases() throws InterruptedException {
        ServerStats stats = new ServerStats();
        long firstReading = stats.getUptimeSeconds();
        assertTrue(firstReading >= 0);
        Thread.sleep(1100);
        long secondReading = stats.getUptimeSeconds();
        assertTrue(secondReading >= firstReading);
    }
}