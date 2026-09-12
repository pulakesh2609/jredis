package com.project.jredis.protocol;

import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RespParserTest {

    private final RespParser parser = new RespParser();

    @Test
    void parsesSimpleString() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("+OK\r\n"));
        assertEquals(new RespSimpleString("OK"), parser.parse(reader));
    }

    @Test
    void parsesError() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("-ERR unknown command\r\n"));
        assertEquals(new RespError("ERR unknown command"), parser.parse(reader));
    }

    @Test
    void parsesInteger() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(":1000\r\n"));
        assertEquals(new RespInteger(1000), parser.parse(reader));
    }
}