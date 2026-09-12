package com.project.jredis.protocol;

import java.nio.charset.StandardCharsets;

public class RespEncoder {

    public String encode(RespValue value) {
        if (value instanceof RespSimpleString s) {
            return "+" + s.value() + "\r\n";
        }
        if (value instanceof RespError e) {
            return "-" + e.message() + "\r\n";
        }
        if (value instanceof RespInteger i) {
            return ":" + i.value() + "\r\n";
        }
        if (value instanceof RespBulkString b) {
            return encodeBulkString(b);
        }
        if (value instanceof RespArray a) {
            return encodeArray(a);
        }
        throw new IllegalArgumentException("Unsupported RespValue type: " + value.getClass());
    }

    private String encodeBulkString(RespBulkString bulkString) {
        if (bulkString.value() == null) {
            return "$-1\r\n";
        }
        byte[] bytes = bulkString.value().getBytes(StandardCharsets.UTF_8);
        return "$" + bytes.length + "\r\n" + bulkString.value() + "\r\n";
    }

    private String encodeArray(RespArray array) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(array.values().size()).append("\r\n");
        for (RespValue element : array.values()) {
            sb.append(encode(element)); // recursion
        }
        return sb.toString();
    }
}