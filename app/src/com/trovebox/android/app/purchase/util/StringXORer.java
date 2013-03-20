package com.trovebox.android.app.purchase.util;


/**
 * Basic string encoding/decoding class via XOR
 */
public class StringXORer {
    public static String encode(String s, String key) {
        return base64Encode(xorWithKey(s.getBytes(), key.getBytes()));
    }

    public static String decode(String s, String key) {
        return new String(xorWithKey(base64Decode(s), key.getBytes()));
    }

    private static byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i % key.length]);
        }
        return out;
    }

    private static byte[] base64Decode(String s) {
        try {
            return Base64.decode(s);
        } catch (Base64DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.encode(bytes).replaceAll("\\s", "");

    }
}
