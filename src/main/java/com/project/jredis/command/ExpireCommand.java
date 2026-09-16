package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ExpireCommand implements Command {

    private final Database database;

    public ExpireCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "EXPIRE";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 2) {
            return new RespError("ERR wrong number of arguments for 'expire' command");
        }
        long seconds;
        try {
            seconds = Long.parseLong(args.get(1));
        } catch (NumberFormatException e) {
            return new RespError("ERR value is not an integer or out of range");
        }
        long expiryEpochMillis = System.currentTimeMillis() + (seconds * 1000);
        boolean wasSet = database.setExpiration(args.get(0), expiryEpochMillis);
        return new RespInteger(wasSet ? 1 : 0);
    }
}