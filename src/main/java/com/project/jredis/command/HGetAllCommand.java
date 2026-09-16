package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisHash;
import com.project.jredis.storage.RedisValue;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HGetAllCommand implements Command {

    private final Database database;

    public HGetAllCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "HGETALL";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 1) {
            return new RespError("ERR wrong number of arguments for 'hgetall' command");
        }
        RedisValue stored = database.get(args.get(0));
        if (stored == null) {
            return new RespArray(List.of());
        }
        if (!(stored instanceof RedisHash hash)) {
            return new RespError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        List<RespValue> flattened = new ArrayList<>();
        for (Map.Entry<String, String> entry : hash.values().entrySet()) {
            flattened.add(new RespBulkString(entry.getKey()));
            flattened.add(new RespBulkString(entry.getValue()));
        }
        return new RespArray(flattened);
    }
}