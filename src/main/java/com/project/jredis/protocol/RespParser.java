package com.project.jredis.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RespParser {

    public RespValue parse(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null; // clean disconnect — matches BufferedReader's own convention
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
            case '$' -> parseBulkString(reader, body);
            case '*' -> parseArray(reader, body);
            default -> throw new IllegalArgumentException("Unknown RESP type prefix: " + prefix);
        };
    }

    private RespBulkString parseBulkString(BufferedReader reader, String lengthPart) throws IOException {
        int length = Integer.parseInt(lengthPart);
        if (length == -1) {
            return new RespBulkString(null); // nil
        }
        char[] buffer = new char[length];
        int read = 0;
        while (read < length) {
            int n = reader.read(buffer, read, length - read);
            if (n == -1) {
                throw new IOException("Client disconnected mid-bulk-string");
            }
            read += n;
        }
        reader.readLine(); // consume the trailing CRLF after the data
        return new RespBulkString(new String(buffer));
    }

    private RespArray parseArray(BufferedReader reader, String countPart) throws IOException {
        int count = Integer.parseInt(countPart);
        List<RespValue> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(parse(reader));
        }
        return new RespArray(values);
    }
}