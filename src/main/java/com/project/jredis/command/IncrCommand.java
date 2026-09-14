package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisString;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class IncrCommand implements Command {

    private final Database database;

    public IncrCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "INCR";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 1) {
            return new RespError("ERR wrong number of arguments for 'incr' command");
        }
        String key = args.get(0);
        AtomicLong newValue = new AtomicLong();

        try {
            database.compute(key, (k, existing) -> {
                long current = 0;
                if (existing != null) {
                    if (!(existing instanceof RedisString rs)) {
                        throw new IllegalStateException("WRONGTYPE");
                    }
                    current = Long.parseLong(rs.value());
                }
                long updated = current + 1;
                newValue.set(updated);
                return new RedisString(String.valueOf(updated));
            });
        } catch (IllegalStateException e) {
            return new RespError("WRONGTYPE Operation against a key holding the wrong kind of value");
        } catch (NumberFormatException e) {
            return new RespError("ERR value is not an integer or out of range");
        }

        return new RespInteger(newValue.get());
    }
}