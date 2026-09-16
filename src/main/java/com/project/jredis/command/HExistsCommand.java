package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisHash;
import com.project.jredis.storage.RedisValue;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class HExistsCommand implements Command {

    private final Database database;

    public HExistsCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "HEXISTS";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 2) {
            return new RespError("ERR wrong number of arguments for 'hexists' command");
        }
        RedisValue stored = database.get(args.get(0));
        if (stored == null) {
            return new RespInteger(0);
        }
        if (!(stored instanceof RedisHash hash)) {
            return new RespError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
        return new RespInteger(hash.values().containsKey(args.get(1)) ? 1 : 0);
    }
}