package com.project.jredis.command;

import com.project.jredis.protocol.RespValue;
import java.util.ArrayList;
import java.util.List;

public class ClientSession {

    private boolean inTransaction = false;
    private boolean dirty = false;
    private final List<RespValue> queuedCommands = new ArrayList<>();

    public boolean isInTransaction() {
        return inTransaction;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void startTransaction() {
        inTransaction = true;
        dirty = false;
        queuedCommands.clear();
    }

    public void markDirty() {
        dirty = true;
    }

    public void queueCommand(RespValue command) {
        queuedCommands.add(command);
    }

    public List<RespValue> drainQueuedCommands() {
        List<RespValue> copy = new ArrayList<>(queuedCommands);
        endTransaction();
        return copy;
    }

    public void endTransaction() {
        inTransaction = false;
        dirty = false;
        queuedCommands.clear();
    }
}