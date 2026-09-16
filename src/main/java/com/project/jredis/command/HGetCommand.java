package com.project.jredis.command;

import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisHash;
import com.project.jredis.storage.RedisValue;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class HGetCommand implements Command {

    private final Database database;

    public HGetCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "HGET";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 2) {
            return new RespError("ERR wrong number of arguments for 'hget' command");
        }
        RedisValue stored = database.get(args.get(0));
        if (stored == null) {
            return new RespBulkString(null);
        }
        if (!(stored instanceof RedisHash hash)) {
            return new RespError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
        return new RespBulkString(hash.values().get(args.get(1)));
    }
}