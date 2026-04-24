package sync;

import crypto.EncryptedDocumentPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PIPE-07 Fix [failure mode]: LocalCache using SQLite for durable storage of 
 * decrypted document fields and a FIFO offline write queue. 
 * Ensures edits are processed in order and survive JVM restarts.
 */
public class LocalCache {

    private final String dbUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocalCache(String dbPath) throws SQLException {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initDb();
    }

    private void initDb() throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS document_cache (" +
                    "doc_uuid    TEXT PRIMARY KEY," +
                    "team_id     TEXT NOT NULL," +
                    "version_seq INTEGER NOT NULL," +
                    "fields_json TEXT NOT NULL," +
                    "cached_at   INTEGER NOT NULL" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS pending_operations (" +
                    "id              INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "doc_uuid        TEXT NOT NULL," +
                    "team_id         TEXT NOT NULL," +
                    "ciphertext_b64  TEXT NOT NULL," +
                    "nonce_b64       TEXT NOT NULL," +
                    "aad_b64         TEXT NOT NULL," +
                    "version_seq     INTEGER NOT NULL," +
                    "vector_clock    TEXT NOT NULL," +
                    "created_at      INTEGER NOT NULL," +
                    "retry_count     INTEGER NOT NULL DEFAULT 0" +
                    ");");
        }
    }

    public void cacheDocument(String docUuid, String teamId, int versionSeq, Map<String, Object> fields) throws SQLException, JsonProcessingException {
        String json = objectMapper.writeValueAsString(fields);
        String sql = "INSERT OR REPLACE INTO document_cache (doc_uuid, team_id, version_seq, fields_json, cached_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, docUuid);
            pstmt.setString(2, teamId);
            pstmt.setInt(3, versionSeq);
            pstmt.setString(4, json);
            pstmt.setLong(5, System.currentTimeMillis());
            pstmt.executeUpdate();
        }
    }

    public Optional<Map<String, Object>> getCachedFields(String docUuid) throws SQLException, JsonProcessingException {
        String sql = "SELECT fields_json FROM document_cache WHERE doc_uuid = ?";
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, docUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String json = rs.getString("fields_json");
                return Optional.of(objectMapper.readValue(json, Map.class));
            }
        }
        return Optional.empty();
    }

    public void enqueuePendingOperation(String docUuid, String teamId, EncryptedDocumentPayload payload, Map<String, Integer> vectorClock) throws SQLException, JsonProcessingException {
        String clockJson = objectMapper.writeValueAsString(vectorClock);
        String sql = "INSERT INTO pending_operations (doc_uuid, team_id, ciphertext_b64, nonce_b64, aad_b64, version_seq, vector_clock, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, docUuid);
            pstmt.setString(2, teamId);
            pstmt.setString(3, payload.ciphertextBase64());
            pstmt.setString(4, payload.nonceBase64());
            pstmt.setString(5, payload.aadBase64());
            pstmt.setInt(6, payload.versionSeq());
            pstmt.setString(7, clockJson);
            pstmt.setLong(8, System.currentTimeMillis());
            pstmt.executeUpdate();
        }
    }

    public List<PendingOperation> drainQueue() throws SQLException {
        // Requirement 605: ORDER BY created_at ASC for FIFO
        String sql = "SELECT * FROM pending_operations ORDER BY created_at ASC";
        List<PendingOperation> list = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(new PendingOperation(
                        rs.getLong("id"),
                        rs.getString("doc_uuid"),
                        rs.getString("team_id"),
                        rs.getString("ciphertext_b64"),
                        rs.getString("nonce_b64"),
                        rs.getString("aad_b64"),
                        rs.getInt("version_seq"),
                        rs.getString("vector_clock"),
                        rs.getInt("retry_count")
                ));
            }
        }
        return list;
    }

    public void deleteOperation(long operationId) throws SQLException {
        String sql = "DELETE FROM pending_operations WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, operationId);
            pstmt.executeUpdate();
        }
    }

    public void incrementRetryCount(long operationId) throws SQLException {
        String sql = "UPDATE pending_operations SET retry_count = retry_count + 1 WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, operationId);
            pstmt.executeUpdate();
        }
    }
}
