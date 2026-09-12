package com.project.jredis.protocol;

import java.io.BufferedReader;
import java.io.IOException;

public class RespParser {

    public RespValue parse(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Client disconnected mid-message");
        }
        if (line.isEmpty()) {
            throw new IllegalArgumentException("Empty RESP line");
        }

        char prefix = line.charAt(0);
        String body = line.substring(1);

        return switch (prefix) {
            case '+' -> new RespSimpleString(body);
            case '-' -> new RespError(body);
            case ':' -> new RespInteger(Long.parseLong(body));
            default -> throw new IllegalArgumentException("Unknown RESP type prefix: " + prefix);
        };
    }
}