package com.zlc.work.autoinstall;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zlc.work.R;
import com.zlc.work.util.IoUtil;
import com.zlc.work.util.SettingsUtil;
import com.zlc.work.util.ToastCompat;

import java.io.File;
import java.io.FileFilter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author: liuchun
 * date: 2018/10/16
 */
public class AutoInstallActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQ_STORAGE = 100;

    @BindView(R.id.vivo_account) EditText vivoAccountInput;
    @BindView(R.id.vivo_password) EditText vivoPwdInput;
    @BindView(R.id.vivo_group) LinearLayout vivoGroup;

    @BindView(R.id.jump_setting) Button jumpSettings;
    @BindView(R.id.install_apk) Button installApk;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility);
        ButterKnife.bind(this);

        findViews();
    }

    private void findViews() {
        String model = Build.MODEL;
        if ("vivo X21".equalsIgnoreCase(model)) {
            vivoGroup.setVisibility(View.VISIBLE);
        } else {
            vivoGroup.setVisibility(View.GONE);
        }

        jumpSettings.setOnClickListener(this);
        installApk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.jump_setting:
                jumpToSettingsIfNeed();
                break;
            case R.id.install_apk:
                installApk();
                break;
            default:break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            installApk();
        }
    }

    private void installApk() {
        if (jumpToSettingsIfNeed()) {
            return;
        }
        // 开启了自动安装辅助功能, 准备安装apk
        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_STORAGE);
            return;
        }

        File extDir = Environment.getExternalStorageDirectory();
        if (extDir != null && extDir.exists() && extDir.canRead()) {
            File[] files = extDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".apk");
                }
            });

            if (files != null && files.length > 0) {
                File apkFile = files[0];
                installApkIfNeed(apkFile);
            }
        }
    }

    private void installApkIfNeed(File apkFile) {
        PackageManager pm = getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
        String pkgName = pi.packageName;
        long versionCode = pi.versionCode;

        PackageInfo prePi = null;
        try {
            prePi = pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (prePi == null) {
            // 未安装过
            IoUtil.installApkFile(this, apkFile);
        } else if (prePi.versionCode < versionCode) {
            // 安装的是低版本的apk, 覆盖安装
            IoUtil.installApkFile(this, apkFile);
        } else {
            String name = prePi.applicationInfo.loadLabel(pm).toString();
            ToastCompat.makeText(this, name + "已经安装过了", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean jumpToSettingsIfNeed() {
        if (!SettingsUtil.isUnknownInstallAllowed(this)) {
            ToastCompat.makeText(this, "跳转设置页面，请打开允许安装未知来源选项", Toast.LENGTH_SHORT).show();
            SettingsUtil.jumpToSettingSecure(this);
            return true;
        }

        if (!SettingsUtil.isAccessibilityServiceEnable(this, AutoInstallService.class)) {
            ToastCompat.makeText(this, "跳转设置页面，请打开自动安装辅助功能", Toast.LENGTH_SHORT).show();
            SettingsUtil.jumpToSettingAccessibility(this);
            return true;
        }
        return false;
    }
}
