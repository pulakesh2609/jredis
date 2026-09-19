package com.project.jredis.persistence;

import com.project.jredis.config.ServerConfig;
import com.project.jredis.storage.Database;
import com.project.jredis.storage.RedisHash;
import com.project.jredis.storage.RedisList;
import com.project.jredis.storage.RedisSet;
import com.project.jredis.storage.RedisString;
import com.project.jredis.storage.RedisValue;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class SnapshotManager {

    private static final Logger LOGGER = Logger.getLogger(SnapshotManager.class.getName());

    private static final byte TYPE_STRING = 0;
    private static final byte TYPE_LIST = 1;
    private static final byte TYPE_SET = 2;
    private static final byte TYPE_HASH = 3;

    private final Database database;
    private final ServerConfig config;
    private final ScheduledExecutorService autoSaveExecutor = Executors.newSingleThreadScheduledExecutor();

    public SnapshotManager(Database database, ServerConfig config) {
        this.database = database;
        this.config = config;
    }

    private Path snapshotPath() {
        return Path.of(config.getPersistenceDirectory(), config.getSnapshotFilename());
    }

    @PostConstruct
    void initialize() {
        try {
            load(snapshotPath());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load snapshot on startup", e);
        }

        int intervalSeconds = config.getSnapshotIntervalSeconds();
        if (intervalSeconds > 0) {
            autoSaveExecutor.scheduleAtFixedRate(this::autoSave, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
            LOGGER.info("Periodic auto-save enabled every " + intervalSeconds + "s");
        }
    }

    @PreDestroy
    void shutdown() {
        autoSaveExecutor.shutdown();
    }

    private void autoSave() {
        try {
            save();
            LOGGER.info("Auto-save completed");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Auto-save failed", e);
        }
    }

    public void save() throws IOException {
        save(snapshotPath());
    }

    public void save(Path targetFile) throws IOException {
        Path tempFile = Path.of(targetFile.toString() + ".tmp");

        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(tempFile))) {
            Map<String, RedisValue> entries = database.snapshotEntries();
            out.writeInt(entries.size());

            for (Map.Entry<String, RedisValue> entry : entries.entrySet()) {
                String key = entry.getKey();
                out.writeUTF(key);
                Long expiry = database.getExpiration(key);
                out.writeLong(expiry == null ? -1 : expiry);
                writeValue(out, entry.getValue());
            }
        }

        Files.move(tempFile, targetFile,
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public void load(Path sourceFile) throws IOException {
        if (!Files.exists(sourceFile)) {
            return;
        }

        database.clear();

        try (DataInputStream in = new DataInputStream(Files.newInputStream(sourceFile))) {
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                String key = in.readUTF();
                long expiry = in.readLong();
                RedisValue value = readValue(in);
                database.restoreEntry(key, value, expiry == -1 ? null : expiry);
            }
        }
    }

    private void writeValue(DataOutputStream out, RedisValue value) throws IOException {
        if (value instanceof RedisString s) {
            out.writeByte(TYPE_STRING);
            out.writeUTF(s.value());
        } else if (value instanceof RedisList l) {
            out.writeByte(TYPE_LIST);
            out.writeInt(l.values().size());
            for (String element : l.values()) {
                out.writeUTF(element);
            }
        } else if (value instanceof RedisSet s) {
            out.writeByte(TYPE_SET);
            out.writeInt(s.values().size());
            for (String member : s.values()) {
                out.writeUTF(member);
            }
        } else if (value instanceof RedisHash h) {
            out.writeByte(TYPE_HASH);
            out.writeInt(h.values().size());
            for (Map.Entry<String, String> field : h.values().entrySet()) {
                out.writeUTF(field.getKey());
                out.writeUTF(field.getValue());
            }
        } else {
            throw new IOException("Unknown RedisValue type: " + value.getClass());
        }
    }

    private RedisValue readValue(DataInputStream in) throws IOException {
        byte type = in.readByte();
        return switch (type) {
            case TYPE_STRING -> new RedisString(in.readUTF());
            case TYPE_LIST -> {
                int size = in.readInt();
                List<String> values = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    values.add(in.readUTF());
                }
                yield new RedisList(values);
            }
            case TYPE_SET -> {
                int size = in.readInt();
                Set<String> values = new HashSet<>();
                for (int i = 0; i < size; i++) {
                    values.add(in.readUTF());
                }
                yield new RedisSet(values);
            }
            case TYPE_HASH -> {
                int size = in.readInt();
                Map<String, String> values = new LinkedHashMap<>();
                for (int i = 0; i < size; i++) {
                    String field = in.readUTF();
                    String fieldValue = in.readUTF();
                    values.put(field, fieldValue);
                }
                yield new RedisHash(values);
            }
            default -> throw new IOException("Unknown type byte in snapshot: " + type);
        };
    }
}