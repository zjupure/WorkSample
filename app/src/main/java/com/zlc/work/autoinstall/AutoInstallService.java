package com.zlc.work.autoinstall;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * author: liuchun
 * date: 2018/10/16
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AutoInstallService extends AccessibilityService {
    private static final String TAG = "AutoInstallService";
    private static final int DELAY_PAGE = 320;

    private final Handler mHandler = new Handler();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "onServiceConnected() called, autoInstall is enabled");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        String pkgName = event.getPackageName().toString();
        if (TextUtils.equals(pkgName, "com.vivo.secime.service")) {
            performAutoFillPassword(event);
        }
        // 各种系统的安装器界面
        if (pkgName.contains("packageinstaller")) {
            performAutoInstall(event);
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() called, autoInstall is disabled");
    }

    /**
     * Vivo X21自动填充账号密码，自动安装apk问题
     */
    private void performAutoFillPassword(AccessibilityEvent event) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        //vivo账号密码
        String password = "12345678";
        fillPassword(rootNode, password);

        findButtonTextAndClick(rootNode, "确定");
    }

    private void fillPassword(AccessibilityNodeInfo rootNode, String password) {
        AccessibilityNodeInfo editText = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (editText == null) return;

        if (editText.getPackageName().equals("com.bbk.account")
                && editText.getClassName().equals("android.widget.EditText")) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo
                    .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, password);
            editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
    }

    /**
     * 自动点击安装apk文件
     */
    private void performAutoInstall(AccessibilityEvent event) {
        AccessibilityNodeInfo eventNode = event.getSource();
        if (eventNode == null) {
            performGlobalAction(GLOBAL_ACTION_RECENTS); //打开最近页面
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //返回安装页面
                    performGlobalAction(GLOBAL_ACTION_BACK);
                }
            }, DELAY_PAGE);
            return;
        }

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        findCheckBoxTextAndClick(rootNode, "安装后删除安装包");  // vivo
        findCheckBoxTextAndClick(rootNode, "继续安装", true);  // 华为

        findButtonTextAndClick(rootNode, "允许全部安装");
        findButtonTextAndClick(rootNode, "仅允许本次安装");
        if (isNotAD(rootNode)) {
            findButtonTextAndClick(rootNode, "安装");
        }
        findButtonTextAndClick(rootNode, "继续安装", true); // vivo
        findButtonTextAndClick(rootNode, "下一步");
        findButtonTextAndClick(rootNode, "打开");  // 直接启动已安装应用
        //findButtonTextAndClick(rootNode, "确定");
        //findButtonTextAndClick(rootNode, "完成");
        // 回收节点
        eventNode.recycle();
        rootNode.recycle();
    }

    // 查找带文本的CheckBox，并且模拟点击
    private void findCheckBoxTextAndClick(AccessibilityNodeInfo rootNode, String text) {
        findCheckBoxTextAndClick(rootNode, text, false);
    }

    // 查找带文本的CheckBox，并且模拟点击
    private void findCheckBoxTextAndClick(AccessibilityNodeInfo rootNode, String text, boolean fuzzyMatch) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(text);
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        // 华为会出现 <我已充分了解该风险，继续安装> 复选框，默认未勾选
        // vivo会出现 <安装后删除安装包> 复选框，默认已勾选
        Log.i(TAG, "find related view with text: " + text + ", size=" + nodes.size());
        for (AccessibilityNodeInfo node : nodes) {
            if (node.isEnabled() && node.isCheckable() && node.isClickable()
                    && node.getClassName().equals("android.widget.CheckBox")) {
                if (fuzzyMatch || TextUtils.equals(text, node.getText())) {
                    Log.i(TAG, "find target view and perform click with text: " + text);
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    // 查找带文本的按钮，并且模拟点击
    private void findButtonTextAndClick(AccessibilityNodeInfo rootNode, String text) {
        findButtonTextAndClick(rootNode, text, false);
    }

    // 查找带文本的按钮，并且模拟点击
    private void findButtonTextAndClick(AccessibilityNodeInfo rootNode, String text, boolean fuzzyMatch) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(text);
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        Log.i(TAG, "find related view with text: " + text + ", size=" + nodes.size());
        for (AccessibilityNodeInfo node : nodes) {
            if (node.isEnabled() && node.isClickable()
                    && (node.getClassName().equals("android.widget.Button")
                    || node.getClassName().equals("android.widget.TextView"))) {
                // vivo会出现 <安全安装> <继续安装> <继续安装旧版本>
                if (fuzzyMatch || TextUtils.equals(text, node.getText())) {
                    Log.i(TAG, "find target view and perform click with text: " + text);
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    // 广告引导安装界面
    private boolean isNotAD(AccessibilityNodeInfo rootNode) {
        return isNotFound(rootNode, "还喜欢")  // 小米
                && isNotFound(rootNode, "官方安装");  //华为
    }

    private boolean isNotFound(AccessibilityNodeInfo rootNode, String text) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(text);
        return nodes == null || nodes.isEmpty();
    }
}
