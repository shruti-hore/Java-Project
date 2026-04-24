package crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * PIPE-01 Fix [Nonce uniqueness]: Counter for a document must never go backwards. 
 * Persisted after every increment using atomic write to prevent data loss or corruption.
 */
public class NonceCounterStore {

    // docUuid (String) -> current counter value (Long)
    private final Map<String, Long> counters = new ConcurrentHashMap<>();
    private final Path storePath; // path to local file, injected at construction

    public NonceCounterStore(Path storePath) {
        this.storePath = storePath;
    }

    /**
     * Returns current value, increments, persists. Never returns the same value twice
     * for the same docUuid. If docUuid is new, starts at 0.
     */
    public synchronized long getAndIncrement(String docUuid) {
        long currentValue = counters.getOrDefault(docUuid, 0L);
        counters.put(docUuid, currentValue + 1);
        persist();
        return currentValue;
    }

    /**
     * Called once on startup. Reads persisted state into counters map.
     * If file does not exist, starts with empty map (all new docs begin at 0).
     */
    public void load() {
        if (!Files.exists(storePath)) {
            return;
        }
        try (var lines = Files.lines(storePath, StandardCharsets.UTF_8)) {
            for (String line : (Iterable<String>) lines::iterator) {
                if (line.isBlank()) continue;
                String[] parts = line.split("=", 2);
                if (parts.length != 2) {
                    throw new IOException("Invalid line format: " + line);
                }
                counters.put(parts[0], Long.parseLong(parts[1]));
            }
        } catch (IOException | NumberFormatException e) {
            // Requirement 107: load() throws IOException if corrupted
            throw new RuntimeException("Failed to load NonceCounterStore: " + e.getMessage(), e);
        }
    }

    /**
     * Called inside getAndIncrement after every increment.
     * Writes counters map to storePath as line-delimited "uuid=value" pairs.
     * Must be atomic: write to a temp file, then rename over storePath.
     */
    private void persist() {
        Path tempFile = storePath.resolveSibling(storePath.getFileName().toString() + ".tmp");
        try {
            String content = counters.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("\n"));
            
            Files.writeString(tempFile, content, StandardCharsets.UTF_8, 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            Files.move(tempFile, storePath, 
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist NonceCounterStore: " + e.getMessage(), e);
        }
    }
}
