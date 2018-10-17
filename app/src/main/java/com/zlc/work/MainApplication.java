package com.zlc.work;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.zlc.work.keepalive.GrayReceiver;
import com.zlc.work.keepalive.GrayService;

/**
 * author: liuchun
 * date: 2018/10/17
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        startKeepAlive();
    }

    private void startKeepAlive() {
        startGrayService();
        registerBroadcastReceiver();
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
