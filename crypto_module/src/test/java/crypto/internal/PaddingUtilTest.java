package crypto.internal;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;

public class PaddingUtilTest {

    private final PaddingUtil util = new PaddingUtil();

    @Test
    void shouldPadEmptyInputToBlockSize() {
        byte[] padded = util.pad(new byte[0]);
        assertThat(padded).hasSize(256);
        assertThat(padded).containsOnly((byte) 0); // 256 bytes of 256 (0)
    }

    @Test
    void shouldPadOneByteToBlockSize() {
        byte[] padded = util.pad(new byte[1]);
        assertThat(padded).hasSize(256);
        assertThat(padded[255]).isEqualTo((byte) 255);
    }

    @Test
    void shouldPad255BytesToBlockSize() {
        byte[] padded = util.pad(new byte[255]);
        assertThat(padded).hasSize(256);
        assertThat(padded[255]).isEqualTo((byte) 1);
    }

    @Test
    void shouldAddFullBlockIfAlreadyAligned() {
        byte[] input = new byte[256];
        byte[] padded = util.pad(input);
        assertThat(padded).hasSize(512);
        // Last 256 bytes should all be (byte)0
        for (int i = 256; i < 512; i++) {
            assertThat(padded[i]).isEqualTo((byte) 0);
        }
    }

    @Test
    void shouldPad257BytesToTwoBlocks() {
        byte[] padded = util.pad(new byte[257]);
        assertThat(padded).hasSize(512);
        assertThat(padded[511] & 0xFF).isEqualTo(512 - 257); // 255
    }

    @Test
    void roundtripShouldWorkForVariousLengths() {
        for (int i = 0; i <= 512; i++) {
            byte[] input = new byte[i];
            Arrays.fill(input, (byte) 0xAA);
            byte[] padded = util.pad(input);
            assertThat(padded.length % 256).isZero();
            byte[] unpadded = util.unpad(padded);
            assertThat(unpadded).isEqualTo(input);
        }
    }

    @Test
    void unpadShouldFailOnIncorrectAlignment() {
        assertThatThrownBy(() -> util.unpad(new byte[255]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("alignment");
    }

    @Test
    void unpadShouldFailOnInvalidPaddingPattern() {
        byte[] padded = util.pad(new byte[10]);
        padded[padded.length - 2] ^= 0x01; // Tamper with one padding byte
        
        assertThatThrownBy(() -> util.unpad(padded))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pattern");
    }

    @Test
    void unpadShouldFailOnZeroLastByteIfPatternIsInvalid() {
        // This is a tricky one. If last byte is 0 (256), then all 256 bytes must be 0.
        // If we provide a 256-byte array where the last byte is 0 but others are not, it should fail.
        byte[] badPad = new byte[256];
        badPad[0] = 1; // Not 0
        badPad[255] = 0;
        
        assertThatThrownBy(() -> util.unpad(badPad))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void unpadShouldAcceptAllZerosAs256BytePad() {
        byte[] allZeros = new byte[256];
        byte[] unpadded = util.unpad(allZeros);
        assertThat(unpadded).isEmpty();
    }
}
