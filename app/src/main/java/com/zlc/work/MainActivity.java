package com.zlc.work;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.zlc.work.autoinstall.AutoInstallActivity;
import com.zlc.work.deeplink.DeepLinkActivity;
import com.zlc.work.media.ExoPlayerActivity;
import com.zlc.work.media.PlayerActivity;
import com.zlc.work.media.VideoViewActivity;
import com.zlc.work.opengl.OpenGLActivity;
import com.zlc.work.ui.UiCompActivity;
import com.zlc.work.viewpager.ViewPagerActivity;
import com.zlc.work.webview.WebviewActivity;
import com.zlc.work.widget.bubble.BubblePopupWindow;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();

        String apkPath = getApplicationInfo().sourceDir;
        Log.i(TAG, "apkPath: " + apkPath);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findViews() {
        findViewById(R.id.deeplink).setOnClickListener(this);
        findViewById(R.id.autoinstall).setOnClickListener(this);
        findViewById(R.id.webview).setOnClickListener(this);
        findViewById(R.id.player).setOnClickListener(this);
        findViewById(R.id.exo_player).setOnClickListener(this);
        findViewById(R.id.videoview_player).setOnClickListener(this);
        findViewById(R.id.view_pager).setOnClickListener(this);
        findViewById(R.id.opengl).setOnClickListener(this);
        findViewById(R.id.bubble_anchor).setOnClickListener(this);
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
            case R.id.view_pager:
                intent.setClass(this, ViewPagerActivity.class);
                break;
            case R.id.opengl:
                intent.setClass(this, OpenGLActivity.class);
                break;
            case R.id.bubble_anchor:
                intent.setClass(this, UiCompActivity.class);
                break;
            default:
                break;
        }

        PackageManager pm = getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            startActivity(intent);
        }
        if (R.id.bubble_anchor == v.getId()) {
            showBubble(v);
        }
    }

    private void showBubble(View anchor) {
        BubblePopupWindow window = new BubblePopupWindow(this);
        window.setBubbleText("我是一个超长的气泡");
        //window.show(anchor, Gravity.BOTTOM);
        window.show(anchor, Gravity.TOP, Gravity.LEFT, 0);
    }
}
