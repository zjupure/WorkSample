package com.zlc.work.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.util.List;

/**
 * author: liuchun
 * date: 2018/11/2
 */
public class OemInstallUtil {
    private static final String TAG = "OemInstallUtil";

    private static final String ANDROID_LAUNCHER_NAME = "com.google.launcher";
    private static final String ANDROID_INSTALLER_NAME = "com.android.packageinstaller";

    private static final String TENCENT_YINGYONGBAO = "com.tencent.android.qqdownloader";

    public static void installApkFile(Context context, File apkFile) {

        Log.i(TAG, "installApkFile: " + apkFile.getAbsolutePath());
        if (installByYingyongbao(context, apkFile)) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);  //Intent.ACTION_INSTALL_PACKAGE
        intent.setDataAndType(IoUtil.getUriFromFile(context, apkFile), "application/vnd.android.package-archive");
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);  //设置为非未知来源
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);  //接受返回结果
        intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);  //允许覆盖安装
        if (OsUtil.isOppo()) {
            intent.putExtra("refererHost", "m.store.oppomobile.com");  // oppo需要
            //intent.putExtra("oppo_extra_pkg_name", TENCENT_YINGYONGBAO);
        } else if (OsUtil.isVivo()) {
            intent.putExtra("installDir", true);  // vivo需要
        }

        intent.putExtra("caller_package", ANDROID_LAUNCHER_NAME);  //传递调用方的包名
        intent.putExtra("android.intent.extra.PACKAGE_NAME", ANDROID_LAUNCHER_NAME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        Pair<String, String> comp = getInstallerActivity(context);
        if (comp != null) {
            intent.setClassName(comp.first, comp.second);
        }

        PackageManager pm = context.getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            if (context instanceof Activity) {
                ((Activity)context).startActivityForResult(intent, 0);
            } else {
                context.startActivity(intent);
            }
        }
    }

    private static Pair<String, String> getInstallerActivity(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://"), "application/vnd.android.package-archive");
        List<ResolveInfo> queryIntentActivities = pm.queryIntentActivities(intent, 0);
        if (queryIntentActivities != null && queryIntentActivities.size() > 0) {
            for (ResolveInfo resolveInfo : queryIntentActivities) {
                String pkgName = resolveInfo.activityInfo.packageName;
                String actName = resolveInfo.activityInfo.name;
                if ("com.android.packageinstaller".equals(pkgName)
                        || "com.google.android.packageinstaller".equals(pkgName)) {
                    return new Pair<>(pkgName, actName);
                }
            }
        }
        return null;
    }

    private static boolean installByYingyongbao(Context context, File apkFile) {
        if (!isApkInstalled(context, TENCENT_YINGYONGBAO)) {
            return false;
        }

        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                Intent intent = new Intent();
                intent.setClassName(TENCENT_YINGYONGBAO, "com.tencent.pangu.activity.InstallerListenerActivity");
                intent.putExtra("path", apkFile.getAbsolutePath());
                intent.putExtra("package_name", pi.packageName);
                intent.putExtra("version_code", pi.versionCode);
                if (intent.resolveActivity(pm) != null) {
                    context.startActivity(intent);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isApkInstalled(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
            return pi != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
