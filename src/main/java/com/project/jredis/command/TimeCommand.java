package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespValue;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

@Component
public class TimeCommand implements Command {

    @Override
    public String name() {
        return "TIME";
    }

    @Override
    public RespValue execute(List<String> args) {
        Instant now = Instant.now();
        long seconds = now.getEpochSecond();
        long microseconds = now.getNano() / 1000;

        return new RespArray(List.of(
                new RespBulkString(String.valueOf(seconds)),
                new RespBulkString(String.valueOf(microseconds))
        ));
    }
}