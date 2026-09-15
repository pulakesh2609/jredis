package com.project.jredis.command;

import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisList;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class RPopCommand implements Command {

    private final Database database;

    public RPopCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "RPOP";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 1) {
            return new RespError("ERR wrong number of arguments for 'rpop' command");
        }
        String key = args.get(0);
        AtomicReference<String> popped = new AtomicReference<>();

        try {
            database.compute(key, (k, existing) -> {
                if (existing == null) {
                    return null;
                }
                if (!(existing instanceof RedisList list)) {
                    throw new IllegalStateException("WRONGTYPE");
                }
                if (list.values().isEmpty()) {
                    return list;
                }
                int lastIndex = list.values().size() - 1;
                popped.set(list.values().remove(lastIndex));
                return list.values().isEmpty() ? null : list;
            });
        } catch (IllegalStateException e) {
            return new RespError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        return new RespBulkString(popped.get());
    }
}