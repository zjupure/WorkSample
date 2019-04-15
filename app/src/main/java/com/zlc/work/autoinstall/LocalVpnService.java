package com.zlc.work.autoinstall;

import android.content.Intent;
import android.net.VpnService;
import android.util.Log;

/**
 * author: liuchun
 * date: 2018/11/5
 */
public class LocalVpnService extends VpnService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalVpnService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }
}
