package com.project.jredis.storage;

import java.util.Set;

public record RedisSet(Set<String> values) implements RedisValue {
    @Override
    public String typeName() {
        return "set";
    }
}