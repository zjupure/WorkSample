package com.zlc.work.webview;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zlc.work.R;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * author: liuchun
 * date: 2018/11/1
 */
public class WebviewActivity extends AppCompatActivity {
    private static final String TAG = "WebviewActivity";
    private static final String URL = "https://www.runoob.com/try/try.php?filename=tryhtml_select";

    private WebView mWebview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        mWebview = findViewById(R.id.webview);
        WebSettings settings = mWebview.getSettings();
        settings.setSupportZoom(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        mWebview.setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                Log.i(TAG, "load url: " + url);
                return super.shouldInterceptRequest(view, url);
            }
        });

        mWebview.loadUrl(URL);

        getWebviewAssetPath();
    }

    @Override
    protected void onDestroy() {
        if (mWebview != null) {
            mWebview.loadUrl(null);
            mWebview.clearHistory();
            mWebview.destroy();
            mWebview = null;
        }
        super.onDestroy();
    }


    private void getWebviewAssetPath() {
        // Android L上WebViewFactory才存在getLoadedPackageInfo()方法
        // see http://androidxref.com/5.0.0_r2/xref/frameworks/base/core/java/android/webkit/WebViewFactory.java
        try {
            // 初始化WebviewProvider
            Class<?> webviewFactory = Class.forName("android.webkit.WebViewFactory");
            Method getProvider = webviewFactory.getDeclaredMethod("getProvider");
            getProvider.setAccessible(true);
            getProvider.invoke(null);

            Method getPackageInfo = webviewFactory.getDeclaredMethod("getLoadedPackageInfo");
            getPackageInfo.setAccessible(true);
            // 获取Webview资源路径
            PackageInfo pi = (PackageInfo) getPackageInfo.invoke(null);
            if (pi != null && pi.applicationInfo != null && !TextUtils.isEmpty(pi.applicationInfo.sourceDir)) {
                String path = pi.applicationInfo.sourceDir;
                Log.i(TAG, "getWebviewPath from WebViewFactory: " + path);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            // com.internal.R.string.config_webViewPackageName 读取Webview浏览器内核包名
//            Class<?> RStr = Class.forName("com.android.internal.R$string");
//            Field pkgFd = RStr.getDeclaredField("config_webViewPackageName");
//            pkgFd.setAccessible(true);
//            String webPkgName = (String)pkgFd.get(null);
            Resources resources = this.getResources();
            int webPkgNameId = resources.getIdentifier("config_webViewPackageName", "string", "android");
            String webPkgName;
            if (webPkgNameId > 0) {
                webPkgName = resources.getString(webPkgNameId);
            } else {
                webPkgName = Settings.Global.getString(getContentResolver(), "webview_provider");
            }
            PackageManager pm = this.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(webPkgName, PackageManager.GET_ACTIVITIES);
            if (pi != null && pi.applicationInfo != null && !TextUtils.isEmpty(pi.applicationInfo.sourceDir)) {
                String path = pi.applicationInfo.sourceDir;
                Log.i(TAG, "getWebviewPath from R.string.config_webViewPackageName: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 硬编码WebView的路径
        File hardCode = new File("/system/app/WebViewGoogle/WebViewGoogle.apk");
        if (hardCode.exists()) {
            Log.i(TAG, "getWebviewPath hard code: " + hardCode.getAbsolutePath());
        }
        Log.i(TAG, "getWebviewPath null");
    }
}
