package com.zlc.work.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/**
 * author: liuchun
 * date: 2019/8/16
 */
public class LocaleUtils {

    public static void updateAppLanguage(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale newLocale = Locale.CHINA;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(newLocale);
        } else {
            configuration.locale = newLocale;
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public static Locale getLocale(Context context) {
        Locale locale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = LocaleList.getDefault();
            if (localeList != null && !localeList.isEmpty()) {
                locale = localeList.get(0);
            }
        } else {
            locale = Locale.getDefault();
        }
        if (locale == null) {
            Resources resources = context.getResources();
            if (resources != null && resources.getConfiguration() != null) {
                locale = resources.getConfiguration().locale;
            }
        }
        return locale;
    }
}
