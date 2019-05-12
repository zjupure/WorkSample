package com.zlc.work.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * author: liuchun
 * date: 2019-05-11
 */
public class ScreenUtil {

    /**
     * 是否横屏状态
     */
    public static boolean isLand(@NonNull Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return true;
        } else if (configuration.orientation == Configuration.ORIENTATION_UNDEFINED) {
            // 未定义的状态，根据屏幕宽高判断
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            return dm.widthPixels > dm.heightPixels;
        }
        return false;
    }

    /**
     * 是否竖屏状态
     */
    public static boolean isPortrait(@NonNull Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        } else if (configuration.orientation == Configuration.ORIENTATION_UNDEFINED){
            // 未定义的状态，根据屏幕宽高判断
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            return dm.heightPixels > dm.widthPixels;
        }
        return false;
    }

    /**
     * 获取屏幕的宽度
     */
    public static int getScreenWidth(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm);
        } else {
            try {
                Method method = display.getClass().getDeclaredMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(wm.getDefaultDisplay(), dm);
            } catch (Exception e) {
                display.getMetrics(dm);
            }
        }
        return dm.widthPixels;
    }

    /**
     * 获取屏幕的高度，包含虚拟导航按键区域
     */
    public static int getScreenHeight(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm);
        } else {
            try {
                Method method = display.getClass().getDeclaredMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(wm.getDefaultDisplay(), dm);
            } catch (Exception e) {
                display.getMetrics(dm);
            }
        }
        return dm.heightPixels;
    }

    /**
     * 获取顶部状态栏的高度
     */
    public static int getStatusBarHeight(@NonNull Context context) {
        int height = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            // 通过resource获取
            height = context.getResources().getDimensionPixelSize(resId);
        } else {
            // 通过反射R$dimen类获取resId
            try {
                Class clazz = Class.forName("com.android.internal.R$dimen");
                Field field = clazz.getDeclaredField("status_bar_height");
                resId = (int)field.get(null);
                height = context.getResources().getDimensionPixelSize(resId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (height <= 0 && context instanceof Activity) {
            // 如果还没有拿到, Activity渲染之后，decorView的高度减去内容区域的高度
            Rect rect = new Rect();
            Window window = ((Activity)context).getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rect);
            int contentViewTop = window.findViewById(android.R.id.content).getTop();
            height = contentViewTop - rect.top;
        }

        return height;
    }

    /**
     * 获取底部虚拟导航栏的高度
     */
    public static int getNavigationBarHeight(@NonNull Context context) {
        int height = 0;
        int resId = context.getResources().getIdentifier("navigation_bar_height","dimen", "android");
        if (resId > 0) {
            height = context.getResources().getDimensionPixelSize(resId);
        } else {
            // 通过反射R$dimen类获取resId
            try {
                Class clazz = Class.forName("com.android.internal.R$dimen");
                Field field = clazz.getDeclaredField("navigation_bar_height");
                resId = (int)field.get(null);
                height = context.getResources().getDimensionPixelSize(resId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (height <= 0) {
            // 根据屏幕状态判断虚拟键的高度，如果虚拟导航显示，realMetrics要大于metrics的值；没有虚拟导航或者隐藏，则两者相等
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            // realMetrics - metrics值
            if (isLand(context)) {
                // 横屏的时候，虚拟导航显示在右侧
                height = getScreenWidth(context) - dm.widthPixels;
            } else {
                // 竖屏的时候，虚拟导航显示在底部
                height = getScreenHeight(context) - dm.heightPixels;
            }
        }
        return height;
    }

    /**
     * 是否全面屏启用
     */
    public static boolean navigationGestureEnable(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            int result = Settings.Global.getInt(context.getContentResolver(), getGestureKey(), 0);
            return result > 0;
        }
        return false;
    }

    private static String getGestureKey() {
        String brand = Build.BRAND;
        String key = "navigationbar_is_min";
        if (TextUtils.isEmpty(brand)) {
            key = "navigationbar_is_min";
        } else if (brand.equalsIgnoreCase("huawei")
            || brand.equalsIgnoreCase("honor")) {
            key = "navigationbar_is_min";
        } else if (brand.equalsIgnoreCase("xiaomi")) {
            key = "force_fsg_nav_bar";
        } else if (brand.equalsIgnoreCase("vivo")
            || brand.equalsIgnoreCase("oppo")) {
            key = "navigation_gesture_on";
        }
        return key;
    }

    /**
     * 获取屏幕的dpi
     */
    public static int getScreenDpi(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.densityDpi;
    }

    /**
     * 获取屏幕的density
     */
    public static float getScreenDensity(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.density;
    }
}