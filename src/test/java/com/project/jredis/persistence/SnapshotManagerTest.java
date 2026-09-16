package com.project.jredis.persistence;

import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisHash;
import com.project.jredis.storage.RedisList;
import com.project.jredis.storage.RedisSet;
import com.project.jredis.storage.RedisString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class SnapshotManagerTest {

    @Test
    void savedSnapshotRestoresAllDataTypes(@TempDir Path tempDir) throws IOException {
        Database original = new Database();
        SnapshotManager writer = new SnapshotManager(original);

        original.put("greeting", new RedisString("hello"));
        original.put("mylist", new RedisList(new ArrayList<>(List.of("a", "b", "c"))));
        original.put("myset", new RedisSet(new HashSet<>(Set.of("x", "y"))));
        var hash = new LinkedHashMap<String, String>();
        hash.put("name", "John");
        hash.put("age", "30");
        original.put("myhash", new RedisHash(hash));

        Path snapshotFile = tempDir.resolve("test.rdb");
        writer.save(snapshotFile);

        Database restored = new Database();
        SnapshotManager reader = new SnapshotManager(restored);
        reader.load(snapshotFile);

        assertEquals(new RedisString("hello"), restored.get("greeting"));
        assertEquals(List.of("a", "b", "c"), ((RedisList) restored.get("mylist")).values());
        assertEquals(Set.of("x", "y"), ((RedisSet) restored.get("myset")).values());
        assertEquals("John", ((RedisHash) restored.get("myhash")).values().get("name"));
        assertEquals(4, restored.size());
    }

    @Test
    void expiryMetadataSurvivesRoundTrip(@TempDir Path tempDir) throws IOException {
        Database original = new Database();
        SnapshotManager writer = new SnapshotManager(original);

        original.put("temp", new RedisString("value"));
        long expiry = System.currentTimeMillis() + 100_000;
        original.setExpiration("temp", expiry);

        Path snapshotFile = tempDir.resolve("test.rdb");
        writer.save(snapshotFile);

        Database restored = new Database();
        SnapshotManager reader = new SnapshotManager(restored);
        reader.load(snapshotFile);

        Long restoredExpiry = restored.getExpiration("temp");
        assertNotNull(restoredExpiry);
        assertEquals(expiry, restoredExpiry);
    }

    @Test
    void loadOnMissingFileDoesNothing(@TempDir Path tempDir) throws IOException {
        Database database = new Database();
        SnapshotManager manager = new SnapshotManager(database);

        manager.load(tempDir.resolve("does-not-exist.rdb"));

        assertEquals(0, database.size());
    }
}