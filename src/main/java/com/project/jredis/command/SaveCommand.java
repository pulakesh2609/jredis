package com.project.jredis.command;

import com.project.jredis.persistence.SnapshotManager;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespSimpleString;
import com.project.jredis.protocol.RespValue;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;

@Component
public class SaveCommand implements Command {

    private final SnapshotManager snapshotManager;

    public SaveCommand(SnapshotManager snapshotManager) {
        this.snapshotManager = snapshotManager;
    }

    @Override
    public String name() {
        return "SAVE";
    }

    @Override
    public RespValue execute(List<String> args) {
        if (!args.isEmpty()) {
            return new RespError("ERR wrong number of arguments for 'save' command");
        }
        try {
            snapshotManager.save();
        } catch (IOException e) {
            return new RespError("ERR snapshot failed: " + e.getMessage());
        }
        return new RespSimpleString("OK");
    }
}