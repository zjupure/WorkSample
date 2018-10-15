package com.zlc.work.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 * author: liuchun
 * date: 2017/12/12
 *
 * A workaround Toast to prevent android.view.WindowManager$BadTokenException crash in Nougat-Mr1 when app targetSdkVersion > 25
 * by replace the Handler in Toast$TN and add try-catch protect
 * {@see <href>https://android.googlesource.com/platform/frameworks/base/+/dc24f93%5E%21/#F4</href>}
 * {@see <href>https://android.googlesource.com/platform/frameworks/base/+/0df3702f533667a3825ecbce67db0853385a99ab%5E%21/#F0</href>} for detail
 *
 */
public class ToastCompat extends Toast {
    private static final String TAG = "ToastCompat";

    private static Field sTnField;
    private static Field sTnHandlerField;

    public ToastCompat(Context context) {
        super(context);

        ApplicationInfo appInfo = context.getApplicationInfo();
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1
                && (appInfo != null && appInfo.targetSdkVersion >= Build.VERSION_CODES.O)) {
            // when targetSdkVersion >= 28, Android 7.1 system Toast may crash
            fixNougatMr1();
        }
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast result = new ToastCompat(context);

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Resources resources = context.getResources();
        View v = inflater.inflate(resources.getIdentifier("transient_notification", "layout", "android")
                /*com.android.internal.R.layout.transient_notification*/, null);
        TextView tv = (TextView)v.findViewById(resources.getIdentifier("message", "id", "android")
                /*com.android.internal.R.id.message*/);
        tv.setText(text);
        result.setView(v);
        result.setDuration(duration);

        return result;
    }

    public static Toast makeText(Context context, @StringRes int resId, int duration)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }

    /**
     * 反射替换Toast$TN的Handler
     */
    private void fixNougatMr1() {
        try {
            if (sTnField == null) {
                sTnField = Toast.class.getDeclaredField("mTN");
                sTnField.setAccessible(true);
            }
            // get mTN instance in Toast
            Object mTN = sTnField.get(this);

            if (mTN != null && sTnHandlerField == null) {
                sTnHandlerField = mTN.getClass().getDeclaredField("mHandler");
                sTnHandlerField.setAccessible(true);
            }
            // get mHandler member in Toast$TN
            if (sTnHandlerField != null) {
                Handler mHandler = (Handler) sTnHandlerField.get(mTN);
                // replace mHandler to our wrapped HackyHandler
                sTnHandlerField.set(mTN, new HackyHandler(mHandler));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static class HackyHandler extends Handler {
        private final Handler oriHandler;

        HackyHandler(Handler oriHandler) {
            super();
            this.oriHandler = oriHandler;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                oriHandler.handleMessage(msg);
            } catch (WindowManager.BadTokenException e) {
                e.printStackTrace();
            }
        }
    }
}
