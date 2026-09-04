package com.project.jredis.server;

import com.project.jredis.config.ServerConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@Component
public class TcpServer implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(TcpServer.class.getName());

    private final ServerConfig config;

    public TcpServer(ServerConfig config) {
        this.config = config;
    }

    @Override
    public void run(String... args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            LOGGER.info(() -> "JRedis listening on port " + config.getPort());

            while (true) {
                Socket clientSocket = serverSocket.accept(); // blocks until a client connects
                handleClient(clientSocket);
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
            String line;
            while ((line = in.readLine()) != null) {
                out.println("ECHO: " + line);
            }
            LOGGER.info(() -> "Client disconnected: " + clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            LOGGER.warning(() -> "Error handling client: " + e.getMessage());
        }
    }
}