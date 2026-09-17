package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespValue;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CommandListCommand implements Command {

    private final CommandRegistry registry;

    public CommandListCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String name() {
        return "COMMAND";
    }

    @Override
    public RespValue execute(List<String> args) {
        List<RespValue> names = registry.allCommandNames().stream()
                .map(n -> (RespValue) new RespBulkString(n))
                .toList();
        return new RespArray(names);
    }
}