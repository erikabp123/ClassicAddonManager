package com.CAM.HelperTools;

import java.io.*;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionUtil {
    public static String compress(String input) throws IOException {
        if (input == null || input.length() == 0) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(input.getBytes());
        gzipOutputStream.close();

        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }

    public static String decompress(String input) throws IOException {
        if (input == null) {
            return null;
        }
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(input)));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gzipInputStream));
        StringBuilder decompressedBuilder = new StringBuilder();
        while (bufferedReader.ready()) {
            decompressedBuilder.append(bufferedReader.readLine());
        }
        return decompressedBuilder.toString();
    }
}
