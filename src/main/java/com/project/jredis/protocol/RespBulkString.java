package com.project.jredis.protocol;

public record RespBulkString(String value) implements RespValue {
}