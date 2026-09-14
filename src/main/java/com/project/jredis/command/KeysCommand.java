package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class KeysCommand implements Command {

    private final Database database;

    public KeysCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "KEYS";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (!args.isEmpty()) {
            return new RespError("ERR wrong number of arguments for 'keys' command");
        }
        List<RespValue> keyList = database.keys().stream()
                .map(k -> (RespValue) new RespBulkString(k))
                .toList();
        return new RespArray(keyList);
    }
}