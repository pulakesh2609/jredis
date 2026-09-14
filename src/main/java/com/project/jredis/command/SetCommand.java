package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespSimpleString;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
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
        if (args.size() != 2) {
            return new RespError("ERR wrong number of arguments for 'set' command");
        }
        database.set(args.get(0), args.get(1));
        return new RespSimpleString("OK");
    }
}