package com.project.jredis.storage;

public record RedisString(String value) implements RedisValue {
    @Override
    public String typeName() {
        return "string";
    }
}