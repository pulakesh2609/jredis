package com.project.jredis.storage;

import java.util.Map;

public record RedisHash(Map<String, String> values) implements RedisValue {
    @Override
    public String typeName() {
        return "hash";
    }
}