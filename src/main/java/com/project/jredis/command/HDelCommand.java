package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisHash;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class HDelCommand implements Command {

    private final Database database;

    public HDelCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "HDEL";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() < 2) {
            return new RespError("ERR wrong number of arguments for 'hdel' command");
        }
        String key = args.get(0);
        List<String> fields = args.subList(1, args.size());
        AtomicInteger removedCount = new AtomicInteger();

        try {
            database.compute(key, (k, existing) -> {
                if (existing == null) {
                    return null;
                }
                if (!(existing instanceof RedisHash hash)) {
                    throw new IllegalStateException("WRONGTYPE");
                }
                for (String field : fields) {
                    if (hash.values().remove(field) != null) {
                        removedCount.incrementAndGet();
                    }
                }
                return hash.values().isEmpty() ? null : hash;
            });
        } catch (IllegalStateException e) {
            return new RespError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        return new RespInteger(removedCount.get());
    }
}