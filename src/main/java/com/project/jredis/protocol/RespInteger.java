package com.project.jredis.protocol;

public record RespInteger(long value) implements RespValue {
}