package com.zlc.work.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * author: liuchun
 * date: 2018/10/15
 */
public class IoUtil {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
