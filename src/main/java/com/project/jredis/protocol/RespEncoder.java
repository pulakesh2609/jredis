package com.project.jredis.protocol;

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
        throw new IllegalArgumentException("Unsupported RespValue type: " + value.getClass());
    }
}