package com.project.jredis.command;

import com.project.jredis.protocol.RespValue;
import java.util.List;

public interface Command {
    String name();
    RespValue execute(List<String> args);
}