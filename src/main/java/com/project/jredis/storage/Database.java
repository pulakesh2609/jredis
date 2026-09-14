package com.project.jredis.storage;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class Database {

    // Deliberately a plain HashMap for now — Phase 5 explains exactly why this
    // breaks once multiple client threads touch it at once, and fixes it properly.
    private final Map<String, String> data = new HashMap<>();

    public void set(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key); // null if absent
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
}