package com.project.jredis.server;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ServerStats {

    private final long startTimeMillis = System.currentTimeMillis();
    private final AtomicInteger connectedClients = new AtomicInteger(0);
    private final AtomicLong totalConnectionsEver = new AtomicLong(0);
    private final AtomicLong totalCommandsProcessed = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalLatencyNanos = new AtomicLong(0);
    private final AtomicLong maxLatencyNanos = new AtomicLong(0);

    public void clientConnected() {
        connectedClients.incrementAndGet();
        totalConnectionsEver.incrementAndGet();
    }

    public void clientDisconnected() {
        connectedClients.decrementAndGet();
    }

    public void commandProcessed(long latencyNanos, boolean wasError) {
        totalCommandsProcessed.incrementAndGet();
        totalLatencyNanos.addAndGet(latencyNanos);
        if (wasError) {
            totalErrors.incrementAndGet();
        }
        maxLatencyNanos.updateAndGet(current -> Math.max(current, latencyNanos));
    }

    public int getConnectedClients() {
        return connectedClients.get();
    }

    public long getTotalConnectionsEver() {
        return totalConnectionsEver.get();
    }

    public long getTotalCommandsProcessed() {
        return totalCommandsProcessed.get();
    }

    public long getTotalErrors() {
        return totalErrors.get();
    }

    public double getAverageLatencyMillis() {
        long count = totalCommandsProcessed.get();
        if (count == 0) {
            return 0.0;
        }
        return (totalLatencyNanos.get() / (double) count) / 1_000_000.0;
    }

    public double getMaxLatencyMillis() {
        return maxLatencyNanos.get() / 1_000_000.0;
    }

    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - startTimeMillis) / 1000;
    }
}