package com.zlc.work.hook;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * author: liuchun
 * date: 2018/11/2
 */
public class AmsHooker {
    private static final String TAG = "AmsHooker";

    public static void hookAms() {
        try {
            Class<?> amnClz = Class.forName("android.app.ActivityManagerNative");
            // 获取gDefault字段
            Field gDefaultFd = amnClz.getDeclaredField("gDefault");
            gDefaultFd.setAccessible(true);
            Object gDefault = gDefaultFd.get(null);
            // 4.x上gDefault是一个android.util.Singleton对象
            Class<?> singleton = Class.forName("android.util.Singleton");
            Field mInstanceFd = singleton.getDeclaredField("mInstance");
            mInstanceFd.setAccessible(true);
            // ActivityManagerNative 的gDefault对象里面原始的 IActivityManager对象
            Object rowAm = mInstanceFd.get(gDefault);
            // 创建IActivityManager的一个动态代理，然后替换回去
            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[] { iActivityManagerInterface }, new IActivityManagerHandler(rowAm));
            mInstanceFd.set(gDefault, proxy);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public static class IActivityManagerHandler implements InvocationHandler {

        private Object iActivityManager;

        public IActivityManagerHandler(Object am) {
            iActivityManager = am;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Log.i(TAG, "method: " + method.getName() + ", paramType: " + Arrays.toString(args));
            if (method.getName().startsWith("startActivity")) {
                Log.i(TAG, "startActivity called: " + method.getName());
            }
            return method.invoke(iActivityManager, args);
        }
    }
}
