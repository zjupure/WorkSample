package com.zlc.work.autoinstall;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlc.work.R;
import com.zlc.work.util.OemInstallUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * author: liuchun
 * date: 2018/11/2
 */
public class ApkRecyclerAdapter extends RecyclerView.Adapter<ApkRecyclerAdapter.ApkViewHolder> {

    private List<ApkItem> mApkItems;
    private Context mContext;

    public ApkRecyclerAdapter(Context context) {
        mContext = context;
        mApkItems = new ArrayList<>();
    }

    public void setData(List<ApkItem> items) {
        mApkItems.clear();
        mApkItems.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ApkViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.apk_info_item, viewGroup, false);
        return new ApkViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ApkViewHolder apkViewHolder, int i) {
        final ApkItem item = mApkItems.get(i);

        apkViewHolder.mAppIcon.setImageDrawable(item.appIcon);
        apkViewHolder.mAppName.setText(item.appName);
        apkViewHolder.mAppPkgName.setText(item.appPkgName);
        apkViewHolder.mAppPath.setText(item.apkPath);
        apkViewHolder.mInstallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OemInstallUtil.installApkFile(mContext, new File(item.apkPath));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mApkItems != null ? mApkItems.size() : 0;
    }

    static class ApkViewHolder extends RecyclerView.ViewHolder {
        ImageView mAppIcon;
        TextView mAppName;
        TextView mAppPkgName;
        TextView mAppPath;
        Button mInstallBtn;

        ApkViewHolder(View itemView) {
            super(itemView);

            mAppIcon = itemView.findViewById(R.id.app_icon);
            mAppName = itemView.findViewById(R.id.app_name);
            mAppPkgName = itemView.findViewById(R.id.app_pkgName);
            mAppPath = itemView.findViewById(R.id.apk_path);
            mInstallBtn = itemView.findViewById(R.id.install_apk);
        }
    }
}
