package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PersistCommand implements Command {

    private final Database database;

    public PersistCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "PERSIST";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 1) {
            return new RespError("ERR wrong number of arguments for 'persist' command");
        }
        boolean removed = database.persist(args.get(0));
        return new RespInteger(removed ? 1 : 0);
    }
}