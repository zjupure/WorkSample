package com.zlc.work.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * author: liuchun
 * date: 2018/10/12
 *
 * 读写系统剪切板数据
 */
public class ClipboardUtil {

    public static String getText(Context context) {
        ClipboardManager cm = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData data = cm.getPrimaryClip();
            if (data != null && data.getItemCount() >= 0 && data.getItemAt(0) != null) {
                ClipData.Item item = data.getItemAt(0);
                CharSequence cs = item.coerceToText(context);
                return cs.toString();
            }
        }
        return "";
    }

    public static void copyText(Context context, CharSequence text) {
        ClipboardManager cm = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData data = ClipData.newPlainText("text", text);
            cm.setPrimaryClip(data);
        }
    }

    public static Uri getUri(Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData data = cm.getPrimaryClip();
            if (data != null && data.getItemCount() >= 0 && data.getItemAt(0) != null) {
                ClipData.Item item = data.getItemAt(0);
                return item.getUri();
            }
        }
        return null;
    }

    public static void copyUri(Context context, Uri uri) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData data = ClipData.newUri(context.getContentResolver(), "uri", uri);
            cm.setPrimaryClip(data);
        }
    }

    public static Intent getIntent(Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData data = cm.getPrimaryClip();
            if (data != null && data.getItemCount() >= 0 && data.getItemAt(0) != null) {
                ClipData.Item item = data.getItemAt(0);
                return item.getIntent();
            }
        }
        return null;
    }

    public static void copyIntent(Context context, Intent intent) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData data = ClipData.newIntent("intent", intent);
            cm.setPrimaryClip(data);
        }
    }
}
