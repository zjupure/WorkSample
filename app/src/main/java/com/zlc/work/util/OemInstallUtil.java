package com.zlc.work.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.VpnService;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.zlc.work.autoinstall.AutoInstallService;
import com.zlc.work.autoinstall.LocalVpnService;

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
    private static final String BAIDU_SEARCH = "com.baidu.searchbox";
    private static final String BAIDU_MARKET = "com.baidu.appsearch";

    private static void startVpnService(Activity activity) {
        Intent intent = VpnService.prepare(activity);
        if (intent != null) {
            activity.startActivityForResult(intent, 100);
        }

        Intent vpnIntent = new Intent(activity, LocalVpnService.class);
        activity.startService(vpnIntent);
    }

    public static void installApkFile(Context context, File apkFile) {

        Log.i(TAG, "installApkFile: " + apkFile.getAbsolutePath());
        //startVpnService((Activity)context);
        if (installByYingyongbao(context, apkFile)) {
            Log.i(TAG, "installApkFile by yingyongbao");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);  //Intent.ACTION_INSTALL_PACKAGE
        intent.setDataAndType(IoUtil.getUriFromFile(context, apkFile), "application/vnd.android.package-archive");
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);  //设置为非未知来源
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);  //接受返回结果
        intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);  //允许覆盖安装

        intent.putExtra("refererHost", "m.store.oppomobile.com");  // 部分oppo有效
        intent.putExtra("oppo_extra_pkg_name", "com.oppo.market");  // oppo
        intent.putExtra("installDir", true);  // 部分vivo有效

        intent.putExtra("caller_package", ANDROID_LAUNCHER_NAME);  //部分华为设备有效
        //intent.putExtra("android.intent.extra.PACKAGE_NAME", ANDROID_LAUNCHER_NAME);
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

    private static boolean jumpToSettingsIfNeed(Context context) {
        if (!SettingsUtil.isUnknownInstallAllowed(context)) {
            ToastCompat.makeText(context, "跳转设置页面，请打开允许安装未知来源选项", Toast.LENGTH_SHORT).show();
            SettingsUtil.jumpToSettingSecure(context);
            return true;
        }

        if (!SettingsUtil.isAccessibilityServiceEnable(context, AutoInstallService.class)) {
            ToastCompat.makeText(context, "跳转设置页面，请打开自动安装辅助功能", Toast.LENGTH_SHORT).show();
            SettingsUtil.jumpToSettingAccessibility(context);
            return true;
        }
        return false;
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
                        || "com.google.android.packageinstaller".equals(pkgName)
                        || "com.miui.packageinstaller".equals(pkgName)) {
                    return new Pair<>(pkgName, actName);
                }
            }
        }
        return null;
    }

    private static boolean installByYingyongbao(Context context, File apkFile) {
        if (isApkInstalled(context, TENCENT_YINGYONGBAO)) {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setDataAndType(IoUtil.getUriFromFile(context, apkFile), "application/vnd.android.package-archive");
            intent.setPackage(TENCENT_YINGYONGBAO);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return true;
            }
        }
        return false;
    }

    public static boolean downloadApkByPartner(Context context, String apkUrl) {
        PackageManager pm = context.getPackageManager();
//        if (true) {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.addCategory(Intent.CATEGORY_BROWSABLE);
//            intent.setData(Uri.parse(apkUrl));
//            context.startActivity(intent);
//            return true;
//        }
//
//
        if (isApkInstalled(context, TENCENT_YINGYONGBAO)) {
            // 腾讯应用宝
            Intent intent = buildDownloadIntent(context, apkUrl, TENCENT_YINGYONGBAO);
            ComponentName cn = intent.resolveActivity(pm);
            if (cn != null) {
                intent.setAction(null);
                intent.setComponent(cn);
                intent.putExtra("outer_call_id", "100");
                intent.putExtra("downl_url", apkUrl);
                Log.i(TAG, "start tencent yingyongbao download apk: " + apkUrl);
                context.startActivity(intent);
                return true;
            }
        }

        if (isApkInstalled(context, BAIDU_MARKET)) {
            // 百度手机助手下载
            Intent intent = buildDownloadIntent(context, apkUrl, BAIDU_MARKET);
            if (intent.resolveActivity(pm) != null) {
                Log.i(TAG, "start baidu shoujizhushou download apk: " + apkUrl);
                context.startActivity(intent);
                return true;
            }
        }

        if (isApkInstalled(context, BAIDU_SEARCH)) {
            // 手百，浏览器
            Intent intent = buildDownloadIntent(context, apkUrl, BAIDU_SEARCH);
            if (intent.resolveActivity(pm) != null) {
                Log.i(TAG, "start shouji baidu download apk: " + apkUrl);
                context.startActivity(intent);
                return true;
            }
        }
        return false;
    }

    private static Intent buildDownloadIntent(Context context, String apkUrl, String pkgName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(apkUrl));
        intent.setPackage(pkgName);
//        if (!TextUtils.isEmpty(pkgName)) {
//            intent.setPackage(pkgName);
//        }
        return intent;
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
