package com.project.jredis.command;

import com.project.jredis.persistence.SnapshotManager;
import com.project.jredis.protocol.RespSimpleString;
import com.project.jredis.protocol.RespValue;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class BgSaveCommand implements Command {

    private static final Logger LOGGER = Logger.getLogger(BgSaveCommand.class.getName());

    private final SnapshotManager snapshotManager;
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    public BgSaveCommand(SnapshotManager snapshotManager) {
        this.snapshotManager = snapshotManager;
    }

    @Override
    public String name() {
        return "BGSAVE";
    }

    @Override
    public RespValue execute(List<String> args) {
        // Fire-and-forget: the client gets an immediate reply while the snapshot
        // writes on a separate thread — that's the entire point of BGSAVE vs SAVE.
        backgroundExecutor.submit(() -> {
            try {
                snapshotManager.save();
                LOGGER.info("Background save completed");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Background save failed", e);
            }
        });
        return new RespSimpleString("Background saving started");
    }
}