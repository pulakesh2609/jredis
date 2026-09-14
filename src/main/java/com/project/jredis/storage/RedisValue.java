package com.project.jredis.storage;

public sealed interface RedisValue
        permits RedisString, RedisList, RedisSet, RedisHash {

    String typeName();
}