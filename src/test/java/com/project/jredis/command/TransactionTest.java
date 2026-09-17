package com.project.jredis.command;

import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespSimpleString;
import com.project.jredis.protocol.RespValue;
import com.project.jredis.storage.Database;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private final Database database = new Database();
    private final CommandRegistry registry = new CommandRegistry(
            List.of(new SetCommand(database), new GetCommand(database), new PingCommand()));
    private final PubSubBroker pubSubBroker = new PubSubBroker();
    private final CommandDispatcher dispatcher = new CommandDispatcher(registry, pubSubBroker);

    private RespArray command(String... parts) {
        List<RespValue> values = new ArrayList<>();
        for (String part : parts) {
            values.add(new RespBulkString(part));
        }
        return new RespArray(values);
    }

    @Test
    void commandsQueueInsteadOfExecutingDuringMulti() {
        ClientSession session = new ClientSession();
        dispatcher.dispatch(command("MULTI"), session);
        Object result = dispatcher.dispatch(command("SET", "key", "value"), session);
        assertEquals(new RespSimpleString("QUEUED"), result);
        assertNull(database.get("key"));
    }

    @Test
    void execRunsAllQueuedCommandsInOrder() {
        ClientSession session = new ClientSession();
        dispatcher.dispatch(command("MULTI"), session);
        dispatcher.dispatch(command("SET", "key", "value"), session);
        dispatcher.dispatch(command("GET", "key"), session);

        RespArray result = (RespArray) dispatcher.dispatch(command("EXEC"), session);
        assertEquals(2, result.values().size());
        assertEquals(new RespSimpleString("OK"), result.values().get(0));
        assertEquals(new RespBulkString("value"), result.values().get(1));
    }

    @Test
    void discardCancelsQueuedCommands() {
        ClientSession session = new ClientSession();
        dispatcher.dispatch(command("MULTI"), session);
        dispatcher.dispatch(command("SET", "key", "value"), session);
        dispatcher.dispatch(command("DISCARD"), session);

        assertFalse(session.isInTransaction());
        assertNull(database.get("key"));
    }

    @Test
    void execWithoutMultiIsAnError() {
        ClientSession session = new ClientSession();
        assertInstanceOf(RespError.class, dispatcher.dispatch(command("EXEC"), session));
    }

    @Test
    void nestedMultiIsRejected() {
        ClientSession session = new ClientSession();
        dispatcher.dispatch(command("MULTI"), session);
        assertInstanceOf(RespError.class, dispatcher.dispatch(command("MULTI"), session));
    }

    @Test
    void unknownCommandDuringMultiAbortsTheWholeTransaction() {
        ClientSession session = new ClientSession();
        dispatcher.dispatch(command("MULTI"), session);
        dispatcher.dispatch(command("SET", "key", "value"), session);
        dispatcher.dispatch(command("NOTAREALCOMMAND"), session);

        Object result = dispatcher.dispatch(command("EXEC"), session);
        assertInstanceOf(RespError.class, result);
        assertTrue(((RespError) result).message().startsWith("EXECABORT"));
        assertNull(database.get("key"));
    }

    @Test
    void separateSessionsDoNotShareTransactionState() {
        ClientSession sessionA = new ClientSession();
        ClientSession sessionB = new ClientSession();

        dispatcher.dispatch(command("MULTI"), sessionA);
        Object result = dispatcher.dispatch(command("SET", "key", "value"), sessionB);
        assertEquals(new RespSimpleString("OK"), result);
    }
}