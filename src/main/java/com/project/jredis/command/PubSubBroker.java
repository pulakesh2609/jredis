package com.project.jredis.command;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


@Component
public class PubSubBroker {

    private final Map<String, Set<ClientSession>> subscribers = new ConcurrentHashMap<>();

    public void subscribe(String channel, ClientSession session) {
        subscribers.computeIfAbsent(channel, c -> new CopyOnWriteArraySet<>()).add(session);
        session.addSubscription(channel);
    }

    public void unsubscribe(String channel, ClientSession session) {
        Set<ClientSession> channelSubscribers = subscribers.get(channel);
        if (channelSubscribers != null) {
            channelSubscribers.remove(session);
        }
        session.removeSubscription(channel);
    }

    public void unsubscribeAll(ClientSession session) {
        for (String channel : session.getSubscriptions()) {
            Set<ClientSession> channelSubscribers = subscribers.get(channel);
            if (channelSubscribers != null) {
                channelSubscribers.remove(session);
            }
        }
        session.clearSubscriptions();
    }

    public int publish(String channel, String message) {
        Set<ClientSession> channelSubscribers = subscribers.get(channel);
        if (channelSubscribers == null) {
            return 0;
        }
        int delivered = 0;
        for (ClientSession subscriber : channelSubscribers) {
            subscriber.pushMessage(channel, message);
            delivered++;
        }
        return delivered;
    }
}