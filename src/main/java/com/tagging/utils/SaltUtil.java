package com.tagging.utils;


import java.util.Random;

public class SaltUtil {
    private final static String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    /**
     * 生成盐
     *
     */
    public static String salt(int saltLength) {
        StringBuilder uuid = new StringBuilder();
        for (int i = 0; i < saltLength; i++) {
            char ch = str.charAt(new Random().nextInt(str.length()));
            uuid.append(ch);
        }
        return uuid.toString();
    }
}
