package com.zlc.work.keepalive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * author: liuchun
 * date: 2018/10/17
 */
public class GrayReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent gray = new Intent(context, GrayService.class);
        context.startService(gray);
    }
}
