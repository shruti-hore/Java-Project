package crypto.util;

import java.util.Arrays;

public class MemoryUtil {

    // Wipes a byte array by filling it with zeros.
    public static void wipe(byte[] array) {
        if (array != null) {
            Arrays.fill(array, (byte) 0);
        }
    }

    // Wipes a char array (passwords) by filling it with null characters.
    public static void wipe(char[] array) {
        if (array != null) {
            Arrays.fill(array, '\0');
        }
    }
}