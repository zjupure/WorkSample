package com.zlc.work;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zlc.work.keepalive.GrayReceiver;
import com.zlc.work.keepalive.GrayService;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * author: liuchun
 * date: 2018/10/17
 */
public class MainApplication extends Application {
    private static final String TAG = "MainApplication";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startKeepAlive();
        //Fresco.initialize(this);
    }

    private void startKeepAlive() {
        registerBroadcastReceiver();
        registerActivityObserver();

        //AmsHooker.hookAms();
    }

    private void registerActivityObserver() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                startGrayService();
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    private void startGrayService() {
        Intent intent = new Intent(this, GrayService.class);
        startService(intent);
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        BroadcastReceiver receiver = new GrayReceiver();
        registerReceiver(receiver, filter);
    }


    private void hookTelephonyManager() {
        try {
            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Method method = serviceManager.getDeclaredMethod("getService", String.class);
            method.setAccessible(true);

            IBinder telephony = (IBinder)method.invoke(null, Context.TELEPHONY_SERVICE);
            Log.i(TAG, "telephony binder: " + telephony.getClass().getName());
            IBinder proxy = (IBinder) Proxy.newProxyInstance(telephony.getClass().getClassLoader(),
                    new Class[]{IBinder.class}, new TelephonyBinder(telephony));

            Field field = serviceManager.getDeclaredField("sCache");
            field.setAccessible(true);
            Map<String, IBinder> sCache = (Map<String, IBinder>) field.get(null);
            sCache.put(Context.TELEPHONY_SERVICE, proxy);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class TelephonyBinder implements InvocationHandler {
        private IBinder rawBinder;

        public TelephonyBinder(IBinder rawBinder) {
            this.rawBinder = rawBinder;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("queryLocalInterface".equals(method.getName())) {
                Object raw = method.invoke(rawBinder, args);
                if (raw != null) {
                    return Proxy.newProxyInstance(raw.getClass().getClassLoader(),
                            new Class[]{raw.getClass()}, new InvocationHandler() {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                    if ("getDeviceId".equals(method.getName())) {
                                        Log.i(TAG, "hook getDeviceId success!");
                                    }
                                    return null;
                                }
                            });
                }
            } else if ("transact".equals(method.getName())) {

            }

            return null;
        }
    }
}
