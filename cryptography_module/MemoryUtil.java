package com.zero.crypto.util;

import java.util.Arrays;

public class MemoryUtil {
    public static void wipe(byte[] array) {
        if (array != null) {
            Arrays.fill(array, (byte) 0);
        }
    }

    public static void wipe(char[] array) {
        if (array != null) {
            Arrays.fill(array, '\0');
        }
    }
}