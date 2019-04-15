package com.zlc.work.image;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.zlc.work.R;
import com.zlc.work.util.IoUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * author: liuchun
 * date: 2019/6/10
 */
public class FrescoActivity extends AppCompatActivity {

    private static final String[] URL_LINKS = {
            "http://desk.fd.zol-img.com.cn/t_s1440x900c5/g5/M00/0F/09/ChMkJlauzbOIb6JqABF4o12gc_AAAH9HgF1sh0AEXi7441.jpg",
            "http://desk.fd.zol-img.com.cn/t_s1440x900c5/g5/M00/08/0A/ChMkJli9XIOIHZlxACrBWTH-3-kAAae8QCVIF4AKsFx521.jpg",
            "http://desk.fd.zol-img.com.cn/t_s1440x900c5/g5/M00/08/0A/ChMkJ1i9XJmIJnFtABXosJGWaOkAAae8QGrHE8AFejI057.jpg",
            "http://desk.fd.zol-img.com.cn/t_s1440x900c5/g5/M00/08/0A/ChMkJ1i9XIWIfPrHACa8wnLl-YYAAae8QDvfUkAJrza322.jpg",
            "http://desk.fd.zol-img.com.cn/t_s1440x900c5/g5/M00/08/0A/ChMkJli9XJuIGhrMADspbh_OzE4AAae8QHCouwAOymG127.jpg",
    };

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fresco.initialize(this);
        FLog.setMinimumLoggingLevel(FLog.VERBOSE);

        setContentView(R.layout.activity_fresco);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        List<String> mImageUrls = Arrays.asList(URL_LINKS);
        mAdapter = new ImageAdapter(this, mImageUrls);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }
}
