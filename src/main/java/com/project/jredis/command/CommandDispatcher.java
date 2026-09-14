package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespValue;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class CommandDispatcher {

    private final CommandRegistry registry;

    public CommandDispatcher(CommandRegistry registry) {
        this.registry = registry;
    }

    public RespValue dispatch(RespValue request) {
        if (!(request instanceof RespArray array) || array.values().isEmpty()) {
            return new RespError("ERR invalid request");
        }

        List<String> parts = new ArrayList<>();
        for (RespValue element : array.values()) {
            if (!(element instanceof RespBulkString bulkString) || bulkString.value() == null) {
                return new RespError("ERR invalid argument type");
            }
            parts.add(bulkString.value());
        }

        String commandName = parts.get(0).toUpperCase();
        List<String> args = parts.subList(1, parts.size());

        Command command = registry.find(commandName);
        if (command == null) {
            return new RespError("ERR unknown command '" + commandName + "'");
        }

        return command.execute(args);
    }
}