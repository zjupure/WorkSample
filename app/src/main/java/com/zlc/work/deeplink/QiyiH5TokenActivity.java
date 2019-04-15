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
    private static final String DEFAULT_PLAYER_REG = "iqiyi://mobile/register_business/player?pluginParams=%7B%22biz_id%22%3A%22102%22%2C%22biz_params%22%3A%7B%22biz_sub_id%22%3A%22101%22%2C%22biz_params%22%3A%22aid%3D226585001%26tvid%3D2382603800%26ctype%3D0%26pc%3D0%26_frd%3DXvjm5121IVu2tF%252F1gj%252F36I%252BAmNHz7rHm4WmALqHQP80XV4R%252BpW%252F%252Bjmn1lVUdfVlTWK8C4uf33we%252BXCj%252BXRd2a59RkupOtU3hMCR6idwDOjeCxsp0pKzWJK5cZQJftDnsVC6wsUJk5ocztOPCJWHecg%253D%253D%22%2C%22biz_dynamic_params%22%3A%22%22%2C%22biz_extend_params%22%3A%22screenMode%3D1%26openType%3D%22%2C%22biz_statistics%22%3A%22%22%7D%2C%22init_type%22%3A%2227%22%2C%22init_sub_type%22%3A%22472%22%2C%22wx_schema_pid%22%3A%22player%22%7D";
    private static final String DEFAULT_ICON_LINK = "https://m.iqiyipic.com/image/20190506/85/ce/v_129111780_m_601_480_360.jpg";
    public static final String LINK_KEY = "universal_link";
    public static final String REGISTER_PARAM = "register_param";
    public static final String ICON_KEY = "icon";

    private static final String ND = "{\"universal_url\":\"iqiyi://mobile/register_business/mall?pluginParams=%7b%22biz_id%22%3a%221%22%2c%22biz_plugin%22%3a%22com.iqiyi.imall%22%2c%22biz_params%22%3a%7b%22biz_sub_id%22%3a%222%22%2c%22biz_params%22%3a%22%22%2c%22biz_dynamic_params%22%3a%22http%3a%2f%2fmall.iqiyi.com%2fkszt%2fwelfareYoung180703.html%3fodfrm%3dVIPIP%22%2c%22biz_extend_params%22%3a%22%22%2c%22biz_statistics%22%3a%22%22%7d%7d\",\"timestamp\":1539590904195,\"pop_message\":\"弹窗文案\"}";
    private static final String MD = "eiF0bWh1ZHFyYGtedHFrITkhaHBoeGg5Li5sbmFoa2QucWRmaHJzZHFeYXRyaG1kcnIub2tgeGRxPm9rdGZobU9gcWBscjwkNkEkMTFhaHleaGMkMTEkMkAkMTEwLzEkMTEkMUIkMTFhaHleb2BxYGxyJDExJDJAJDZBJDExYWh5XnJ0YV5oYyQxMSQyQCQxMTAvMCQxMSQxQiQxMWFoeV5vYHFgbHIkMTEkMkAkMTFgaGMkMkMxMTU0NzQvLzAkMTVzdWhjJDJDMTI3MTUvMjcvLyQxNWJzeG9kJDJDLyQxNW9iJDJDLyQxNV5lcWMkMkNXdWlsNDAxMEhVdDFzRSQxNDFFMGZpJDE0MUUyNUgkMTQxQUBsTUd5NnFHbDNWbEBLcEdQTzcvV1UzUSQxNDFBb1YkMTQxRSQxNDFBaWxtMGtVVGNlVWtTVko3QjN0ZTIydmQkMTQxQVdCaSQxNDFBV1FjMWA0OFFqdG9Oc1QyZ0xCUTVoY3ZDTmlkQndyby9vSnlWSUo0YllQSWVzQ21yVUI1dnJUSWo0bmJ5c05PQklWR2RiZiQxNDJDJDE0MkMkMTEkMUIkMTFhaHleY3htYGxoYl5vYHFgbHIkMTEkMkAkMTEkMTEkMUIkMTFhaHleZHdzZG1jXm9gcWBsciQxMSQyQCQxMXJicWRkbUxuY2QkMkMwJDE1bm9kbVN4b2QkMkMkMTEkMUIkMTFhaHlecnNgc2hyc2hiciQxMSQyQCQxMSQxMSQ2QyQxQiQxMWhtaHNec3hvZCQxMSQyQCQxMTE2JDExJDFCJDExaG1oc15ydGFec3hvZCQxMSQyQCQxMTM2MSQxMSQxQiQxMXZ3XnJiZ2RsYF5vaGMkMTEkMkAkMTFva2B4ZHEkMTEkNkMhKyFzaGxkcnNgbG8hOTA0NDYyODAxMTI4MTArIW9ub15sZHJyYGZkITkhJEQ2JEBDJDgwJEQ1JEAxJEA1JEQ1JDcyJDc0JEQ2JEFCJDg3JEQ2JEBCJEBCMCREOCQ4QSQ3NSErIWhibm0hOSFnc3NvcjkuLmwtaHBoeGhvaGItYm5sLmhsYGZkLjEvMDgvNC81Ljc0LmJkLnVeMDE4MDAwNjcvXmxeNS8wXjM3L14yNS8taW9mIXw=";

    @BindView(R.id.link_url) EditText linkInput;
    @BindView(R.id.register_params) EditText registerInput;
    @BindView(R.id.timestamp) EditText timeInput;
    @BindView(R.id.pop_message) EditText messageInput;
    @BindView(R.id.icon_image) EditText iconInput;

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
        iconInput.addTextChangedListener(this);

        saveOutput.setOnClickListener(this);

        Intent intent = getIntent();
        String link = intent.getStringExtra(LINK_KEY);
        String message = "筑梦情缘第1集";
//        if (TextUtils.isEmpty(link)) {
//            link = DEFAULT_PLAYER_LINK;
//            message = "播放一出好戏";
//        }

        linkInput.setText(link);
        messageInput.setText(message);

        String params = intent.getStringExtra(REGISTER_PARAM);
        if (!TextUtils.isEmpty(params)) {
            registerInput.setText(params);
        } else {
            registerInput.setText(DEFAULT_PLAYER_REG);
        }

        String icon = intent.getStringExtra(ICON_KEY);
        if (!TextUtils.isEmpty(icon)) {
            iconInput.setText(icon);
        } else {
            iconInput.setText(DEFAULT_ICON_LINK);
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
        createTokenData();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void createTokenData() {
        String link = linkInput.getText().toString();
        String register_param = registerInput.getText().toString();
        String message = messageInput.getText().toString();
        String iconLink = iconInput.getText().toString();
        long timestamp = System.currentTimeMillis();

        timeInput.setText(String.valueOf(timestamp));

        String json = makeJson(link, register_param, timestamp, message, iconLink);
        jsonOutput.setText(json);

        String data = encryptData(json);
        dataOutput.setText(data);
    }


    private void saveDataToClipboard() {
        createTokenData();
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

    private String makeJson(String link, String register_param, long timestamp, String message, String iconLink) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("universal_url", link);
            jObj.put("register_params", register_param);
            jObj.put("timestamp", timestamp);
            jObj.put("pop_message", message);
            jObj.put("icon", iconLink);
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
