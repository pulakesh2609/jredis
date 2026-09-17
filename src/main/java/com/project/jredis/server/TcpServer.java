package com.project.jredis.server;

import com.project.jredis.command.ClientSession;
import com.project.jredis.command.CommandDispatcher;
import com.project.jredis.command.PubSubBroker;
import com.project.jredis.config.ServerConfig;
import com.project.jredis.protocol.RespArray;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespEncoder;
import com.project.jredis.protocol.RespError;
import com.project.jredis.protocol.RespParser;
import com.project.jredis.protocol.RespValue;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Component
@Profile("!test")
public class TcpServer implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(TcpServer.class.getName());
    private static final int MAX_CLIENTS = 50;

    private final ServerConfig config;
    private final CommandDispatcher dispatcher;
    private final PubSubBroker pubSubBroker;
    private final ServerStats stats;
    private final RespParser parser = new RespParser();
    private final RespEncoder encoder = new RespEncoder();
    private final ExecutorService clientPool = Executors.newFixedThreadPool(MAX_CLIENTS);

    public TcpServer(ServerConfig config, CommandDispatcher dispatcher, PubSubBroker pubSubBroker, ServerStats stats) {
        this.config = config;
        this.dispatcher = dispatcher;
        this.pubSubBroker = pubSubBroker;
        this.stats = stats;
    }

    @Override
    public void run(String... args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            LOGGER.info(() -> "JRedis listening on port " + config.getPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientPool.submit(() -> handleClient(clientSocket));
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        ClientSession session = new ClientSession();
        try (
                clientSocket;
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8)
        ) {
            LOGGER.info(() -> "Client connected: " + clientSocket.getRemoteSocketAddress());
            stats.clientConnected();
            session.setMessagePusher((channel, message) -> {
                RespArray pushArray = new RespArray(List.of(
                        new RespBulkString("message"),
                        new RespBulkString(channel),
                        new RespBulkString(message)
                ));
                sendResponse(out, pushArray);
            });

            while (true) {
                RespValue request;
                try {
                    request = parser.parse(in);
                } catch (IOException e) {
                    break;
                } catch (IllegalArgumentException e) {
                    sendResponse(out, new RespError("ERR Protocol error: " + e.getMessage()));
                    break;
                }

                if (request == null) {
                    break;
                }

                RespValue response = dispatcher.dispatch(request, session);
                sendResponse(out, response);
            }
        } catch (IOException e) {
            LOGGER.warning(() -> "Error handling client: " + e.getMessage());
        } finally {
            pubSubBroker.unsubscribeAll(session);
            stats.clientDisconnected();
            LOGGER.info(() -> "Client disconnected: " + clientSocket.getRemoteSocketAddress());
        }
    }

    private void sendResponse(PrintWriter out, RespValue value) {
        synchronized (out) {
            out.print(encoder.encode(value));
            out.flush();
        }
    }
}