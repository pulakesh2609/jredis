package com.project.jredis.command;

@FunctionalInterface
public interface MessagePusher {
    void push(String channel, String message);
}