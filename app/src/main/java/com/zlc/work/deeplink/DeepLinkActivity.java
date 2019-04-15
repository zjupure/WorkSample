package com.zlc.work.deeplink;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zlc.work.R;
import com.zlc.work.util.ClipboardUtil;
import com.zlc.work.util.ToastCompat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author: liuchun
 * date: 2018/10/12
 */
public class DeepLinkActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener{

    @BindView(R.id.url_scheme) EditText schemeInput;
    @BindView(R.id.url_host) EditText hostInput;
    @BindView(R.id.url_path) EditText pathInput;
    @BindView(R.id.url_params) EditText paramsInput;

    @BindView(R.id.deeplink_input_result) EditText resultInput;

    @BindView(R.id.send_intent) Button sendIntent;
    @BindView(R.id.save_link) Button saveLink;

    private static final String GAME_URL = "iqiyi://mobile/register_business/game?pluginParams=%257B%2522biz_params%2522%253A%257B%2522biz_params%2522%253A%2522%2522%252C%2522biz_statistics%2522%253A%2522block%253DbizDemo%2526partner%253DbizDemo%2526p1%253D%2522%252C%2522biz_extend_params%2522%253A%2522%257B%25CE%25B2qipu_id%25CE%25B2%253A%25CE%25B2209523020%25CE%25B2%257D%2522%252C%2522biz_sub_id%2522%253A%25221%2522%257D%257D%250A";

    private Uri mUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deeplink);
        ButterKnife.bind(this);

        findViews();
    }

    private void findViews() {
        schemeInput.addTextChangedListener(this);
        hostInput.addTextChangedListener(this);
        pathInput.addTextChangedListener(this);
        paramsInput.addTextChangedListener(this);

        resultInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String uri = resultInput.getText().toString();

                if (!TextUtils.isEmpty(uri)) {
                    mUri = Uri.parse(uri);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        resultInput.setText(GAME_URL);

        sendIntent.setOnClickListener(this);
        saveLink.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deeplink_guide_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.iqiyi_deepplink:
                intent.setClass(this, QiyiDeepLinkActivity.class);
                break;
            case R.id.iqiyi_register_biz:
                intent.setClass(this, QiyiRegisterBizActivity.class);
                break;
            case R.id.iqiyi_h5_token:
                intent.setClass(this, QiyiH5TokenActivity.class);
                break;
            default:
                break;
        }

        PackageManager pm = getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        String scheme = schemeInput.getText().toString();
        String host = hostInput.getText().toString();
        String path = pathInput.getText().toString();
        String params = paramsInput.getText().toString();

        Uri.Builder builder = new Uri.Builder()
                .scheme(scheme)
                .authority(host)
                .path(path)
                .encodedQuery(params);
        mUri = builder.build();
        resultInput.setText(mUri.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private boolean checkUriValid() {
        if (mUri == null) {
            ToastCompat.makeText(this, "请填写url配置", Toast.LENGTH_SHORT).show();
            return false;
        }
        // sheme和host必填
        String scheme = mUri.getScheme();
        String host = mUri.getHost();

        if (TextUtils.isEmpty(scheme) || TextUtils.isEmpty(host)) {
            ToastCompat.makeText(this, "scheme和host必现填写，不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void jumpWithLink() {
        if (!checkUriValid()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(mUri);
        PackageManager pm = getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            startActivity(intent);
            ToastCompat.makeText(this, "链接跳转成功", Toast.LENGTH_SHORT).show();
        } else {
            ToastCompat.makeText(this, "没有页面可以响应该链接", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLinkToClipboard() {
        if (!checkUriValid()) {
            return;
        }
        ClipboardUtil.copyUri(this, mUri);
    }
}
