package com.project.jredis.protocol;

import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RespBulkStringAndArrayTest {

    private final RespParser parser = new RespParser();
    private final RespEncoder encoder = new RespEncoder();

    @Test
    void parsesBulkString() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("$4\r\nJohn\r\n"));
        assertEquals(new RespBulkString("John"), parser.parse(reader));
    }

    @Test
    void parsesNilBulkString() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("$-1\r\n"));
        assertEquals(new RespBulkString(null), parser.parse(reader));
    }

    @Test
    void encodesBulkString() {
        assertEquals("$4\r\nJohn\r\n", encoder.encode(new RespBulkString("John")));
    }

    @Test
    void encodesNilBulkString() {
        assertEquals("$-1\r\n", encoder.encode(new RespBulkString(null)));
    }

    @Test
    void parsesArrayOfBulkStrings() throws IOException {
        // exactly what a real client sends for: SET name John
        String raw = "*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$4\r\nJohn\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(raw));
        RespValue result = parser.parse(reader);

        RespArray expected = new RespArray(List.of(
                new RespBulkString("SET"),
                new RespBulkString("name"),
                new RespBulkString("John")
        ));
        assertEquals(expected, result);
    }

    @Test
    void parsesNestedArray() throws IOException {
        String raw = "*1\r\n*1\r\n:42\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(raw));
        RespValue result = parser.parse(reader);

        RespArray expected = new RespArray(List.of(
                new RespArray(List.of(new RespInteger(42)))
        ));
        assertEquals(expected, result);
    }
}