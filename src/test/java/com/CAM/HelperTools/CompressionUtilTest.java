package com.CAM.HelperTools;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CompressionUtilTest {

    @Test
    public void testCompressAndDecompress() throws IOException {
        final String initialString = "hello there";
        final String compressed = CompressionUtil.compress(initialString);
        final String decompressed = CompressionUtil.decompress(compressed);
        System.out.println(compressed);
        assertThat(decompressed, is(initialString));
    }


}