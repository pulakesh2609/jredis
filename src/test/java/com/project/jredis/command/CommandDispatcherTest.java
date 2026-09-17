package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespSimpleString;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CommandDispatcherTest {

    private final CommandRegistry registry =
            new CommandRegistry(List.of(new PingCommand(), new EchoCommand()));
    private final PubSubBroker pubSubBroker = new PubSubBroker();
    private final CommandDispatcher dispatcher = new CommandDispatcher(registry, pubSubBroker);
    private final ClientSession session = new ClientSession();

    @Test
    void dispatchesPing() {
        RespArray request = new RespArray(List.of(new RespBulkString("PING")));
        assertEquals(new RespSimpleString("PONG"), dispatcher.dispatch(request, session));
    }

    @Test
    void dispatchIsCaseInsensitive() {
        RespArray request = new RespArray(List.of(new RespBulkString("ping")));
        assertEquals(new RespSimpleString("PONG"), dispatcher.dispatch(request, session));
    }

    @Test
    void returnsErrorForUnknownCommand() {
        RespArray request = new RespArray(List.of(new RespBulkString("FOOBAR")));
        assertInstanceOf(RespError.class, dispatcher.dispatch(request, session));
    }

    @Test
    void returnsErrorForNonArrayRequest() {
        assertInstanceOf(RespError.class, dispatcher.dispatch(new RespSimpleString("nope"), session));
    }
}