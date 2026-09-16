package com.project.jredis.storage;

import com.project.jredis.command.ExpireCommand;
import com.project.jredis.command.GetCommand;
import com.project.jredis.command.LLenCommand;
import com.project.jredis.command.LPushCommand;
import com.project.jredis.command.RPopCommand;
import com.project.jredis.command.SetCommand;
import com.project.jredis.protocol.RespBulkString;
import com.project.jredis.protocol.RespInteger;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyStressTest {

    @Test
    void concurrentSetAcrossManyKeysAndClients() throws InterruptedException {
        Database database = new Database();
        SetCommand set = new SetCommand(database);

        int threadCount = 50;
        int keysPerThread = 500;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int t = 0; t < threadCount; t++) {
            int threadId = t;
            pool.submit(() -> {
                for (int k = 0; k < keysPerThread; k++) {
                    set.execute(List.of("key" + threadId + "-" + k, "value"));
                }
            });
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        assertEquals(threadCount * keysPerThread, database.size());
    }

    @Test
    void concurrentPushAndPopOnSameListKey() throws InterruptedException {
        Database database = new Database();
        LPushCommand lpush = new LPushCommand(database);
        RPopCommand rpop = new RPopCommand(database);
        LLenCommand llen = new LLenCommand(database);

        int pushThreads = 20;
        int pushesPerThread = 500;
        int totalPushes = pushThreads * pushesPerThread;

        ExecutorService pushPool = Executors.newFixedThreadPool(pushThreads);
        for (int t = 0; t < pushThreads; t++) {
            pushPool.submit(() -> {
                for (int i = 0; i < pushesPerThread; i++) {
                    lpush.execute(List.of("mylist", "item"));
                }
            });
        }
        pushPool.shutdown();
        assertTrue(pushPool.awaitTermination(30, TimeUnit.SECONDS));

        // All pushes finish before popping starts — isolates push-safety from pop-safety.
        assertEquals(new RespInteger(totalPushes), llen.execute(List.of("mylist")));

        AtomicInteger successfulPops = new AtomicInteger();
        ExecutorService popPool = Executors.newFixedThreadPool(pushThreads);
        for (int t = 0; t < pushThreads; t++) {
            popPool.submit(() -> {
                for (int i = 0; i < pushesPerThread; i++) {
                    if (rpop.execute(List.of("mylist")) instanceof RespBulkString bulk && bulk.value() != null) {
                        successfulPops.incrementAndGet();
                    }
                }
            });
        }
        popPool.shutdown();
        assertTrue(popPool.awaitTermination(30, TimeUnit.SECONDS));

        // Exactly as many successful pops as pushes — no lost items, no phantom duplicates.
        assertEquals(totalPushes, successfulPops.get());
        assertFalse(database.exists("mylist")); // fully drained, key should be gone
    }

    // A deliberately naive store, for comparison only — never used anywhere in the real project.
    // Notice it still uses a ConcurrentHashMap, and still has a real bug — the map alone isn't enough.
    static class NaiveListStore {
        private final Map<String, List<String>> data = new ConcurrentHashMap<>();

        void push(String key, String value) {
            List<String> list = data.get(key);           // 1. READ
            if (list == null) {
                list = new ArrayList<>();
                data.put(key, list);                       // 2. WRITE (only on first push)
            }
            list.add(value);                               // 3. MUTATE — outside any atomic map operation
        }

        int size(String key) {
            List<String> list = data.get(key);
            return list == null ? 0 : list.size();
        }
    }

    @Test
    void naiveStoreLosesPushesUnderConcurrency() throws InterruptedException {
        NaiveListStore naive = new NaiveListStore();
        int threads = 20;
        int pushesPerThread = 500;
        int expected = threads * pushesPerThread;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                for (int i = 0; i < pushesPerThread; i++) {
                    naive.push("key", "item");
                }
            });
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        // The actual point: we EXPECT this to fail. If it ever passes, it got lucky that run —
        // rerun it a few times; the bug is real even when one run doesn't happen to trigger it.
        assertNotEquals(expected, naive.size("key"),
                "If this fails, the naive store got lucky this run — the bug is still real");
    }

    @Test
    void expirationDuringConcurrentReadsNeverCorrupts() throws InterruptedException {
        Database database = new Database();
        SetCommand set = new SetCommand(database);
        GetCommand get = new GetCommand(database);
        ExpireCommand expire = new ExpireCommand(database);

        set.execute(List.of("key", "value"));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(() -> {
            for (int i = 0; i < 2000; i++) {
                expire.execute(List.of("key", "0"));
                set.execute(List.of("key", "value")); // re-create; also clears TTL, matching real Redis
            }
        });
        pool.submit(() -> {
            for (int i = 0; i < 2000; i++) {
                get.execute(List.of("key")); // the only thing checked: this never throws
            }
        });
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
    }
}