package crypto.internal;

import java.util.Arrays;

/**
 * Task CRY-08: Payload Padding.
 * Implements 256-byte block alignment using a PKCS#7-style scheme.
 */
public class PaddingUtil {

    private static final int BLOCK_SIZE = 256;

    /**
     * Pads a byte array to the nearest 256-byte boundary.
     * Always adds at least one byte of padding. If input is already block-aligned,
     * a full extra block (256 bytes) is added.
     *
     * @param plaintext Original data.
     * @return Padded data, length is a multiple of 256.
     */
    public byte[] pad(byte[] plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("Plaintext cannot be null");
        }

        int padLength = BLOCK_SIZE - (plaintext.length % BLOCK_SIZE);
        // Note: if len % 256 == 0, padLength will be 256.
        
        byte[] padded = new byte[plaintext.length + padLength];
        System.arraycopy(plaintext, 0, padded, 0, plaintext.length);
        
        // Fill padding bytes with the length value.
        // For padLength = 256, (byte)256 is 0.
        byte padValue = (byte) (padLength & 0xFF);
        for (int i = plaintext.length; i < padded.length; i++) {
            padded[i] = padValue;
        }
        
        return padded;
    }

    /**
     * Removes PKCS#7-style padding from a 256-byte aligned array.
     *
     * @param padded Padded data.
     * @return Original plaintext.
     * @throws IllegalArgumentException if padding is invalid or alignment is wrong.
     */
    public byte[] unpad(byte[] padded) {
        if (padded == null) {
            throw new IllegalArgumentException("Padded data cannot be null");
        }
        if (padded.length == 0 || padded.length % BLOCK_SIZE != 0) {
            throw new IllegalArgumentException("Invalid alignment: length must be a multiple of 256");
        }

        // Read last byte to determine padding length
        int lastByte = padded[padded.length - 1] & 0xFF;
        int effectivePadLength = (lastByte == 0) ? 256 : lastByte;
        
        if (effectivePadLength > padded.length) {
            throw new IllegalArgumentException("Padding length exceeds array size");
        }

        // Validate all padding bytes
        for (int i = padded.length - effectivePadLength; i < padded.length; i++) {
            if ((padded[i] & 0xFF) != lastByte) {
                throw new IllegalArgumentException("Invalid padding pattern");
            }
        }

        return Arrays.copyOfRange(padded, 0, padded.length - effectivePadLength);
    }
}
