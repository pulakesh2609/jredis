package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespSimpleString;
import com.project.jredis.protocol.RespValue;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import com.project.jredis.storage.Database;
class StorageCommandsTest {

    private final Database database = new Database();
    private final SetCommand setCommand = new SetCommand(database);
    private final GetCommand getCommand = new GetCommand(database);
    private final DelCommand delCommand = new DelCommand(database);
    private final ExistsCommand existsCommand = new ExistsCommand(database);
    private final KeysCommand keysCommand = new KeysCommand(database);
    private final DbSizeCommand dbSizeCommand = new DbSizeCommand(database);

    @Test
    void setThenGetRoundTrips() {
        assertEquals(new RespSimpleString("OK"), setCommand.execute(List.of("name", "John")));
        assertEquals(new RespBulkString("John"), getCommand.execute(List.of("name")));
    }

    @Test
    void getOnMissingKeyReturnsNil() {
        assertEquals(new RespBulkString(null), getCommand.execute(List.of("doesNotExist")));
    }

    @Test
    void existsReflectsCurrentState() {
        assertEquals(new RespInteger(0), existsCommand.execute(List.of("temp")));
        setCommand.execute(List.of("temp", "value"));
        assertEquals(new RespInteger(1), existsCommand.execute(List.of("temp")));
    }

    @Test
    void delRemovesAndReportsWhetherItExisted() {
        setCommand.execute(List.of("gone", "soon"));
        assertEquals(new RespInteger(1), delCommand.execute(List.of("gone")));
        assertEquals(new RespInteger(0), delCommand.execute(List.of("gone"))); // already gone
    }

    @Test
    void dbSizeTracksEntryCount() {
        assertEquals(new RespInteger(0), dbSizeCommand.execute(List.of()));
        setCommand.execute(List.of("a", "1"));
        setCommand.execute(List.of("b", "2"));
        assertEquals(new RespInteger(2), dbSizeCommand.execute(List.of()));
    }

    @Test
    void keysReturnsArrayOfAllKeys() {
        setCommand.execute(List.of("x", "1"));
        RespValue result = keysCommand.execute(List.of());
        assertInstanceOf(RespArray.class, result);
        assertEquals(1, ((RespArray) result).values().size());
    }
}