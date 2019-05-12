package com.zlc.work;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zlc.work.autoinstall.AutoInstallActivity;
import com.zlc.work.deeplink.DeepLinkActivity;
import com.zlc.work.media.ExoPlayerActivity;
import com.zlc.work.media.PlayerActivity;
import com.zlc.work.media.VideoViewActivity;
import com.zlc.work.webview.WebviewActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
    }

    private void findViews() {
        findViewById(R.id.deeplink).setOnClickListener(this);
        findViewById(R.id.autoinstall).setOnClickListener(this);
        findViewById(R.id.webview).setOnClickListener(this);
        findViewById(R.id.player).setOnClickListener(this);
        findViewById(R.id.exo_player).setOnClickListener(this);
        findViewById(R.id.videoview_player).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.deeplink:
                intent.setClass(this, DeepLinkActivity.class);
                break;
            case R.id.autoinstall:
                intent.setClass(this, AutoInstallActivity.class);
                break;
            case R.id.webview:
                intent.setClass(this, WebviewActivity.class);
                break;
            case R.id.player:
                intent.setClass(this, PlayerActivity.class);
                break;
            case R.id.exo_player:
                intent.setClass(this, ExoPlayerActivity.class);
                break;
            case R.id.videoview_player:
                intent.setClass(this, VideoViewActivity.class);
                break;
            default:
                break;
        }

        PackageManager pm = getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            startActivity(intent);
        }
    }
}
