package com.zlc.work.opengl;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * author: liuchun
 * date: 2020/1/17
 */
public class OpenGLActivity extends AppCompatActivity {

    private GLSurfaceView glView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glView = new VideoGLSurfaceView(this);
        setContentView(glView);
    }
}
