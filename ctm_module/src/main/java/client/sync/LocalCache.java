package client.sync;

import client.crypto.EncryptedDocumentPayload;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * WIRE-FIX: Local cache for encrypted document versions.
 * Uses SQLite to persist document state, enabling offline access and sync tracking.
 */
public class LocalCache {
    private final String dbPath;

    public LocalCache(String dbPath) {
        this.dbPath = dbPath;
        init();
    }

    private void init() {
        try (Connection conn = getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS local_docs (" +
                         "doc_uuid TEXT PRIMARY KEY," +
                         "team_id TEXT," +
                         "version_seq INTEGER," +
                         "ciphertext_base64 TEXT," +
                         "nonce_base64 TEXT," +
                         "aad_base64 TEXT," +
                         "dirty INTEGER DEFAULT 0" +
                         ")";
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to init LocalCache at " + dbPath, e);
        }
    }

    private Connection getConnection() throws SQLException {
        // Ensure the directory exists
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    public void cacheDocument(String docUuid, String teamId, int versionSeq, EncryptedDocumentPayload payload, boolean dirty) {
        String sql = "INSERT OR REPLACE INTO local_docs (doc_uuid, team_id, version_seq, ciphertext_base64, nonce_base64, aad_base64, dirty) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, docUuid);
            pstmt.setString(2, teamId);
            pstmt.setInt(3, versionSeq);
            pstmt.setString(4, payload.ciphertextBase64());
            pstmt.setString(5, payload.nonceBase64());
            pstmt.setString(6, payload.aadBase64());
            pstmt.setInt(7, dirty ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<EncryptedDocumentPayload> getDocument(String docUuid) {
        String sql = "SELECT ciphertext_base64, nonce_base64, aad_base64, version_seq FROM local_docs WHERE doc_uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, docUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new EncryptedDocumentPayload(
                    rs.getString("ciphertext_base64"),
                    rs.getString("nonce_base64"),
                    rs.getString("aad_base64"),
                    0, // counter not stored
                    rs.getInt("version_seq")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<String> getDirtyDocumentUuids() {
        List<String> uuids = new ArrayList<>();
        String sql = "SELECT doc_uuid FROM local_docs WHERE dirty = 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                uuids.add(rs.getString("doc_uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uuids;
    }

    public void markClean(String docUuid, int newVersionSeq) {
        String sql = "UPDATE local_docs SET dirty = 0, version_seq = ? WHERE doc_uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newVersionSeq);
            pstmt.setString(2, docUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteDocument(String docUuid) {
        String sql = "DELETE FROM local_docs WHERE doc_uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, docUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getDocumentsForTeam(String teamId) {
        List<String> uuids = new ArrayList<>();
        String sql = "SELECT doc_uuid FROM local_docs WHERE team_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teamId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                uuids.add(rs.getString("doc_uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uuids;
    }

    public String getTeamId(String docUuid) {
        String sql = "SELECT team_id FROM local_docs WHERE doc_uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, docUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("team_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
