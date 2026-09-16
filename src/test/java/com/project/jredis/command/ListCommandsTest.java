package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.storage.RedisString;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import com.project.jredis.storage.Database;

class ListCommandsTest {

    private final Database database = new Database();
    private final LPushCommand lpush = new LPushCommand(database);
    private final RPushCommand rpush = new RPushCommand(database);
    private final LPopCommand lpop = new LPopCommand(database);
    private final RPopCommand rpop = new RPopCommand(database);
    private final LRangeCommand lrange = new LRangeCommand(database);

    @Test
    void lpushInsertsAtHeadAndReportsNewSize() {
        assertEquals(new RespInteger(1), lpush.execute(List.of("mylist", "a")));
        assertEquals(new RespInteger(2), lpush.execute(List.of("mylist", "b")));
        assertEquals(new RespBulkString("b"), lpop.execute(List.of("mylist")));
    }

    @Test
    void rpushAppendsAtTail() {
        rpush.execute(List.of("mylist", "a"));
        rpush.execute(List.of("mylist", "b"));
        assertEquals(new RespBulkString("a"), lpop.execute(List.of("mylist")));
        assertEquals(new RespBulkString("b"), lpop.execute(List.of("mylist")));
    }

    @Test
    void popOnMissingKeyReturnsNil() {
        assertEquals(new RespBulkString(null), lpop.execute(List.of("nope")));
    }

    @Test
    void listKeyDisappearsOnceEmptied() {
        rpush.execute(List.of("temp", "only"));
        rpop.execute(List.of("temp"));
        assertFalse(database.exists("temp"));
    }

    @Test
    void wrongTypeOnNonListKey() {
        database.put("str", new RedisString("hello"));
        assertInstanceOf(RespError.class, lpush.execute(List.of("str", "x")));
    }

    @Test
    void lrangeSupportsNegativeIndices() {
        rpush.execute(List.of("mylist", "a", "b", "c", "d"));
        RespArray result = (RespArray) lrange.execute(List.of("mylist", "-2", "-1"));
        assertEquals(List.of(new RespBulkString("c"), new RespBulkString("d")), result.values());
    }
    @Test
    void llenReportsLengthAndZeroForMissingKey() {
        assertEquals(new RespInteger(0), new LLenCommand(database).execute(List.of("nope")));
        rpush.execute(List.of("mylist", "a", "b", "c"));
        assertEquals(new RespInteger(3), new LLenCommand(database).execute(List.of("mylist")));
    }
}