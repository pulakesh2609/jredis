package com.project.jredis.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RespEncoderTest {

    private final RespEncoder encoder = new RespEncoder();

    @Test
    void encodesSimpleString() {
        assertEquals("+OK\r\n", encoder.encode(new RespSimpleString("OK")));
    }

    @Test
    void encodesError() {
        assertEquals("-ERR unknown command\r\n", encoder.encode(new RespError("ERR unknown command")));
    }

    @Test
    void encodesInteger() {
        assertEquals(":1000\r\n", encoder.encode(new RespInteger(1000)));
    }
}