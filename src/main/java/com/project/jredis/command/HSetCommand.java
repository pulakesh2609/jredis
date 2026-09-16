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
public class HSetCommand implements Command {

    private final Database database;

    public HSetCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "HSET";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 3) {
            return new RespError("ERR wrong number of arguments for 'hset' command");
        }
        String key = args.get(0);
        String field = args.get(1);
        String value = args.get(2);
        AtomicInteger isNewField = new AtomicInteger();

        try {
            database.compute(key, (k, existing) -> {
                RedisHash hash = HashTypeSupport.asHashOrCreate(existing);
                String previous = hash.values().put(field, value);
                isNewField.set(previous == null ? 1 : 0);
                return hash;
            });
        } catch (IllegalStateException e) {
            return new RespError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        return new RespInteger(isNewField.get());
    }
}