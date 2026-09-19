package com.project.jredis.command;

import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.server.ServerStats;
import com.project.jredis.storage.Database;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class InfoCommand implements Command {

    private static final String SERVER_VERSION = "0.1.0";

    private final ServerStats stats;
    private final Database database;

    public InfoCommand(ServerStats stats, Database database) {
        this.stats = stats;
        this.database = database;
    }

    @Override
    public String name() {
        return "INFO";
    }

    @Override
    public RespValue execute(List<String> args) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory();

        String info = """
            # Server
            jredis_version:%s
            uptime_in_seconds:%d

            # Clients
            connected_clients:%d
            total_connections_received:%d

            # Stats
            total_commands_processed:%d
            total_errors:%d
            avg_latency_ms:%.3f
            max_latency_ms:%.3f

            # Keyspace
            db0:keys=%d

            # Memory
            used_memory_bytes:%d
            """.formatted(
                SERVER_VERSION,
                stats.getUptimeSeconds(),
                stats.getConnectedClients(),
                stats.getTotalConnectionsEver(),
                stats.getTotalCommandsProcessed(),
                stats.getTotalErrors(),
                stats.getAverageLatencyMillis(),
                stats.getMaxLatencyMillis(),
                database.size(),
                usedMemoryBytes
        );

        return new RespBulkString(info);
    }
}