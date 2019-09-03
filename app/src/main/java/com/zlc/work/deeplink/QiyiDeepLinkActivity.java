package com.zlc.work.deeplink;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zlc.work.R;
import com.zlc.work.util.ClipboardUtil;
import com.zlc.work.util.OrientationUtil;
import com.zlc.work.util.ToastCompat;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author: liuchun
 * date: 2018/10/12
 */
public class QiyiDeepLinkActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {

    private static final String KEY_POSITION = "spinner_position";

    @BindView(R.id.link_desc) TextView mLinkDesc;
    @BindView(R.id.link_list) Spinner mLinkSpinner;

    @BindView(R.id.param_ftype) EditText mFtypeInput;
    @BindView(R.id.param_subtype) EditText mSubtypeInput;
    @BindView(R.id.param_aid) EditText mAidInput;
    @BindView(R.id.param_tvid) EditText mTvidInput;
    @BindView(R.id.param_url) EditText mUrlInput;

    @BindView(R.id.param_aid_group) LinearLayout mAidGroup;
    @BindView(R.id.param_tvid_group) LinearLayout mTvidGroup;
    @BindView(R.id.param_url_group) LinearLayout mUrlGroup;

    @BindView(R.id.deeplink_input_result) EditText mLinkOutput;

    @BindView(R.id.send_intent) Button mSendAction;

    private final List<Pair<String, String>> mUrlData = new ArrayList<>();

    private static final String TEST_LINK = "iqiyi://mobile/player?aid=240687701&tvid=2473559100&sid=oTfzI10C&package=cn.quicktv.androidpro&deeplink=jrysdq%3a%2f%2f&ftype=27&subtype=jrysdq_976";

    private int mPosition = 0;
    private Uri mFinalUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qiyi_deeplink);
        ButterKnife.bind(this);
        OrientationUtil.requestScreenOrientation(this, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        initData(savedInstanceState);
        findViews();
    }

    private void initData(Bundle savedInstanceState) {
        mUrlData.add(new Pair<String, String>("播放页", "iqiyi://mobile/player"));
        mUrlData.add(new Pair<String, String>("首页", "iqiyi://mobile/home"));
        mUrlData.add(new Pair<String, String>("欢迎页", "iqiyi://mobile/launcher"));
        mUrlData.add(new Pair<String, String>("Webview", "iqiyi://mobile/webview"));
        mUrlData.add(new Pair<String, String>("乐活", "iqiyi://mobile/lehas"));
        mUrlData.add(new Pair<String, String>("VIP页面", "iqiyi://mobile/vip"));
        mUrlData.add(new Pair<String, String>("我的", "iqiyi://mobile/mine"));
        mUrlData.add(new Pair<String, String>("频道页", "iqiyi://mobile/channel"));
        mUrlData.add(new Pair<String, String>("登录页", "iqiyi://mobile/login"));
        mUrlData.add(new Pair<String, String>("搜索页", "iqiyi://mobile/search"));
        mUrlData.add(new Pair<String, String>("图搜页", "iqiyi://mobile/searchimg"));
        mUrlData.add(new Pair<String, String>("离线下载", "iqiyi://mobile/download"));
        mUrlData.add(new Pair<String, String>("扫一扫", "iqiyi://mobile/scan"));
        mUrlData.add(new Pair<String, String>("播放记录", "iqiyi://mobile/playrecord"));

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(KEY_POSITION, 0);
        }
    }

    private void findViews() {
        List<String> urls = new ArrayList<>();
        for (Pair<String, String> pair : mUrlData) {
            urls.add(pair.second);
        }
        SpinnerAdapter adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, urls);
        mLinkSpinner.setAdapter(adapter);
        mLinkSpinner.setSelection(mPosition);
        mLinkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mLinkSpinner.setSelection(mPosition);
                refresh();
            }
        });

        mFtypeInput.setText("27");
        mAidInput.setText("991049000");
        mTvidInput.setText("991049000");
        mUrlInput.setText("http://m.iqiyi.com/");

        mSendAction.setOnClickListener(this);
    }

    private void refresh() {
        Pair<String, String> pair = mUrlData.get(mPosition);
        mLinkDesc.setText(pair.first);

        String uri = pair.second;
        if (!TextUtils.isEmpty(uri) && uri.startsWith("iqiyi://mobile/player")) {
            //播放页面
            mAidGroup.setVisibility(View.VISIBLE);
            mTvidGroup.setVisibility(View.VISIBLE);
        } else {
            mAidGroup.setVisibility(View.GONE);
            mTvidGroup.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(uri) && uri.startsWith("iqiyi://mobile/webview")) {
            //Webview页面
            mUrlGroup.setVisibility(View.VISIBLE);
        } else {
            mUrlGroup.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(uri)) {
            buildUri(uri);
        }
    }

    private void buildUri(String link) {
        String ftype = mFtypeInput.getText().toString();
        String subtype = mSubtypeInput.getText().toString();

        Uri uri = Uri.parse(link);
        Uri.Builder builder = new Uri.Builder()
                .scheme(uri.getScheme())
                .authority(uri.getAuthority())
                .path(uri.getPath());
        for (String paramKey : uri.getQueryParameterNames()) {
            builder.appendQueryParameter(paramKey, uri.getQueryParameter(paramKey));
        }

        if (link.startsWith("iqiyi://mobile/player")) {
            String aid = mAidInput.getText().toString();
            String tvid = mTvidInput.getText().toString();
            builder.appendQueryParameter("aid", aid)
                    .appendQueryParameter("tvid", tvid);
        }

        if (link.startsWith("iqiyi://mobile/webview")) {
            String url = mUrlInput.getText().toString();
            builder.appendQueryParameter("url", url);
        }

        builder.appendQueryParameter("ftype", ftype);
        builder.appendQueryParameter("subtype", subtype);

        mFinalUri = builder.build();
        mLinkOutput.setText(mFinalUri.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_intent:
                jumpWithLink();
                break;
            case R.id.save_link:
                saveLinkToClipboard();
                break;
            default:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        refresh();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }


    private void jumpWithLink() {
        if (mFinalUri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(mFinalUri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PackageManager pm = getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            startActivity(intent);
            ToastCompat.makeText(this, "链接跳转成功", Toast.LENGTH_SHORT).show();
        } else {
            ToastCompat.makeText(this, "没有页面可以响应该链接", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLinkToClipboard() {
        if (mFinalUri == null) {
            return;
        }
        ClipboardUtil.copyUri(this, mFinalUri);
    }
}
