package com.zlc.work.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * author: liuchun
 * date: 2018/10/15
 */
public class Md5Util {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final int BUF_SIZE = 16 * 1024;

    /**
     * 字符串做小写md5
     */
    public static String md5(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(data.getBytes());
            byte[] bytes = digest.digest();

            return byteArrayToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 文件做md5
     */
    public static String md5(File file) {
        FileInputStream fis = null;
        DigestInputStream dis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            dis = new DigestInputStream(fis, digest);
            byte[] buffer = new byte[BUF_SIZE];
            while (dis.read(buffer) > 0) {
                // skip, go on
            }

            byte[] bytes = digest.digest();
            return byteArrayToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtil.close(dis);
            IoUtil.close(fis);
        }
        return "";
    }

    /**
     * 字节数组转换成16进制形式
     */
    public static String byteArrayToHex(byte[] byteArray) {
        StringBuilder builder = new StringBuilder();
        for (byte b : byteArray) {
            builder.append(HEX_DIGITS[b >> 4 & 0x0f]);
            builder.append(HEX_DIGITS[b & 0x0f]);
        }
        return builder.toString();
    }

    /**
     * 字节转换成16进制0x的形式
     */
    public static String byteToHex(byte b) {
        StringBuilder builder = new StringBuilder("0x");
        builder.append(HEX_DIGITS[b >> 4 & 0x0f]);
        builder.append(HEX_DIGITS[b & 0x0f]);
        return builder.toString();
    }
}
