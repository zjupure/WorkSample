package com.zlc.work.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * author: liuchun
 * date: 2018/10/15
 */
public class IoUtil {

    /**
     * 安装apk文件
     */
    public static void installApkFile(Context context, File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(getUriFromFile(context, apkFile), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        PackageManager pm = context.getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * 获取文件URI
     */
    public static Uri getUriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                String authority = context.getPackageName() + ".fileprovider";
                return FileProvider.getUriForFile(context, authority, file);
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        return Uri.fromFile(file);
    }


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
