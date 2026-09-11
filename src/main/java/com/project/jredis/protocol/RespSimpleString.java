package com.project.jredis.protocol;

public record RespSimpleString(String value) implements RespValue {
}