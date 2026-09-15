package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisList;
import com.project.jredis.storage.RedisString;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DecrCommandTest {

    private final Database database = new Database();
    private final DecrCommand decrCommand = new DecrCommand(database);

    @Test
    void missingKeyStartsAtNegativeOne() {
        assertEquals(new RespInteger(-1), decrCommand.execute(List.of("counter")));
    }

    @Test
    void existingNumericValueDecrements() {
        database.put("counter", new RedisString("42"));
        assertEquals(new RespInteger(41), decrCommand.execute(List.of("counter")));
    }

    @Test
    void nonNumericValueReturnsError() {
        database.put("counter", new RedisString("notanumber"));
        assertInstanceOf(RespError.class, decrCommand.execute(List.of("counter")));
    }

    @Test
    void wrongTypeReturnsWrongTypeError() {
        database.put("mylist", new RedisList(new ArrayList<>(List.of("a"))));
        RespError result = (RespError) decrCommand.execute(List.of("mylist"));
        assertTrue(result.message().startsWith("WRONGTYPE"));
    }
}