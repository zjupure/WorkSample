package com.zlc.work.autoinstall;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * author: liuchun
 * date: 2018/11/2
 */
public class ApkItem {

    public String apkPath;
    public String appName;
    public String appPkgName;
    public Drawable appIcon;

    public ApkItem(Context context, String apkPath) {
        this.apkPath = apkPath;

        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            if (pi == null) {
                return;
            }

            appName = pi.applicationInfo.name;
            appPkgName = pi.packageName;
            appIcon = pi.applicationInfo.loadIcon(pm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
