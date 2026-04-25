package auth.session;

import java.security.PrivateKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class SessionState {
    private final String userId;
    private final String jwt;
    private final byte[] authSigningKey;        // 32 bytes
    private final PrivateKey x25519PrivateKey;
    private final byte[] x25519PublicKeyBytes;  // 32 bytes
    private final Map<String, byte[]> teamKeys; // teamId → 32-byte AES key

    public SessionState(String userId, String jwt, byte[] authSigningKey,
                        PrivateKey x25519PrivateKey, byte[] x25519PublicKeyBytes) {
        this.userId = userId;
        this.jwt = jwt;
        this.authSigningKey = authSigningKey;
        this.x25519PrivateKey = x25519PrivateKey;
        this.x25519PublicKeyBytes = x25519PublicKeyBytes;
        this.teamKeys = new HashMap<>();
    }

    public String getJwt() {
        return jwt;
    }

    public String getUserId() {
        return userId;
    }

    public byte[] getAuthSigningKey() {
        return authSigningKey;
    }

    public PrivateKey getX25519PrivateKey() {
        return x25519PrivateKey;
    }

    public byte[] getX25519PublicKeyBytes() {
        return x25519PublicKeyBytes;
    }

    public void addTeamKey(String teamId, byte[] teamKey) {
        teamKeys.put(teamId, teamKey);
    }

    public byte[] getTeamKey(String teamId) {
        byte[] key = teamKeys.get(teamId);
        if (key == null) {
            throw new IllegalStateException("Team key not found for ID: " + teamId);
        }
        return key;
    }

    /**
     * Zeros all byte[] fields and clears teamKeys to ensure no key material lingers in heap.
     */
    public void zero() {
        if (authSigningKey != null) {
            Arrays.fill(authSigningKey, (byte) 0);
        }
        if (x25519PublicKeyBytes != null) {
            Arrays.fill(x25519PublicKeyBytes, (byte) 0);
        }
        
        // Zero each team key before clearing the map
        for (byte[] key : teamKeys.values()) {
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
        }
        teamKeys.clear();
    }
}
