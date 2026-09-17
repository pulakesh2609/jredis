package com.project.jredis.server;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ServerStats {

    private final long startTimeMillis = System.currentTimeMillis();
    private final AtomicInteger connectedClients = new AtomicInteger(0);
    private final AtomicLong totalCommandsProcessed = new AtomicLong(0);

    public void clientConnected() {
        connectedClients.incrementAndGet();
    }

    public void clientDisconnected() {
        connectedClients.decrementAndGet();
    }

    public void commandProcessed() {
        totalCommandsProcessed.incrementAndGet();
    }

    public int getConnectedClients() {
        return connectedClients.get();
    }

    public long getTotalCommandsProcessed() {
        return totalCommandsProcessed.get();
    }

    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - startTimeMillis) / 1000;
    }
}