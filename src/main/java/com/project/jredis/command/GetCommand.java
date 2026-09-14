package com.project.jredis.command;

import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisString;
import com.project.jredis.storage.RedisValue;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class GetCommand implements Command {

    private final Database database;

    public GetCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "GET";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 1) {
            return new RespError("ERR wrong number of arguments for 'get' command");
        }
        RedisValue stored = database.get(args.get(0));
        if (stored == null) {
            return new RespBulkString(null);
        }
        if (!(stored instanceof RedisString redisString)) {
            return new RespError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
        return new RespBulkString(redisString.value());
    }
}