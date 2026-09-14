package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DbSizeCommand implements Command {

    private final Database database;

    public DbSizeCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "DBSIZE";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (!args.isEmpty()) {
            return new RespError("ERR wrong number of arguments for 'dbsize' command");
        }
        return new RespInteger(database.size());
    }
}