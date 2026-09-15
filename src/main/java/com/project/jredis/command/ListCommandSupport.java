package com.project.jredis.command;
import com.project.jredis.storage.RedisList;
import java.util.ArrayList;


public class ListCommandSupport {
}

final class listCommandSupport {

    private listCommandSupport() {
    }

    static <RedisValue> RedisList asListOrCreate(RedisValue existing) {
        if (existing == null) {
            return new RedisList(new ArrayList<>());
        }
        if (existing instanceof RedisList list) {
            return list;
        }
        throw new IllegalStateException("WRONGTYPE");
    }
}