package com.zlc.work.media;

import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.zlc.work.R;
import com.zlc.work.util.ScreenUtil;
import com.zlc.work.util.ToastCompat;

/**
 * author: liuchun
 * date: 2019-05-11
 */
public class VideoViewActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "PlayerActivity";

    private static final String VIDEO_URL = "http://wxsnsdy.tc.qq.com/105/20210/snsdyvideodownload?filekey=30280201010421301f0201690402534804102ca905ce620b1241b726bc41dcff44e00204012882540400&bizid=1023&hy=SH&fileparam=302c020101042530230204136ffd93020457e3c4ff02024ef202031e8d7f02030f42400204045a320a0201000400";

    private MediaController mMediaController;
    private VideoView mVideoView;

    private boolean isPaused = false;
    private int currentPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        setContentView(R.layout.activity_videoview);
        setupVideoView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged");
        setVideoViewSize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPaused) {
            mVideoView.seekTo(currentPosition);
            mVideoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentPosition = mVideoView.getCurrentPosition();
        mVideoView.stopPlayback();
        isPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "MediaPlayer onCompletion");
        //mVideoView.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "MediaPlayer onError");
        ToastCompat.makeText(this, "播放出错", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "MediaPlayer onPrepared");
        mVideoView.seekTo(currentPosition);
        mVideoView.start();
    }

    private void setupVideoView() {
        mVideoView = findViewById(R.id.player_area);
        mMediaController = new MediaController(this);
        mVideoView.setMediaController(mMediaController);

        setVideoViewSize();

        mVideoView.setVideoURI(Uri.parse(VIDEO_URL));
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
    }

    private void setVideoViewSize() {
        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        if (ScreenUtil.isLand(this)) {
            // 横屏状态，设置为全屏播放
            lp.width = dm.widthPixels;
            lp.height = dm.heightPixels;
        } else {
            // 竖屏状态，设置比例为4：3
            lp.width = dm.widthPixels;
            lp.height = dm.widthPixels * 9 / 16;
        }
        mVideoView.setLayoutParams(lp);
        PlayerTool.setupImmersive(this);
    }
}
