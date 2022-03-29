package com.yandex.ydb.demo.ydb;

import java.util.zip.CRC32;

/**
 *
 * @author Alexandr Gorshenin
 */
public class HashTool {
    private static final String HASH_REGEXP = "[a-zA-Z0-9]+";

    private HashTool() { }

    public static String hash(String url) {
        CRC32 crc = new CRC32();
        crc.update(url.getBytes());
        String enc = Long.toHexString(crc.getValue());
        // left padding with zeros
        return ("00000000" + enc).substring(enc.length());
    }

    public static boolean isHash(String hash) {
        return hash != null && hash.length() == 8 && hash.matches(HASH_REGEXP);
    }
}
