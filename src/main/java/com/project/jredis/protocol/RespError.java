package com.project.jredis.protocol;

public record RespError(String message) implements RespValue {
}