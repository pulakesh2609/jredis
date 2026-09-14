package com.project.jredis.command;

import com.project.jredis.protocol.RespSimpleString;
import com.project.jredis.protocol.RespValue;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PingCommand implements Command {

    @Override
    public String name() {
        return "PING";
    }

    @Override
    public RespValue execute(List<String> args) {
        return new RespSimpleString("PONG");
    }
}