package me.xuan.bdocr.sdk.utils;

import java.security.MessageDigest;

/**
 * Author: xuan
 * Created on 2019/10/23 15:24.
 * <p>
 * Describe:
 */
public class Util {
    public Util() {
    }

    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String md5Str = byteArrayToHex(md.digest(str.getBytes()));
            return md5Str;
        } catch (Exception var3) {
            var3.printStackTrace();
            return "";
        }
    }

    public static String byteArrayToHex(byte[] byteArray) {
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] resultCharArray = new char[byteArray.length * 2];
        int index = 0;
        byte[] var4 = byteArray;
        int var5 = byteArray.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            byte b = var4[var6];
            resultCharArray[index++] = hexDigits[b >>> 4 & 15];
            resultCharArray[index++] = hexDigits[b & 15];
        }

        return new String(resultCharArray);
    }
}
