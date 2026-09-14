package com.project.jredis.storage;

import java.util.List;

public record RedisList(List<String> values) implements RedisValue {
    @Override
    public String typeName() {
        return "list";
    }
}