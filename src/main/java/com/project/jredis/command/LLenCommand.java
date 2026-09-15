package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisList;
import com.project.jredis.storage.RedisValue;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LLenCommand implements Command {

    private final Database database;

    public LLenCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "LLEN";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 1) {
            return new RespError("ERR wrong number of arguments for 'llen' command");
        }
        RedisValue stored = database.get(args.get(0));
        if (stored == null) {
            return new RespInteger(0);
        }
        if (!(stored instanceof RedisList list)) {
            return new RespError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
        return new RespInteger(list.values().size());
    }
}