package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ExistsCommand implements Command {

    private final Database database;

    public ExistsCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "EXISTS";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 1) {
            return new RespError("ERR wrong number of arguments for 'exists' command");
        }
        return new RespInteger(database.exists(args.get(0)) ? 1 : 0);
    }
}