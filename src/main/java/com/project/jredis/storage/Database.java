package com.project.jredis.storage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

@Component
public class Database {

    private final Map<String, RedisValue> data = new ConcurrentHashMap<>();
    private final Map<String, Long> expirations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    void startActiveExpiration() {
        cleanupExecutor.scheduleAtFixedRate(this::sweepExpiredKeys, 1, 1, TimeUnit.SECONDS);
    }

    @PreDestroy
    void stopActiveExpiration() {
        cleanupExecutor.shutdown();
    }

    private void sweepExpiredKeys() {
        for (String key : expirations.keySet()) {
            expireIfNeeded(key);
        }
    }

    private boolean isExpired(String key) {
        Long expiry = expirations.get(key);
        return expiry != null && System.currentTimeMillis() >= expiry;
    }

    private void expireIfNeeded(String key) {
        if (isExpired(key)) {
            data.remove(key);
            expirations.remove(key);
        }
    }

    public RedisValue get(String key) {
        expireIfNeeded(key);
        return data.get(key);
    }

    public void put(String key, RedisValue value) {
        expirations.remove(key);
        data.put(key, value);
    }

    public boolean delete(String key) {
        expirations.remove(key);
        return data.remove(key) != null;
    }

    public boolean exists(String key) {
        expireIfNeeded(key);
        return data.containsKey(key);
    }

    public Set<String> keys() {
        sweepExpiredKeys();
        return data.keySet();
    }

    public int size() {
        sweepExpiredKeys();
        return data.size();
    }

    public RedisValue compute(String key, BiFunction<String, RedisValue, RedisValue> remappingFunction) {
        expireIfNeeded(key);
        return data.compute(key, remappingFunction);
    }

    public boolean setExpiration(String key, long expiryEpochMillis) {
        expireIfNeeded(key);
        if (!data.containsKey(key)) {
            return false;
        }
        expirations.put(key, expiryEpochMillis);
        return true;
    }

    public Long getExpiration(String key) {
        expireIfNeeded(key);
        return expirations.get(key);
    }

    public boolean persist(String key) {
        expireIfNeeded(key);
        return expirations.remove(key) != null;
    }

    public Map<String, RedisValue> snapshotEntries() {
        sweepExpiredKeys(); // never persist keys that have already expired
        return new HashMap<>(data); // defensive copy for the snapshot writer to iterate independently
    }

    public void restoreEntry(String key, RedisValue value, Long expiryEpochMillis) {
        data.put(key, value);
        if (expiryEpochMillis != null) {
            expirations.put(key, expiryEpochMillis);
        }
    }

    public void clear() {
        data.clear();
        expirations.clear();
    }
}