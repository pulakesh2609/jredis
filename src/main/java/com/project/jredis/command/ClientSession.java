package com.project.jredis.command;

import com.project.jredis.protocol.RespValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSession {

    private boolean inTransaction = false;
    private boolean dirty = false;
    private final List<RespValue> queuedCommands = new ArrayList<>();

    private final Set<String> subscribedChannels = ConcurrentHashMap.newKeySet();
    private MessagePusher messagePusher;

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

    public void setMessagePusher(MessagePusher pusher) {
        this.messagePusher = pusher;
    }

    public void pushMessage(String channel, String message) {
        if (messagePusher != null) {
            messagePusher.push(channel, message);
        }
    }

    public void addSubscription(String channel) {
        subscribedChannels.add(channel);
    }

    public void removeSubscription(String channel) {
        subscribedChannels.remove(channel);
    }

    public Set<String> getSubscriptions() {
        return subscribedChannels;
    }

    public void clearSubscriptions() {
        subscribedChannels.clear();
    }
}