package com.zlc.work.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

/**
 * author: liuchun
 * date: 2018/10/17
 */
public class SettingsUtil {

    /**
     * 检查设置页面的，辅助服务功能是否开启
     */
    public static boolean isAccessibilityServiceEnable(Context context, Class<?> service) {
        ContentResolver cr = context.getContentResolver();
        int enable = Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, 0);
        if (enable <= 0) {
            return false;
        }
        String services = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (TextUtils.isEmpty(services)) {
            return false;
        }
        TextUtils.SimpleStringSplitter split = new TextUtils.SimpleStringSplitter(':');
        split.setString(services);
        while (split.hasNext()) {
            String name = context.getPackageName() + "/" + service.getName();
            if (name.equalsIgnoreCase(split.next())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 跳转设置页面---辅助功能
     */
    public static void jumpToSettingAccessibility(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 是否允许安装未知来源的APP
     */
    public static boolean isUnknownInstallAllowed(Context context) {
        ContentResolver cr = context.getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_INSTALL_PACKAGES)
                == PackageManager.PERMISSION_GRANTED) {
            PackageManager pm = context.getPackageManager();
            return pm.canRequestPackageInstalls();
        }
        int result = Settings.Secure.getInt(cr, Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
        return result == 1;
    }

    /**
     * 跳转设置--安全界面，允许安装未知来源APP
     */
    public static void jumpToSettingSecure(Context context) {
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
