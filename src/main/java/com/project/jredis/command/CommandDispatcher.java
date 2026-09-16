package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespSimpleString;
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

    public RespValue dispatch(RespValue request, ClientSession session) {
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

        switch (commandName) {
            case "MULTI" -> {
                if (session.isInTransaction()) {
                    return new RespError("ERR MULTI calls can not be nested");
                }
                session.startTransaction();
                return new RespSimpleString("OK");
            }
            case "DISCARD" -> {
                if (!session.isInTransaction()) {
                    return new RespError("ERR DISCARD without MULTI");
                }
                session.endTransaction();
                return new RespSimpleString("OK");
            }
            case "EXEC" -> {
                if (!session.isInTransaction()) {
                    return new RespError("ERR EXEC without MULTI");
                }
                if (session.isDirty()) {
                    session.endTransaction();
                    return new RespError("EXECABORT Transaction discarded because of previous errors");
                }
                return executeQueuedCommands(session.drainQueuedCommands());
            }
            default -> {
                if (session.isInTransaction()) {
                    Command command = registry.find(commandName);
                    if (command == null) {
                        session.markDirty(); // real Redis: an unknown command dooms the whole EXEC
                        return new RespError("ERR unknown command '" + commandName + "'");
                    }
                    session.queueCommand(request);
                    return new RespSimpleString("QUEUED");
                }
                return executeSingleCommand(commandName, args);
            }
        }
    }

    private RespValue executeQueuedCommands(List<RespValue> queuedRequests) {
        List<RespValue> results = new ArrayList<>();
        for (RespValue queuedRequest : queuedRequests) {
            RespArray array = (RespArray) queuedRequest; // safe — only ever queued via this same validation path
            List<String> parts = new ArrayList<>();
            for (RespValue element : array.values()) {
                parts.add(((RespBulkString) element).value());
            }
            String commandName = parts.get(0).toUpperCase();
            List<String> args = parts.subList(1, parts.size());
            results.add(executeSingleCommand(commandName, args));
        }
        return new RespArray(results);
    }

    private RespValue executeSingleCommand(String commandName, List<String> args) {
        Command command = registry.find(commandName);
        if (command == null) {
            return new RespError("ERR unknown command '" + commandName + "'");
        }
        return command.execute(args);
    }
}