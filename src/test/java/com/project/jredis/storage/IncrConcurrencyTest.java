package com.project.jredis.storage;

import com.project.jredis.command.IncrCommand;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IncrConcurrencyTest {

    @Test
    void concurrentIncrementsAreNotLost() throws InterruptedException {
        Database database = new Database();
        IncrCommand incr = new IncrCommand(database);
        int threadCount = 50;
        int incrementsPerThread = 1000;

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int t = 0; t < threadCount; t++) {
            pool.submit(() -> {
                for (int i = 0; i < incrementsPerThread; i++) {
                    incr.execute(List.of("counter"));
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        RedisString result = (RedisString) database.get("counter");
        assertEquals(threadCount * incrementsPerThread, Long.parseLong(result.value()));
    }
}