package com.zlc.work.viewpager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * author: liuchun
 * date: 2019/8/8
 */
public class ViewPagerFragment extends Fragment {

    private String content = "";
    private int position = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView textView = new TextView(container.getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setLayoutParams(layoutParams);
        textView.setGravity(Gravity.CENTER);
        initData();
        textView.setText(content);
        return textView;
    }

    private void initData() {
        Bundle args = getArguments();
        if (args != null) {
            content = args.getString("content");
            position = args.getInt("position");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("ViewPagerFragment", "position: " + position + ", tab title: " + content + ", isHidden: " + isHidden());
    }
}
