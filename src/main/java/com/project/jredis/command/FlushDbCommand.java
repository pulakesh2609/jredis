package com.project.jredis.command;

import com.project.jredis.protocol.RespSimpleString;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class FlushDbCommand implements Command {

    private final Database database;

    public FlushDbCommand(Database database) {
        this.database = database;
    }

    @Override
    public String name() {
        return "FLUSHDB";
    }

    @Override
    public RespValue execute(List<String> args) {
        database.clear();
        return new RespSimpleString("OK");
    }
}