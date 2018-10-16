package com.zlc.work.util;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author: liuchun
 * date: 2018/10/16
 *
 * wrap {@link android.os.SystemProperties} due to it is @hide
 */
public class SystemProperties {

    private static Class<?> clazz = null;
    private static Map<String, Method> sMethodCache = new ConcurrentHashMap<>();

    private static Method getMethod(String methodName, Class<?>... paramTypes)
        throws ClassNotFoundException, NoSuchMethodException {

        String key = methodName;
        if (paramTypes.length > 0) {
            for (Class<?> paramType : paramTypes) {
                key += String.valueOf(paramType.hashCode());
            }
        }

        Method method = sMethodCache.get(key);
        if (method != null) {
            // hit from cache
            return method;
        }

        if (clazz == null) {
            clazz = Class.forName("android.os.SystemProperties");
        }
        method = clazz.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        sMethodCache.put(key, method);
        return method;
    }

    public static String get(String key) {
        try {
            Method method = getMethod("get", String.class);
            return  (String)method.invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String get(String key, String def) {
        try {
            Method method = getMethod("get", String.class, String.class);
            return  (String)method.invoke(null, key, def);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    public static int getInt(String key, int def) {
        try {
            Method method = getMethod("getInt", String.class, String.class);
            return  (int)method.invoke(null, key, def);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    public static long getLong(String key, long def) {
        try {
            Method method = getMethod("getLong", String.class, String.class);
            return  (long)method.invoke(null, key, def);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    public static boolean getBoolean(String key, boolean def) {
        try {
            Method method = getMethod("getInt", String.class, String.class);
            return  (boolean)method.invoke(null, key, def);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    public static void set(String key, String val) {
        try {
            Method method = getMethod("set", String.class, String.class);
            method.invoke(null, key, val);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
