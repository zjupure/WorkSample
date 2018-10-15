package com.zlc.work.deeplink;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zlc.work.R;
import com.zlc.work.util.ClipboardUtil;
import com.zlc.work.util.ToastCompat;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author: liuchun
 * date: 2018/10/12
 */
public class QiyiH5TokenActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {
    private static final String TAG = "QiyiH5TokenActivity";

    private static final String DEFAULT_PLAYER_LINK = "iqiyi://mobile/player?aid=991049000&tvid=991049000";
    public static final String LINK_KEY = "universal_link";
    public static final String REGISTER_PARAM = "register_param";

    @BindView(R.id.link_url) EditText linkInput;
    @BindView(R.id.register_params) EditText registerInput;
    @BindView(R.id.timestamp) EditText timeInput;
    @BindView(R.id.pop_message) EditText messageInput;

    @BindView(R.id.h5_token_json) TextView jsonOutput;
    @BindView(R.id.h5_token_data) TextView dataOutput;

    @BindView(R.id.save_data) Button saveOutput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qiyi_h5_token);
        ButterKnife.bind(this);

        findViews();
    }

    private void findViews() {
        linkInput.addTextChangedListener(this);
        registerInput.addTextChangedListener(this);
        messageInput.addTextChangedListener(this);

        saveOutput.setOnClickListener(this);

        Intent intent = getIntent();
        String link = intent.getStringExtra(LINK_KEY);
        String message = "";
        if (TextUtils.isEmpty(link)) {
            link = DEFAULT_PLAYER_LINK;
            message = "播放一出好戏";
        }
        linkInput.setText(link);
        messageInput.setText(message);

        String params = intent.getStringExtra(REGISTER_PARAM);
        if (!TextUtils.isEmpty(params)) {
            registerInput.setText(params);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_data:
                saveDataToClipboard();
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
        String link = linkInput.getText().toString();
        String register_param = registerInput.getText().toString();
        String message = messageInput.getText().toString();
        long timestamp = System.currentTimeMillis();

        timeInput.setText(String.valueOf(timestamp));

        String json = makeJson(link, register_param, timestamp, message);
        jsonOutput.setText(json);

        String data = encryptData(json);
        dataOutput.setText(data);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }


    private void saveDataToClipboard() {
        String data = dataOutput.getText().toString();
        if (TextUtils.isEmpty(data)) {
            ToastCompat.makeText(this, "请配置有效的H5 Token指令", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(TAG, "data write to clipboard: " + data);
        ClipboardUtil.copyText(this, data);
        ToastCompat.makeText(this, "保存到剪切板成功", Toast.LENGTH_SHORT).show();

        String read = tryReadDataFromClipborad();
        if (TextUtils.equals(data, read)) {
            Log.i(TAG, "write data are consistency with read data from clipboard");
        } else {
            Log.w(TAG, "write data not consistency with read data from clipboard, something wrong");
        }
    }


    private String tryReadDataFromClipborad() {
        String data = ClipboardUtil.getText(this);
        if (!TextUtils.isEmpty(data)) {
            Log.i(TAG, "data read from clipboard: " + data);
        }
        return data;
    }

    private String makeJson(String link, String register_param, long timestamp, String message) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("universal_url", link);
            jObj.put("register_params", register_param);
            jObj.put("timestamp", timestamp);
            jObj.put("pop_message", message);
            return jObj.toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObj.toString();
    }

    /**
     * 加密数据
     * 1. 每个byte字节-1,
     * 2. 做Base64（NO_WRAP)编码
     */
    private String encryptData(String data) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            builder.append((char)(data.charAt(i) - 1));
        }
        String result = builder.toString();
        byte[] bytes = Base64.encode(result.getBytes(), Base64.NO_WRAP);
        return new String(bytes);
    }
}
