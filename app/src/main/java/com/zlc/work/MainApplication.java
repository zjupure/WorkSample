package com.zlc.work;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.zlc.work.hook.AmsHooker;
import com.zlc.work.hook.ResourcesHook;
import com.zlc.work.keepalive.GrayReceiver;
import com.zlc.work.keepalive.GrayService;

/**
 * author: liuchun
 * date: 2018/10/17
 */
public class MainApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ResourcesHook.hookResources(base);
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
}
