package com.zlc.work.media;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.zlc.work.R;
import com.zlc.work.util.ScreenUtil;
import com.zlc.work.util.ToastCompat;

import java.io.IOException;

/**
 * author: liuchun
 * date: 2019-05-11
 */
public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnVideoSizeChangedListener,
        SurfaceHolder.Callback {
    private static final String TAG = "PlayerActivity";

    private static final float PLAY_ARAE_SIZE = 9.0f / 16;

    private static final String VIDEO_URL = "http://wxsnsdy.tc.qq.com/105/20210/snsdyvideodownload?filekey=30280201010421301f0201690402534804102ca905ce620b1241b726bc41dcff44e00204012882540400&bizid=1023&hy=SH&fileparam=302c020101042530230204136ffd93020457e3c4ff02024ef202031e8d7f02030f42400204045a320a0201000400";

    private MediaPlayer mMediaPlayer;

    private FrameLayout mPlayerArea;
    private SurfaceView mPlayerSurface;
    private FrameLayout mPlayerLoadingMask;

    private int mCurrentPosition = 0;
    private int mOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        setContentView(R.layout.activity_player);
        setupPlayerSurface();
        initMediaPlayer();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged");
        setPlayerAreaSize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        // onStop之后Surface会销毁
        if (mOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            // 恢复用户设置的横竖屏状态
            setRequestedOrientation(mOrientation);
        }
        PlayerTool.setupImmersive(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mCurrentPosition = mMediaPlayer.getCurrentPosition();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "MediaPlayer onCompletion");
        mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "MediaPlayer onError, what: " + what);
        ToastCompat.makeText(this, "播放出错", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "MediaPlayer onPrepared");
        startPlay();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.i(TAG, "MediaPlayer onVideoSizeChanged, width: " + width + ", height: " + height);
        setVideoSize(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        prepare();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged, width: " + width + ", height: " + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mCurrentPosition = mMediaPlayer.getCurrentPosition();
        }
    }

    private void setupPlayerSurface() {
        mPlayerArea = findViewById(R.id.player_area);
        mPlayerSurface = findViewById(R.id.player_surface);
        mPlayerSurface.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mPlayerSurface.setZOrderOnTop(true);
        mPlayerSurface.setZOrderMediaOverlay(true);
        //mPlayerSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mPlayerSurface.getHolder().addCallback(this);
        mPlayerLoadingMask = findViewById(R.id.player_loading_mask);
        setPlayerAreaSize();
    }

    private void setPlayerAreaSize() {
        ViewGroup.LayoutParams areaLp = mPlayerArea.getLayoutParams();
        ViewGroup.LayoutParams surfaceLp = mPlayerSurface.getLayoutParams();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        if (ScreenUtil.isLand(this)) {
            // 横屏状态，设置为全屏播放
            areaLp.width = surfaceLp.width = dm.widthPixels;
            areaLp.height = surfaceLp.height = dm.heightPixels;
        } else {
            // 竖屏状态，设置比例为4：3
            areaLp.width = surfaceLp.width = dm.widthPixels;
            areaLp.height = surfaceLp.height = (int)(dm.widthPixels * PLAY_ARAE_SIZE);
        }
        mOrientation = getRequestedOrientation();
        mPlayerArea.setLayoutParams(areaLp);
        mPlayerSurface.setLayoutParams(surfaceLp);
        PlayerTool.setupImmersive(this);
    }

    private void setVideoSize(int width, int height) {
        if (ScreenUtil.isLand(this)) {
            // skip，满屏播放
        } else {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int areaWidth = dm.widthPixels;
            int areaHeight = (int)(dm.widthPixels * PLAY_ARAE_SIZE);

            int videoWidth = areaWidth;
            int videoHeight = videoWidth * height / width;

            ViewGroup.LayoutParams areaLp = mPlayerArea.getLayoutParams();
            ViewGroup.LayoutParams lp = mPlayerSurface.getLayoutParams();
            areaLp.width = lp.width = videoWidth;
            areaLp.height = lp.height = videoHeight;
            mPlayerArea.setLayoutParams(areaLp);
            mPlayerSurface.setLayoutParams(lp);
        }
    }

    private void initMediaPlayer() {
        if (mMediaPlayer != null) {
            Log.i(TAG, "MediaPlayer has initialized");
            return;
        }

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(this, Uri.parse(VIDEO_URL));
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            //mMediaPlayer.setLooping(true);
        } catch (IOException e) {
            e.printStackTrace();
            mMediaPlayer = null;
        }
    }

    private void prepare() {
        initMediaPlayer();
        mMediaPlayer.setDisplay(mPlayerSurface.getHolder());
        mMediaPlayer.prepareAsync();
    }

    private void startPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(mCurrentPosition);
            mMediaPlayer.start();
        }
        mPlayerLoadingMask.setVisibility(View.GONE);
        mPlayerArea.removeView(mPlayerLoadingMask);
    }
}
