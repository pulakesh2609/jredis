package com.project.jredis.protocol;

import java.util.List;

public record RespArray(List<RespValue> values) implements RespValue {
}