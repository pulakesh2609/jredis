package com.project.jredis.command;

import com.project.jredis.storage.Database;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.storage.RedisList;
import com.project.jredis.storage.RedisString;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class IncrCommandTest {

    private final Database database = new Database();
    private final IncrCommand incrCommand = new IncrCommand(database);

    @Test
    void missingKeyStartsAtOne() {
        assertEquals(new RespInteger(1), incrCommand.execute(List.of("counter")));
    }

    @Test
    void existingNumericValueIncrements() {
        database.put("counter", new RedisString("41"));
        assertEquals(new RespInteger(42), incrCommand.execute(List.of("counter")));
    }

    @Test
    void nonNumericValueReturnsError() {
        database.put("counter", new RedisString("notanumber"));
        assertInstanceOf(RespError.class, incrCommand.execute(List.of("counter")));
    }

    @Test
    void wrongTypeReturnsWrongTypeError() {
        database.put("mylist", new RedisList(new ArrayList<>(List.of("a"))));
        RespError result = (RespError) incrCommand.execute(List.of("mylist"));
        assertTrue(result.message().startsWith("WRONGTYPE"));
    }
}