package com.project.jredis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jredis")
public class ServerConfig {
    private int port = 6380;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}