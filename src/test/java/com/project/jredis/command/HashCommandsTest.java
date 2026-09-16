package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisString;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HashCommandsTest {

    private final Database database = new Database();
    private final HSetCommand hset = new HSetCommand(database);
    private final HGetCommand hget = new HGetCommand(database);
    private final HDelCommand hdel = new HDelCommand(database);
    private final HGetAllCommand hgetall = new HGetAllCommand(database);

    @Test
    void hsetReportsWhetherFieldWasNew() {
        assertEquals(new RespInteger(1), hset.execute(List.of("user:1", "name", "John")));
        assertEquals(new RespInteger(0), hset.execute(List.of("user:1", "name", "Jane")));
    }

    @Test
    void hgetReturnsFieldValue() {
        hset.execute(List.of("user:1", "name", "John"));
        assertEquals(new RespBulkString("John"), hget.execute(List.of("user:1", "name")));
    }

    @Test
    void hgetOnMissingFieldReturnsNil() {
        hset.execute(List.of("user:1", "name", "John"));
        assertEquals(new RespBulkString(null), hget.execute(List.of("user:1", "age")));
    }

    @Test
    void hdelRemovesAndCounts() {
        hset.execute(List.of("user:1", "name", "John"));
        hset.execute(List.of("user:1", "age", "30"));
        assertEquals(new RespInteger(1), hdel.execute(List.of("user:1", "age", "missing")));
    }

    @Test
    void hashKeyDisappearsOnceEmptied() {
        hset.execute(List.of("temp", "f", "v"));
        hdel.execute(List.of("temp", "f"));
        assertFalse(database.exists("temp"));
    }

    @Test
    void hgetallReturnsFlatFieldValuePairs() {
        hset.execute(List.of("user:1", "name", "John"));
        hset.execute(List.of("user:1", "age", "30"));
        RespArray result = (RespArray) hgetall.execute(List.of("user:1"));
        assertEquals(
                List.of(new RespBulkString("name"), new RespBulkString("John"),
                        new RespBulkString("age"), new RespBulkString("30")),
                result.values()
        );
    }

    @Test
    void wrongTypeOnNonHashKey() {
        database.put("str", new RedisString("hello"));
        assertInstanceOf(RespError.class, hset.execute(List.of("str", "f", "v")));
    }
}