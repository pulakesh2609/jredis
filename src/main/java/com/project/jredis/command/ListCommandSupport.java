package com.project.jredis.command;

import com.project.jredis.storage.RedisList;
import com.project.jredis.storage.RedisValue;
import java.util.ArrayList;

final class ListCommandSupport {

    private ListCommandSupport() {
    }

    static RedisList asListOrCreate(RedisValue existing) {
        if (existing == null) {
            return new RedisList(new ArrayList<>());
        }
        if (existing instanceof RedisList list) {
            return list;
        }
        throw new IllegalStateException("WRONGTYPE");
    }
}