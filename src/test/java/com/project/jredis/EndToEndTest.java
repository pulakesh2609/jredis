package com.project.jredis;

import com.project.jredis.command.*;
import com.project.jredis.config.ServerConfig;
import com.project.jredis.server.ServerStats;
import com.project.jredis.server.TcpServer;
import com.project.jredis.storage.Database;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EndToEndTest {

    // A different port than the real app's 6380, so this test never collides with a
    // dev instance you might have running in another terminal at the same time.
    private static final int TEST_PORT = 16380;

    @BeforeAll
    static void startRealServer() {
        ServerConfig config = new ServerConfig();
        config.setPort(TEST_PORT);

        Database database = new Database();
        PubSubBroker pubSubBroker = new PubSubBroker();
        ServerStats stats = new ServerStats();

        List<Command> commands = List.of(
                new PingCommand(),
                new EchoCommand(),
                new SetCommand(database),
                new GetCommand(database),
                new DelCommand(database),
                new ExistsCommand(database)
        );
        CommandRegistry registry = new CommandRegistry(commands);
        CommandDispatcher dispatcher = new CommandDispatcher(registry, pubSubBroker, stats);
        TcpServer server = new TcpServer(config, dispatcher, pubSubBroker, stats);

        // run() blocks forever — the real accept loop, exactly like production — so it
        // needs its own thread here. Daemon = true so a stuck server thread can never
        // keep the whole test JVM alive after the test class finishes.
        Thread serverThread = new Thread(() -> {
            try {
                server.run();
            } catch (IOException e) {
                // expected once the JVM shuts down after tests finish
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        waitForPortToOpen();
    }

    private static void waitForPortToOpen() {
        for (int attempt = 0; attempt < 50; attempt++) {
            try (Socket probe = new Socket("localhost", TEST_PORT)) {
                return;
            } catch (IOException e) {
                sleep(50);
            }
        }
        fail("Server never started listening on port " + TEST_PORT);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void pingOverRealSocketReturnsPong() throws IOException {
        try (Session session = new Session()) {
            assertEquals("+PONG", session.send("*1\r\n$4\r\nPING\r\n"));
        }
    }

    @Test
    void setThenGetRoundTripsOverRealSocket() throws IOException {
        try (Session session = new Session()) {
            assertEquals("+OK", session.send("*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$4\r\nJohn\r\n"));
            assertEquals("$4", session.send("*2\r\n$3\r\nGET\r\n$4\r\nname\r\n"));
            // GET's Bulk String reply is two lines — the length header just asserted, then the value:
            assertEquals("John", session.readLine());
        }
    }

    @Test
    void unknownCommandReturnsRealError() throws IOException {
        try (Session session = new Session()) {
            String reply = session.send("*1\r\n$7\r\nNOTREAL\r\n");
            assertTrue(reply.startsWith("-ERR unknown command"));
        }
    }

    @Test
    void malformedRespClosesTheConnection() throws IOException {
        try (
                Socket socket = new Socket("localhost", TEST_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            out.print("@garbage\r\n"); // '@' is not a valid RESP type prefix
            out.flush();

            String errorReply = in.readLine();
            assertNotNull(errorReply);
            assertTrue(errorReply.startsWith("-ERR Protocol error"));

            // Phase 4's design decision: a protocol error closes the connection —
            // the next read should hit end-of-stream, not hang forever.
            assertNull(in.readLine());
        }
    }

    @Test
    void secondClientConnectsWhileFirstIsStillOpen() throws IOException {
        try (Session sessionA = new Session(); Session sessionB = new Session()) {
            assertEquals("+PONG", sessionA.send("*1\r\n$4\r\nPING\r\n"));
            assertEquals("+PONG", sessionB.send("*1\r\n$4\r\nPING\r\n"));
        }
    }

    private static class Session implements AutoCloseable {
        private final Socket socket;
        private final PrintWriter out;
        private final BufferedReader in;

        Session() throws IOException {
            socket = new Socket("localhost", TEST_PORT);
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        }

        String send(String rawRespRequest) throws IOException {
            out.print(rawRespRequest);
            out.flush();
            return in.readLine();
        }

        String readLine() throws IOException {
            return in.readLine();
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}