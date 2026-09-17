package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespSimpleString;
import com.project.jredis.server.ServerStats;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisString;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ServerCommandsTest {

    private final Database database = new Database();
    private final ServerStats stats = new ServerStats();

    @Test
    void infoContainsExpectedFields() {
        InfoCommand info = new InfoCommand(stats, database);
        RespBulkString result = (RespBulkString) info.execute(List.of());
        String output = result.value();

        assertTrue(output.contains("jredis_version"));
        assertTrue(output.contains("uptime_in_seconds"));
        assertTrue(output.contains("connected_clients"));
        assertTrue(output.contains("total_commands_processed"));
    }

    @Test
    void flushdbClearsEverything() {
        database.put("key1", new RedisString("value"));
        database.put("key2", new RedisString("value"));
        assertEquals(2, database.size());

        FlushDbCommand flush = new FlushDbCommand(database);
        assertEquals(new RespSimpleString("OK"), flush.execute(List.of()));
        assertEquals(0, database.size());
    }

    @Test
    void timeReturnsTwoElementArray() {
        TimeCommand time = new TimeCommand();
        RespArray result = (RespArray) time.execute(List.of());
        assertEquals(2, result.values().size());
    }

    @Test
    void commandListsAllRegisteredCommands() {
        CommandRegistry registry = new CommandRegistry(List.of(new PingCommand(), new EchoCommand()));
        CommandListCommand commandList = new CommandListCommand(registry);
        RespArray result = (RespArray) commandList.execute(List.of());
        assertEquals(2, result.values().size());
    }
}