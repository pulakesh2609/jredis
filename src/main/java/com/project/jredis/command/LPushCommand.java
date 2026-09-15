package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisList;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LPushCommand implements Command {

    private final Database database;

    public LPushCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "LPUSH";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() < 2) {
            return new RespError("ERR wrong number of arguments for 'lpush' command");
        }
        String key = args.get(0);
        List<String> valuesToPush = args.subList(1, args.size());
        AtomicInteger newSize = new AtomicInteger();

        try {
            database.compute(key, (k, existing) -> {
                RedisList list = listCommandSupport.asListOrCreate(existing);
                for (String value : valuesToPush) {
                    list.values().add(0, value); // each one lands at the head
                }
                newSize.set(list.values().size());
                return list;
            });
        } catch (IllegalStateException e) {
            return new RespError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        return new RespInteger(newSize.get());
    }
}