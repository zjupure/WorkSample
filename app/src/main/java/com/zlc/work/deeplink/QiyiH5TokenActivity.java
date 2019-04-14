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
import com.zlc.work.util.Md5Util;
import com.zlc.work.util.ToastCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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

    private static final String ND = "{\"universal_url\":\"iqiyi://mobile/register_business/mall?pluginParams=%7b%22biz_id%22%3a%221%22%2c%22biz_plugin%22%3a%22com.iqiyi.imall%22%2c%22biz_params%22%3a%7b%22biz_sub_id%22%3a%222%22%2c%22biz_params%22%3a%22%22%2c%22biz_dynamic_params%22%3a%22http%3a%2f%2fmall.iqiyi.com%2fkszt%2fwelfareYoung180703.html%3fodfrm%3dVIPIP%22%2c%22biz_extend_params%22%3a%22%22%2c%22biz_statistics%22%3a%22%22%7d%7d\",\"timestamp\":1539590904195,\"pop_message\":\"弹窗文案\"}";
    private static final String MD = "eiF0bWh1ZHFyYGtedHFrITkhaHBoeGg5Li5sbmFoa2QucWRmaHJzZHFeYXRyaG1kcnIubGBraz5va3RmaG1PYHFgbHI8JDE0NmEkMTQxMWFoeV5oYyQxNDExJDE0MmAkMTQxMTAkMTQxMSQxNDFiJDE0MTFhaHleb2t0ZmhtJDE0MTEkMTQyYCQxNDExYm5sLWhwaHhoLWhsYGtrJDE0MTEkMTQxYiQxNDExYWh5Xm9gcWBsciQxNDExJDE0MmAkMTQ2YSQxNDExYWh5XnJ0YV5oYyQxNDExJDE0MmAkMTQxMTEkMTQxMSQxNDFiJDE0MTFhaHleb2BxYGxyJDE0MTEkMTQyYCQxNDExJDE0MTEkMTQxYiQxNDExYWh5XmN4bWBsaGJeb2BxYGxyJDE0MTEkMTQyYCQxNDExZ3NzbyQxNDJgJDE0MWUkMTQxZWxga2staHBoeGgtYm5sJDE0MWVqcnlzJDE0MWV2ZGtlYHFkWG50bWYwNy82LzItZ3NsayQxNDJlbmNlcWwkMTQyY1VIT0hPJDE0MTEkMTQxYiQxNDExYWh5XmR3c2RtY15vYHFgbHIkMTQxMSQxNDJgJDE0MTEkMTQxMSQxNDFiJDE0MTFhaHlecnNgc2hyc2hiciQxNDExJDE0MmAkMTQxMSQxNDExJDE0NmMkMTQ2YyErIXNobGRyc2BsbyE5MDQyODU0NS8zNTM2Mishb25vXmxkcnJgZmQhOSEkRDQkQUIkQTgkRDYkQEAkODYkRDUkODUkNzYkRDUkQDAkNzchfA==";


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

        test();
    }

    private void test() {
//        String data = encryptData(ND);
//        Log.i(TAG, "encryptedData: " + data);
//
//        Log.i(TAG, "decyptedData: " + decryptData(data));

        String data = decryptData(MD);
        Log.i(TAG, "decryptedData: " + data);
        try {
            JSONObject obj = new JSONObject(data);
            String message = obj.optString("pop_message");
            Log.i(TAG, "message: " + message);

            String oMsg = URLDecoder.decode(message, "UTF-8");
            Log.i(TAG, "original message: " + oMsg);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    private static String encryptData(String data) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            builder.append((char)(data.charAt(i) - 1));
        }
        String result = builder.toString();
        byte[] bytes = Base64.encode(result.getBytes(), Base64.NO_WRAP);
        return new String(bytes);
    }

    /**
     * 解密数据
     * 1. 做Base64（NO_WRAP)解码,
     * 2. 每个byte字节+1
     */
    private static String decryptData(String data) {
        byte[] bytes = Base64.decode(data.getBytes(), Base64.NO_WRAP);

        String decodedString = new String(bytes);
        StringBuilder builder = new StringBuilder();
        // 解密数据
        for (int i = 0; i < decodedString.length(); i++) {
            builder.append((char)(decodedString.charAt(i) + 1));
        }

        return builder.toString();
    }
}
