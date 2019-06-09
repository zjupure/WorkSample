package com.zlc.work.hook;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.facebook.common.webp.BitmapCreator;
import com.facebook.common.webp.WebpBitmapFactory;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.bitmaps.HoneycombBitmapCreator;
import com.facebook.imagepipeline.memory.PoolConfig;
import com.facebook.imagepipeline.memory.PoolFactory;
import com.facebook.imagepipeline.nativecode.StaticWebpNativeLoader;
import com.facebook.soloader.SoLoader;
import com.facebook.webpsupport.WebpBitmapFactoryImpl;

import java.io.IOException;
import java.io.InputStream;

/**
 * author: liuchun
 * date: 2019-06-09
 */
public class ResourcesCompat extends ResourcesWrapper {

    final TypedValue mTmpValue = new TypedValue();
    final WebpBitmapFactory bitmapFactory;

    private static boolean sInitialized;

    private static synchronized void ensure(Context context) {
        if (!Fresco.hasBeenInitialized()) {
            try {
                SoLoader.init(context, 0);
                StaticWebpNativeLoader.ensure();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Context mContext;

    public ResourcesCompat(Context context, Resources resources) {
        super(resources);
        mContext = context;
        bitmapFactory = new WebpBitmapFactoryImpl();
        PoolFactory poolFactory = new PoolFactory(PoolConfig.newBuilder().build());
        BitmapCreator bitmapCreator = new HoneycombBitmapCreator(poolFactory);
        bitmapFactory.setBitmapCreator(bitmapCreator);
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        Drawable drawable = getWebpDrawable(id, 0);
        if (drawable != null) {
            return drawable;
        }
        return super.getDrawable(id);
    }

    @RequiresApi(21)
    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
        return super.getDrawable(id, theme);
    }

    @RequiresApi(15)
    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        Drawable drawable = getWebpDrawable(id, density);
        if (drawable != null) {
            return drawable;
        }
        return super.getDrawableForDensity(id, density);
    }

    @RequiresApi(21)
    @Override
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
        return super.getDrawableForDensity(id, density, theme);
    }

    private Drawable getWebpDrawable(int id, int desity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // let system to do webp decode
            return null;
        }

        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            getValueForDensity(id, desity, value, true);
            final String file = value.string.toString();
            if (file.endsWith(".webp")) {
                final InputStream is = openRawResource(id, value);
                ensure(mContext);
                Bitmap bitmap = bitmapFactory.decodeStream(is, new Rect(), getBitmapOptions(value));
                if (bitmap != null) {
                    return new BitmapDrawable(this, bitmap);
                }
            }
        }

        return null;
    }

    private BitmapFactory.Options getBitmapOptions(TypedValue value) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        if (opts.inDensity == 0 && value != null) {
            final int density = value.density;
            if (density == TypedValue.DENSITY_DEFAULT) {
                opts.inDensity = DisplayMetrics.DENSITY_DEFAULT;
            } else if (density != TypedValue.DENSITY_NONE) {
                opts.inDensity = density;
            }
        }

        if (opts.inTargetDensity == 0) {
            opts.inTargetDensity = getDisplayMetrics().densityDpi;
        }

//        if (opts.inScreenDensity == 0) {
//            opts.inScreenDensity = getDisplayMetrics().noncompatDensityDpi;
//        }
        return opts;
    }
}
