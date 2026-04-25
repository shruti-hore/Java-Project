package client.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class NonceCounterStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void testSequentialIncrement() {
        Path storePath = tempDir.resolve("nonce.store");
        NonceCounterStore store = new NonceCounterStore(storePath);
        
        assertEquals(0, store.getAndIncrement("doc-1"));
        assertEquals(1, store.getAndIncrement("doc-1"));
        assertEquals(2, store.getAndIncrement("doc-1"));
    }

    @Test
    void testPersistAndReload() {
        Path storePath = tempDir.resolve("nonce.store");
        NonceCounterStore store1 = new NonceCounterStore(storePath);
        
        store1.getAndIncrement("doc-1"); // 0
        store1.getAndIncrement("doc-1"); // 1
        store1.getAndIncrement("doc-1"); // 2
        
        NonceCounterStore store2 = new NonceCounterStore(storePath);
        store2.load();
        
        assertEquals(3, store2.getAndIncrement("doc-1"));
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        Path storePath = tempDir.resolve("nonce.store");
        NonceCounterStore store = new NonceCounterStore(storePath);
        Set<Long> values = Collections.newSetFromMap(new ConcurrentHashMap<>());
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 1000; i++) {
            executor.submit(() -> {
                values.add(store.getAndIncrement("doc-1"));
            });
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        
        assertEquals(1000, values.size(), "All 1000 returned values must be distinct");
        for (long i = 0; i < 1000; i++) {
            assertTrue(values.contains(i), "Missing value: " + i);
        }
    }

    @Test
    void testCorruptedFile() throws IOException {
        Path storePath = tempDir.resolve("nonce.store");
        Files.writeString(storePath, "corrupted-data-no-equals");
        
        NonceCounterStore store = new NonceCounterStore(storePath);
        assertThrows(RuntimeException.class, store::load, "load() must throw exception on corrupted file");
    }
}
