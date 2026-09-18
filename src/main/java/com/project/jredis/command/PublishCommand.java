package com.project.jredis.command;

import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PublishCommand implements Command {

    private final PubSubBroker broker;

    public PublishCommand(PubSubBroker broker) {
        this.broker = broker;
    }

    @Override
    public String name() {
        return "PUBLISH";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (args.size() != 2) {
            return new RespError("ERR wrong number of arguments for 'publish' command");
        }
        int delivered = broker.publish(args.get(0), args.get(1));
        return new RespInteger(delivered);
    }
}