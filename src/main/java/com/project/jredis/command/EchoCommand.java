package com.project.jredis.command;

import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespValue;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class EchoCommand implements Command {

    @Override
    public String name() {
        return "ECHO";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 1) {
            return new RespError("ERR wrong number of arguments for 'echo' command");
        }
        return new RespBulkString(args.get(0));
    }
}