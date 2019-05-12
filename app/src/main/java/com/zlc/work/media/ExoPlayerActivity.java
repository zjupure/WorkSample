package com.zlc.work.media;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.zlc.work.R;

/**
 * author: liuchun
 * date: 2019-05-12
 */
public class ExoPlayerActivity extends AppCompatActivity {
    private static final String TAG = "ExoPlayerActivity";

    private static final String VIDEO_URL = "http://wxsnsdy.tc.qq.com/105/20210/snsdyvideodownload?filekey=30280201010421301f0201690402534804102ca905ce620b1241b726bc41dcff44e00204012882540400&bizid=1023&hy=SH&fileparam=302c020101042530230204136ffd93020457e3c4ff02024ef202031e8d7f02030f42400204045a320a0201000400";


    private PlayerView mPlayerView;
    private SimpleExoPlayer mExoPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer);
        initExoPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PlayerTool.setupImmersive(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExoPlayer != null) {
            mExoPlayer.release();
        }
    }

    private Player.EventListener mPlayerListener = new Player.DefaultEventListener() {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playWhenReady && playbackState == Player.STATE_READY) {
                // Active playback
                mExoPlayer.setPlayWhenReady(true);
            } else if (playWhenReady) {
                // Not playing because playback ended, the player is buffering, stopped or failed
            } else {
                // Paused by app
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.i(TAG, "onPlayerError, error: " + error.type);
        }
    };


    private void initExoPlayer() {
        BandwidthMeter meter = new DefaultBandwidthMeter();
        TrackSelection.Factory factory = new AdaptiveTrackSelection.Factory(meter);
        TrackSelector selector = new DefaultTrackSelector(factory);
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, selector);

        mPlayerView = findViewById(R.id.player_view);
        mPlayerView.setPlayer(mExoPlayer);

        DataSource.Factory dataFactory = new DefaultDataSourceFactory(this, "exoPlayerActivity");
        MediaSource videoSource = new ExtractorMediaSource.Factory(dataFactory)
                .createMediaSource(Uri.parse(VIDEO_URL));
        mExoPlayer.prepare(videoSource);
        //mExoPlayer.setPlayWhenReady(true);
        mExoPlayer.addListener(mPlayerListener);
    }
}


















