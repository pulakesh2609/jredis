package com.project.jredis.storage;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Component
public class Database {

    private final Map<String, RedisValue> data = new ConcurrentHashMap<>();

    public RedisValue get(String key) {
        return data.get(key);
    }

    public void put(String key, RedisValue value) {
        data.put(key, value);
    }

    public boolean delete(String key) {
        return data.remove(key) != null;
    }

    public boolean exists(String key) {
        return data.containsKey(key);
    }

    public Set<String> keys() {
        return data.keySet();
    }

    public int size() {
        return data.size();
    }

    public RedisValue compute(String key, BiFunction<String, RedisValue, RedisValue> remappingFunction) {
        return data.compute(key, remappingFunction);
    }
}