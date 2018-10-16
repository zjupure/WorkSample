package com.zlc.work.util;

import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Method;

/**
 * author: liuchun
 * date: 2018/10/16
 */
public class OsUtil {

    public static final String MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    public static final String MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    public static final String EMUI_VERSION = "ro.build.version.emui";
    public static final String EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    public static final String OPPO_VERSION = "ro.build.version.opporom";
    public static final String VIVO_VERSION = "ro.vivo.os.version";
    public static final String SMARTISAN_VERSION = "ro.smartisan.version";
    public static final String FLYME_ICON = "persist.sys.use.flyme.icon";
    public static final String FLYME_SETUP = "ro.meizu.setupwizard.flyme";
    public static final String BUILD_DISPLAY_ID = "ro.build.display.id";

    public enum OsType {
        MIUI,  //小米MIUI系统
        EMUI,  //华为EMUI系统
        FLYME, //魅族Flyme系统
        VIVO,  //VIVO系统
        OPPO,  //OPPO系统
        SMARTISAN, //锤子系统
        OTHER  //其他
    }

    private static OsType sOsType = null;

    private static OsType getOsType() {
        if (sOsType != null) {
            return sOsType;
        }

        if (isMiui()) {
            return sOsType = OsType.MIUI;
        } else if (isEmui()) {
            return sOsType = OsType.EMUI;
        } else if (isFlyme()) {
            return sOsType = OsType.FLYME;
        } else if (isVivo()) {
            return sOsType = OsType.VIVO;
        } else if (isOppo()) {
            return sOsType = OsType.OPPO;
        } else if (isSmartisan()) {
            return sOsType = OsType.SMARTISAN;
        } else {
            return sOsType = OsType.OTHER;
        }
    }

    public static boolean isMiui() {
        String verName = SystemProperties.get(MIUI_VERSION_NAME);
        String verCode = SystemProperties.get(MIUI_VERSION_CODE);
        return !TextUtils.isEmpty(verName) || !TextUtils.isEmpty(verCode);
    }

    public static boolean isEmui() {
        String version = SystemProperties.get(EMUI_VERSION);
        String apiLevel = SystemProperties.get(EMUI_API_LEVEL);
        return !TextUtils.isEmpty(version) || !TextUtils.isEmpty(apiLevel);
    }

    public static boolean isOppo() {
        String version = SystemProperties.get(OPPO_VERSION);
        return !TextUtils.isEmpty(version);
    }

    public static boolean isVivo() {
        String version = SystemProperties.get(VIVO_VERSION);
        return !TextUtils.isEmpty(version);
    }

    public static boolean isFlyme() {
        // flyme 5.1 has SmartBar
        try {
            // invoke Build.hasSmartBar()
            Method method = Build.class.getDeclaredMethod("hasSmartBar");
            return method != null;
        } catch (Exception e) {
            // ignore
        }
        String icon = SystemProperties.get(FLYME_ICON);
        String setup = SystemProperties.get(FLYME_SETUP);
        if (!TextUtils.isEmpty(icon) || !TextUtils.isEmpty(setup)) {
            return true;
        }

        String displayId = SystemProperties.get(BUILD_DISPLAY_ID);
        return !TextUtils.isEmpty(displayId) && displayId.toLowerCase().contains("flyme");
    }

    public static boolean isSmartisan() {
        String version = SystemProperties.get(SMARTISAN_VERSION);
        return !TextUtils.isEmpty(version);
    }
}
