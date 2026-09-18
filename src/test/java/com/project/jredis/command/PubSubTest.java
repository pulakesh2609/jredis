package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespInteger;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.server.ServerStats;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PubSubTest {

    @Test
    void publishDeliversToAllSubscribersOfThatChannel() {
        PubSubBroker broker = new PubSubBroker();
        PublishCommand publish = new PublishCommand(broker);

        ClientSession subscriber = new ClientSession();
        List<String> received = new ArrayList<>();
        subscriber.setMessagePusher((channel, message) -> received.add(channel + ":" + message));

        broker.subscribe("news", subscriber);
        publish.execute(List.of("news", "hello world"));

        assertEquals(List.of("news:hello world"), received);
    }

    @Test
    void publishToChannelWithNoSubscribersReturnsZero() {
        PubSubBroker broker = new PubSubBroker();
        PublishCommand publish = new PublishCommand(broker);

        assertEquals(new RespInteger(0), publish.execute(List.of("empty-channel", "hello")));
    }

    @Test
    void unsubscribeStopsFurtherDelivery() {
        PubSubBroker broker = new PubSubBroker();
        PublishCommand publish = new PublishCommand(broker);

        ClientSession subscriber = new ClientSession();
        List<String> received = new ArrayList<>();
        subscriber.setMessagePusher((channel, message) -> received.add(message));

        broker.subscribe("news", subscriber);
        broker.unsubscribe("news", subscriber);
        publish.execute(List.of("news", "should not arrive"));

        assertTrue(received.isEmpty());
    }

    @Test
    void multipleSubscribersAllReceiveTheMessage() {
        PubSubBroker broker = new PubSubBroker();
        PublishCommand publish = new PublishCommand(broker);

        List<String> receivedA = new ArrayList<>();
        List<String> receivedB = new ArrayList<>();
        ClientSession subscriberA = new ClientSession();
        ClientSession subscriberB = new ClientSession();
        subscriberA.setMessagePusher((channel, message) -> receivedA.add(message));
        subscriberB.setMessagePusher((channel, message) -> receivedB.add(message));

        broker.subscribe("news", subscriberA);
        broker.subscribe("news", subscriberB);
        publish.execute(List.of("news", "breaking news"));

        assertEquals(List.of("breaking news"), receivedA);
        assertEquals(List.of("breaking news"), receivedB);
    }

    @Test
    void unsubscribeAllCleansUpOnDisconnect() {
        PubSubBroker broker = new PubSubBroker();
        PublishCommand publish = new PublishCommand(broker);

        ClientSession subscriber = new ClientSession();
        List<String> received = new ArrayList<>();
        subscriber.setMessagePusher((channel, message) -> received.add(message));

        broker.subscribe("news", subscriber);
        broker.unsubscribeAll(subscriber);
        publish.execute(List.of("news", "should not arrive"));

        assertTrue(received.isEmpty());
    }

    @Test
    void subscribeCommandUpdatesSessionSubscriptions() {
        PubSubBroker broker = new PubSubBroker();
        CommandRegistry registry = new CommandRegistry(List.of(new PublishCommand(broker)));
        CommandDispatcher dispatcher = new CommandDispatcher(registry, broker, new ServerStats());

        ClientSession session = new ClientSession();
        session.setMessagePusher((c, m) -> {});

        List<RespValue> parts = List.of(new RespBulkString("SUBSCRIBE"), new RespBulkString("news"));
        dispatcher.dispatch(new RespArray(parts), session);

        assertTrue(session.getSubscriptions().contains("news"));
    }
}