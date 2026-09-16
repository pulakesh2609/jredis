package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TtlCommand implements Command {

    private final Database database;

    public TtlCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "TTL";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 1) {
            return new RespError("ERR wrong number of arguments for 'ttl' command");
        }
        String key = args.get(0);
        if (!database.exists(key)) {
            return new RespInteger(-2);
        }
        Long expiryEpochMillis = database.getExpiration(key);
        if (expiryEpochMillis == null) {
            return new RespInteger(-1);
        }
        long remainingMillis = expiryEpochMillis - System.currentTimeMillis();
        long remainingSeconds = Math.max(0, (remainingMillis + 999) / 1000);
        return new RespInteger(remainingSeconds);
    }
}