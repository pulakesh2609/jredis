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

    @Test
    void tracksTotalConnectionsEverSeparatelyFromCurrentlyConnected() {
        ServerStats stats = new ServerStats();
        stats.clientConnected();
        stats.clientConnected();
        stats.clientDisconnected();

        assertEquals(1, stats.getConnectedClients());
        assertEquals(2, stats.getTotalConnectionsEver()); // never decrements
    }

    @Test
    void tracksErrorsSeparatelyFromTotalCommands() {
        ServerStats stats = new ServerStats();
        stats.commandProcessed(1_000_000, false);
        stats.commandProcessed(1_000_000, true);
        stats.commandProcessed(1_000_000, false);

        assertEquals(3, stats.getTotalCommandsProcessed());
        assertEquals(1, stats.getTotalErrors());
    }

    @Test
    void tracksAverageAndMaxLatency() {
        ServerStats stats = new ServerStats();
        stats.commandProcessed(1_000_000, false); // 1ms
        stats.commandProcessed(3_000_000, false); // 3ms

        assertEquals(2.0, stats.getAverageLatencyMillis(), 0.001);
        assertEquals(3.0, stats.getMaxLatencyMillis(), 0.001);
    }

}







