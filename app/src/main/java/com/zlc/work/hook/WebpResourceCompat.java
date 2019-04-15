package com.zlc.work.hook;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.facebook.common.webp.BitmapCreator;
import com.facebook.common.webp.WebpBitmapFactory;
import com.facebook.imagepipeline.bitmaps.HoneycombBitmapCreator;
import com.facebook.imagepipeline.memory.PoolConfig;
import com.facebook.imagepipeline.memory.PoolFactory;
import com.facebook.webpsupport.WebpBitmapFactoryImpl;
import com.zlc.work.reflect.Reflector;

import java.io.InputStream;

/**
 * author: liuchun
 * date: 2019/6/10
 */
public class WebpResourceCompat {

    private static final TypedValue mTmpValue = new TypedValue();
    private static WebpBitmapFactory sBitmapFactory;

    private static WebpBitmapFactory getWebpBitmapFactory() {
        if (sBitmapFactory == null) {
            sBitmapFactory = new WebpBitmapFactoryImpl();
            PoolFactory poolFactory = new PoolFactory(PoolConfig.newBuilder().build());
            BitmapCreator bitmapCreator = new HoneycombBitmapCreator(poolFactory);
            sBitmapFactory.setBitmapCreator(bitmapCreator);
        }
        return sBitmapFactory;
    }

    public static Drawable getWebpDrawable(Resources resources, int id, int density) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // let system to do webp decode
            return null;
        }

        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                resources.getValueForDensity(id, density, value, true);
            } else {
                // API 14，该方法是@hide的
                if (density == 0) {
                    resources.getValue(id, value, true);
                } else {
                    Class<?>[] paramTypes = new Class[]{int.class, int.class, TypedValue.class, boolean.class};
                    Reflector.on(resources).call("getValueForDensity", paramTypes,
                            id, density, value, true);
                }
            }

            final String file = value.string.toString();
            if (file.endsWith(".webp")) {
                final InputStream is = resources.openRawResource(id, value);
                //ensure(mContext);
                WebpBitmapFactory bitmapFactory = getWebpBitmapFactory();
                BitmapFactory.Options opts = getBitmapOptions(resources, value);
                Bitmap bitmap = bitmapFactory.decodeStream(is, new Rect(), opts);
                if (bitmap != null) {
                    return new BitmapDrawable(resources, bitmap);
                }
            }
        }
        return null;
    }

    private static BitmapFactory.Options getBitmapOptions(Resources resources, TypedValue value) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        if (opts.inDensity == 0 && value != null) {
            final int density = value.density;
            if (density == TypedValue.DENSITY_DEFAULT) {
                opts.inDensity = DisplayMetrics.DENSITY_DEFAULT;
            } else if (density != TypedValue.DENSITY_NONE) {
                opts.inDensity = density;
            }
        }
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (opts.inTargetDensity == 0) {
            opts.inTargetDensity = dm.densityDpi;
        }

        if (opts.inScreenDensity == 0) {
            opts.inScreenDensity = Reflector.on(dm).get("noncompatDensityDp");
        }
        return opts;
    }
}
