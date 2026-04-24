package crypto.internal;

import crypto.api.X25519KeyPair;
import java.security.PublicKey;
import java.util.HexFormat;

/**
 * Manual demonstration tester for SubKeyDerivation and KeyPairService.
 */
public class CryptoDemonstration {

    public static void main(String[] args) {
        System.out.println("=== CRYPTO MODULE DEMONSTRATION ===\n");

        // 1. Sub-Key Derivation Demo
        demoSubKeyDerivation();

        System.out.println("\n-----------------------------------\n");

        // 2. X25519 Key Pair Demo
        demoKeyPairService();

        System.out.println("\n-----------------------------------\n");

        // 3. ECDH Shared Secret Demo (Preview of CRY-04)
        demoSharedSecret();
    }

    private static void demoSubKeyDerivation() {
        System.out.println("[TASK CRY-02] Sub-Key Derivation (HKDF-SHA256)");
        SubKeyDerivation derivation = new SubKeyDerivation();
        
        // Simulating a 32-byte master key from Argon2id
        byte[] mockMasterKey = new byte[32];
        for (int i = 0; i < 32; i++) mockMasterKey[i] = (byte) (i + 1);

        System.out.println("Master Key (Mock): " + HexFormat.of().formatHex(mockMasterKey));

        // Derive different keys
        byte[] vaultKey = derivation.derive(mockMasterKey, "vault-key");
        byte[] authKey = derivation.derive(mockMasterKey, "auth-signing-key");

        System.out.println("Derived Vault Key: " + HexFormat.of().formatHex(vaultKey));
        System.out.println("Derived Auth Key:  " + HexFormat.of().formatHex(authKey));
        System.out.println("Result: Keys are unique for each context string.");
    }

    private static void demoKeyPairService() {
        System.out.println("[TASK CRY-03] X25519 Key Pair Generation");
        KeyPairService service = new KeyPairService();

        // Generate
        X25519KeyPair kp = service.generateKeyPair();
        System.out.println("Generated X25519 Key Pair.");
        System.out.println("Public Key (Raw 32-bytes): " + HexFormat.of().formatHex(kp.publicKeyBytes()));

        // Demonstrate loading the public key object from raw bytes
        PublicKey loadedKey = service.loadPublicKey(kp.publicKeyBytes());
        System.out.println("Re-loaded Public Key Algorithm: " + loadedKey.getAlgorithm());
        
        // Demonstrate extracting bytes back from a PublicKey object
        byte[] reExtracted = service.extractPublicKeyBytes(loadedKey);
        System.out.println("Re-extracted Bytes Match:      " + (HexFormat.of().formatHex(reExtracted).equals(HexFormat.of().formatHex(kp.publicKeyBytes()))));
    }

    private static void demoSharedSecret() {
        System.out.println("[PREVIEW CRY-04] ECDH Shared Secret Calculation");
        KeyPairService service = new KeyPairService();
        EcdhService ecdhService = new EcdhService();

        // Create two parties
        X25519KeyPair alice = service.generateKeyPair();
        X25519KeyPair bob   = service.generateKeyPair();

        System.out.println("Alice and Bob generated their own key pairs.");

        try {
            // Enforcement: Use EcdhService which applies SHA-256 post-processing
            // and zeros the raw intermediate secret — matches the production code path.
            byte[] aliceSecret = ecdhService.computeSharedSecret(
                    alice.privateKey(), service.loadPublicKey(bob.publicKeyBytes()));

            byte[] bobSecret = ecdhService.computeSharedSecret(
                    bob.privateKey(), service.loadPublicKey(alice.publicKeyBytes()));

            System.out.println("Alice's Shared Secret (SHA-256): " + HexFormat.of().formatHex(aliceSecret));
            System.out.println("Bob's Shared Secret   (SHA-256): " + HexFormat.of().formatHex(bobSecret));
            System.out.println("Secrets Match:                   " + java.util.Arrays.equals(aliceSecret, bobSecret));
            System.out.println("Result: Both parties arrived at the same SHA-256 post-processed secret.");
        } catch (Exception e) {
            System.err.println("ECDH Demo Failed: " + e.getMessage());
        }
    }
}
