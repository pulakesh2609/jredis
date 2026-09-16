package com.project.jredis.command;

import com.project.jredis.storage.RedisHash;
import com.project.jredis.storage.RedisValue;
import java.util.LinkedHashMap;

final class HashTypeSupport {

    private HashTypeSupport() {
    }

    static RedisHash asHashOrCreate(RedisValue existing) {
        if (existing == null) {
            return new RedisHash(new LinkedHashMap<>());
        }
        if (existing instanceof RedisHash hash) {
            return hash;
        }
        throw new IllegalStateException("WRONGTYPE");
    }
}