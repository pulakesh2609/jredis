package com.project.jredis.command;

import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespSimpleString;
import com.project.jredis.storage.Database;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExpirationCommandsTest {

    private final Database database = new Database();
    private final SetCommand set = new SetCommand(database);
    private final ExpireCommand expire = new ExpireCommand(database);
    private final TtlCommand ttl = new TtlCommand(database);
    private final PttlCommand pttl = new PttlCommand(database);
    private final PersistCommand persist = new PersistCommand(database);

    @Test
    void ttlOnKeyWithNoExpiryIsNegativeOne() {
        set.execute(List.of("key", "value"));
        assertEquals(new RespInteger(-1), ttl.execute(List.of("key")));
    }

    @Test
    void ttlOnMissingKeyIsNegativeTwo() {
        assertEquals(new RespInteger(-2), ttl.execute(List.of("nope")));
    }

    @Test
    void expireSetsATtlThatCountsDown() {
        set.execute(List.of("key", "value"));
        assertEquals(new RespInteger(1), expire.execute(List.of("key", "100")));

        RespInteger result = (RespInteger) ttl.execute(List.of("key"));
        assertTrue(result.value() > 0 && result.value() <= 100);
    }

    @Test
    void expireOnMissingKeyReturnsZero() {
        assertEquals(new RespInteger(0), expire.execute(List.of("nope", "100")));
    }

    @Test
    void keyActuallyExpiresAndDisappears() throws InterruptedException {
        set.execute(List.of("key", "value"));
        expire.execute(List.of("key", "0"));
        Thread.sleep(50);
        assertFalse(database.exists("key"));
    }

    @Test
    void persistRemovesExpiry() {
        set.execute(List.of("key", "value"));
        expire.execute(List.of("key", "100"));
        assertEquals(new RespInteger(1), persist.execute(List.of("key")));
        assertEquals(new RespInteger(-1), ttl.execute(List.of("key")));
    }

    @Test
    void plainSetClearsAnyExistingTtl() {
        set.execute(List.of("key", "value"));
        expire.execute(List.of("key", "100"));
        set.execute(List.of("key", "newvalue"));
        assertEquals(new RespInteger(-1), ttl.execute(List.of("key")));
    }

    @Test
    void setWithExInlineExpiry() {
        assertEquals(new RespSimpleString("OK"), set.execute(List.of("key", "value", "EX", "100")));
        RespInteger result = (RespInteger) ttl.execute(List.of("key"));
        assertTrue(result.value() > 0 && result.value() <= 100);
    }

    @Test
    void pttlReturnsMilliseconds() {
        set.execute(List.of("key", "value"));
        expire.execute(List.of("key", "100"));
        RespInteger result = (RespInteger) pttl.execute(List.of("key"));
        assertTrue(result.value() > 0 && result.value() <= 100_000);
    }
}