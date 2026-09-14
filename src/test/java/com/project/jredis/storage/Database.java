package com.project.jredis.storage;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Database {

    private final Map<String, String> data = new ConcurrentHashMap<>();

    public void set(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
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