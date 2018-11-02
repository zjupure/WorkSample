package com.zlc.work.webview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zlc.work.R;

/**
 * author: liuchun
 * date: 2018/11/1
 */
public class WebviewActivity extends AppCompatActivity {
    private static final String TAG = "WebviewActivity";
    private static final String URL = "http://www.runoob.com/try/try.php?filename=tryhtml_select";

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
}
