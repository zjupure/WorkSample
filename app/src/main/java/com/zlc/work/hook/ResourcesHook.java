package com.zlc.work.hook;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import com.zlc.work.reflect.Reflector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;


/**
 * author: liuchun
 * date: 2019-06-08
 */
public class ResourcesHook {

    public static void hookResources(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            // we wont hook resources for static lossless webp load
//            return;
//        }
        Resources resources = new ResourcesCompat(context, context.getResources());
        Reflector reflector = Reflector.on(context);
        reflector.set("mResources", resources);

        Object loadedApk = reflector.get("mPackageInfo");
        Reflector.on(loadedApk).set("mResources", resources);

        Map<Object, WeakReference<Resources>> mActiveResources = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 4.4以上使用ResourceManager
            Object resManager = Reflector.on("android.app.ResourcesManager").call("getInstance");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android N move to mResourceReferences
                ArrayList<WeakReference<Resources>> mResources = Reflector.on(resManager).get("mResourceReferences");
                mResources.clear();
                mResources.add(new WeakReference<Resources>(resources));
            } else {
                mActiveResources = Reflector.on(resManager).get("mActiveResources");
            }
        } else {
            // 4.4以下使用ActivityThread#mActivityResources
            Object activityThread = Reflector.on("android.app.ActivityThread").call("currentActivityThread");
            mActiveResources = Reflector.on(activityThread).get("mActiveResources");
        }

        if (mActiveResources != null) {
            Object resKey = mActiveResources.keySet().iterator().next();
            if (resKey != null) {
                WeakReference<Resources> weakReference = mActiveResources.get(resKey);
                if (weakReference != null) {
                    mActiveResources.put(resKey, new WeakReference<Resources>(resources));
                }
            }
        }
    }
}
