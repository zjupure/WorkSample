package com.zlc.work.autoinstall;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * author: liuchun
 * date: 2018/11/2
 */
public class ApkInstallerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Log.i("ApkInstallerActivity", "intent: " + intent.toString());
    }
}
