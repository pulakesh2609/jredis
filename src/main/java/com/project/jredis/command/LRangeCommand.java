package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisList;
import com.project.jredis.storage.RedisValue;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class LRangeCommand implements Command {

    private final Database database;

    public LRangeCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "LRANGE";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 3) {
            return new RespError("ERR wrong number of arguments for 'lrange' command");
        }

        RedisValue stored = database.get(args.get(0));
        if (stored == null) {
            return new RespArray(List.of());
        }
        if (!(stored instanceof RedisList redisList)) {
            return new RespError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        int start;
        int stop;
        try {
            start = Integer.parseInt(args.get(1));
            stop = Integer.parseInt(args.get(2));
        } catch (NumberFormatException e) {
            return new RespError("ERR value is not an integer or out of range");
        }

        List<String> source = redisList.values();
        int size = source.size();

        start = normalizeIndex(start, size);
        stop = normalizeIndex(stop, size);
        start = Math.max(start, 0);
        stop = Math.min(stop, size - 1);

        if (start > stop || size == 0) {
            return new RespArray(List.of());
        }

        List<RespValue> result = new ArrayList<>();
        for (int i = start; i <= stop; i++) {
            result.add(new RespBulkString(source.get(i)));
        }
        return new RespArray(result);
    }

    private int normalizeIndex(int index, int size) {
        return index < 0 ? size + index : index;
    }
}