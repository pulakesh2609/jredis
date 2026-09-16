package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespSimpleString;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisString;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class SetCommand implements Command {

    private final Database database;

    public SetCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "SET";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 2 && args.size() != 4) {
            return new RespError("ERR wrong number of arguments for 'set' command");
        }

        String key = args.get(0);
        String value = args.get(1);
        database.put(key, new RedisString(value));

        if (args.size() == 4) {
            if (!args.get(2).equalsIgnoreCase("EX")) {
                return new RespError("ERR syntax error");
            }
            long seconds;
            try {
                seconds = Long.parseLong(args.get(3));
            } catch (NumberFormatException e) {
                return new RespError("ERR value is not an integer or out of range");
            }
            database.setExpiration(key, System.currentTimeMillis() + seconds * 1000);
        }

        return new RespSimpleString("OK");
    }
}