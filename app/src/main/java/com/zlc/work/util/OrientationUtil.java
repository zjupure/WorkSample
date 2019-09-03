package com.zlc.work.util;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;

/**
 * author: liuchun
 * date: 2019/8/14
 */
public class OrientationUtil {
    private static final String TAG = "OrientationUtil";

    private static Method sConvertFromTranslucent = null;
    private static Method sConvertToTranslucent = null;
    private static Method sGetActivityOptions = null;

    /**
     * 适配Android 8.0上透明Activity无法请求orientation
     */
    public static void requestScreenOrientation(Activity activity, int orientation) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O
                && activity.getApplicationInfo().targetSdkVersion > Build.VERSION_CODES.O
                && isTranslucentOrFloating(activity) && isFixedOrientation(orientation)) {
            // 适配Android O透明Activity设置屏幕方向
            // step1: 转成非透明的activity
            convertFromTranslucent(activity);
            // step2: 设置屏幕方向
            setRequestedOrientation(activity, orientation);
            // step3: 转回透明的activity
            convertToTranslucent(activity);
        } else {
            setRequestedOrientation(activity, orientation);
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean isTranslucentOrFloating(Activity activity) {
        final int[] windowAttr = new int[]{
                android.R.attr.windowIsTranslucent,
                android.R.attr.windowSwipeToDismiss,
                android.R.attr.windowIsFloating
        }; /*android.R.styleable.Window*/
        final TypedArray ta = activity.obtainStyledAttributes(windowAttr);
        final boolean isTranslucent =
                ta.getBoolean(0 /*android.R.styleable.Window_windowIsTranslucent*/,
                        false);
        final boolean isSwipeToDismiss = !ta.hasValue(
                0 /*android.R.styleable.Window_windowIsTranslucent*/)
                && ta.getBoolean(1/*android.R.styleable.Window_windowSwipeToDismiss*/, false);
        final boolean isFloating =
                ta.getBoolean(2/*android.R.styleable.Window_windowIsFloating*/,
                        false);
        final boolean isTranslucentOrFloating = isFloating || isTranslucent || isSwipeToDismiss;
        ta.recycle();
        return isTranslucentOrFloating;
    }

    private static boolean isFixedOrientation(int orientation) {
        return isFixedOrientationLandscape(orientation) || isFixedOrientationPortrait(orientation);
    }

    private static boolean isFixedOrientationLandscape(int orientation) {
        return orientation == SCREEN_ORIENTATION_LANDSCAPE
                || orientation == SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                || orientation == SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                orientation == SCREEN_ORIENTATION_USER_LANDSCAPE);
    }

    private static boolean isFixedOrientationPortrait(int orientation) {
        return orientation == SCREEN_ORIENTATION_PORTRAIT
                || orientation == SCREEN_ORIENTATION_SENSOR_PORTRAIT
                || orientation == SCREEN_ORIENTATION_REVERSE_PORTRAIT
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                orientation == SCREEN_ORIENTATION_USER_PORTRAIT);
    }

    private static void setRequestedOrientation(Activity activity, int orientation) {
        try {
            activity.setRequestedOrientation(orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把透明Activity转成非透明的
     */
    private static void convertFromTranslucent(Activity activity) {
        try {
            if (sConvertFromTranslucent == null) {
                sConvertFromTranslucent = Activity.class.getDeclaredMethod("convertFromTranslucent");
                sConvertFromTranslucent.setAccessible(true);
            }
            sConvertFromTranslucent.invoke(activity);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把透明Activity转回透明
     */
    private static boolean convertToTranslucent(Activity activity) {
        try {
            boolean changeCanvasToTranslucent = false;
            Class<?> translucentConversionListenerClass = Class.forName("android.app.Activity$TranslucentConversionListener");
            if (sConvertToTranslucent == null) {
                Class<?>[] paramTypes = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        ? new Class[]{translucentConversionListenerClass, ActivityOptions.class}
                        : new Class[]{translucentConversionListenerClass};
                sConvertToTranslucent = Activity.class.getDeclaredMethod("convertToTranslucent", paramTypes);
                sConvertToTranslucent.setAccessible(true);
            }
            InvocationHandler invocation = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return null;
                }
            };
            Object translucentConversionListener = Proxy.newProxyInstance(translucentConversionListenerClass.getClassLoader(),
                    new Class[]{translucentConversionListenerClass}, invocation);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptions options = getActivityOptions(activity);
                changeCanvasToTranslucent = (boolean) sConvertToTranslucent.invoke(activity, translucentConversionListener, options);
            } else {
                changeCanvasToTranslucent = (boolean) sConvertToTranslucent.invoke(activity, translucentConversionListener);
            }
            return changeCanvasToTranslucent;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private static ActivityOptions getActivityOptions(Activity activity) {
        ActivityOptions options = null;
        try {
            if (sGetActivityOptions == null) {
                sGetActivityOptions = Activity.class.getDeclaredMethod("getActivityOptions");
                sGetActivityOptions.setAccessible(true);
            }
            options = (ActivityOptions) sGetActivityOptions.invoke(activity);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return options;
    }
}
