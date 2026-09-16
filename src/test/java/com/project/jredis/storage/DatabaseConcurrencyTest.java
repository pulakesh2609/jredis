package com.project.jredis.storage;

import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConcurrencyTest {

    @Test
    void concurrentSetsShouldNotLoseData() throws InterruptedException {
        Database database = new Database();
        int threadCount = 50;
        int keysPerThread = 1000;
        int expectedTotal = threadCount * keysPerThread;

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int t = 0; t < threadCount; t++) {
            int threadId = t;
            pool.submit(() -> {
                for (int k = 0; k < keysPerThread; k++) {
                    database.put("thread" + threadId + "-key" + k, new RedisString("value"));
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        assertEquals(expectedTotal, database.size(),
                "Expected every key to be stored — a lower count means concurrent map writes silently lost data");
    }
}