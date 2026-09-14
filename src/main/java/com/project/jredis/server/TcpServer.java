package com.project.jredis.server;

import com.project.jredis.command.CommandDispatcher;
import com.project.jredis.config.ServerConfig;
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
    private final RespParser parser = new RespParser();
    private final RespEncoder encoder = new RespEncoder();
    private final ExecutorService clientPool = Executors.newFixedThreadPool(MAX_CLIENTS);

    public TcpServer(ServerConfig config, CommandDispatcher dispatcher) {
        this.config = config;
        this.dispatcher = dispatcher;
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
        try (
                clientSocket;
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8)
        ) {
            LOGGER.info(() -> "Client connected: " + clientSocket.getRemoteSocketAddress());

            while (true) {
                RespValue request;
                try {
                    request = parser.parse(in);
                } catch (IOException e) {
                    break;
                } catch (IllegalArgumentException e) {
                    out.print(encoder.encode(new RespError("ERR Protocol error: " + e.getMessage())));
                    out.flush();
                    break;
                }

                if (request == null) {
                    break;
                }

                RespValue response = dispatcher.dispatch(request);
                out.print(encoder.encode(response));
                out.flush();
            }

            LOGGER.info(() -> "Client disconnected: " + clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            LOGGER.warning(() -> "Error handling client: " + e.getMessage());
        }
    }
}