package com.zlc.work.keepalive;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/**
 * author: liuchun
 * date: 2018/10/17
 */
public class GrayService extends Service {
    private final static int GRAY_SERVICE_ID = 1000;

    private boolean isFirst = true;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("MainActivity", "onBind() called");
        return new Binder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // 直接new Notification，通知栏不会显示
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            Intent gray = new Intent(this, GrayInnerService.class);
            startService(gray);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * API 18上灰色保活
     */
    public static class GrayInnerService extends Service {

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }
}
