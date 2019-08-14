package com.zlc.work.viewpager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.zlc.work.R;

/**
 * author: liuchun
 * date: 2019/8/8
 */
public class ViewPagerActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;

    private Fragment mContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_viewpager);
//        initViews();
        addFragment();
    }

    private void addFragment() {
        mContainer = new ContainerFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(android.R.id.content, mContainer, "container")
                .commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction()
                        .hide(mContainer)
                        .commitAllowingStateLoss();
            }
        });
    }

    private void initViews() {
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout.setupWithViewPager(mViewPager);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setCurrentItem(1);
    }
}
