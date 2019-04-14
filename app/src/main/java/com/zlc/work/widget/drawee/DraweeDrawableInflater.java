package com.zlc.work.widget.drawee;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.zlc.work.R;


/**
 * author: liuchun
 * date: 2017/11/7
 */
public class DraweeDrawableInflater {
    private static final int DEFAULT_FADE_DURATION = 300;

    private static final ImageView.ScaleType[] sScaleTypeArray = {
            //ImageView.ScaleType.MATRIX,
            ImageView.ScaleType.FIT_XY,
            ImageView.ScaleType.FIT_START,
            ImageView.ScaleType.FIT_CENTER,
            ImageView.ScaleType.FIT_END,
            ImageView.ScaleType.CENTER,
            ImageView.ScaleType.CENTER_CROP,
            ImageView.ScaleType.CENTER_INSIDE
    };

    public static DraweeDrawable inflateDraweeDrawable(
            Context context,
            AttributeSet attrs,
            ImageView.ScaleType scaleType) {
        DraweeDrawable draweeDrawable = new DraweeDrawable(context);
        if (attrs == null) {
            return draweeDrawable;
        }

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GenericDraweeHierarchy);
        Drawable backgroundDrawable = null;
        Drawable placeholderDrawable = null;
        Drawable failureDrawable = null;
        Drawable overlayDrawable = null;
        Drawable pressedStateOverlayDrawable = null;
        Drawable retryDrawable = null;
        Drawable progressBarDrawable = null;
        int fadeDuration = DEFAULT_FADE_DURATION;
        RoundParam roundParam = new RoundParam();

        ImageView.ScaleType placeholderScaleType = scaleType;
        ImageView.ScaleType failureScaleType = scaleType;
        ImageView.ScaleType retryScaleType = scaleType;
        ImageView.ScaleType progressBarScaleType = scaleType;
        ImageView.ScaleType actualScaleType = scaleType;

        try {
            final int indexCount = ta.getIndexCount();
            for (int i = 0; i < indexCount; i++) {
                final int attr = ta.getIndex(i);
                if (attr == R.styleable.GenericDraweeHierarchy_placeholderImage) {
                    placeholderDrawable = ta.getDrawable(attr);
                } else if (attr == R.styleable.GenericDraweeHierarchy_failureImage) {
                    failureDrawable = ta.getDrawable(attr);
                } else if (attr == R.styleable.GenericDraweeHierarchy_backgroundImage) {
                    backgroundDrawable = ta.getDrawable(attr);
                } else if (attr == R.styleable.GenericDraweeHierarchy_overlayImage) {
                    overlayDrawable = ta.getDrawable(attr);
                } else if (attr == R.styleable.GenericDraweeHierarchy_pressedStateOverlayImage) {
                    pressedStateOverlayDrawable = ta.getDrawable(attr);
                } else if (attr == R.styleable.GenericDraweeHierarchy_retryImage) {
                    retryDrawable = ta.getDrawable(attr);
                } else if (attr == R.styleable.GenericDraweeHierarchy_progressBarImage) {
                    progressBarDrawable = ta.getDrawable(attr);
                } else if (attr == R.styleable.GenericDraweeHierarchy_backgroundImage) {
                    backgroundDrawable = ta.getDrawable(attr);
                } else if (attr == R.styleable.GenericDraweeHierarchy_placeholderImageScaleType) {
                    placeholderScaleType = getScaleTypeFromXml(ta, attr, scaleType);
                } else if (attr == R.styleable.GenericDraweeHierarchy_failureImageScaleType) {
                    failureScaleType = getScaleTypeFromXml(ta, attr, scaleType);
                } else if (attr == R.styleable.GenericDraweeHierarchy_retryImage) {
                    retryScaleType = getScaleTypeFromXml(ta, attr, scaleType);
                } else if (attr == R.styleable.GenericDraweeHierarchy_progressBarImage) {
                    progressBarScaleType = getScaleTypeFromXml(ta, attr, scaleType);
                } else if (attr == R.styleable.GenericDraweeHierarchy_actualImageScaleType) {
                    actualScaleType = getScaleTypeFromXml(ta, attr, scaleType);
                } else if (attr == R.styleable.GenericDraweeHierarchy_fadeDuration) {
                    fadeDuration = ta.getInt(attr, DEFAULT_FADE_DURATION);
                } else if (attr == R.styleable.GenericDraweeHierarchy_roundAsCircle) {
                    roundParam.setRoundAsCircle(ta.getBoolean(attr, false));
                } else if (attr == R.styleable.GenericDraweeHierarchy_roundedCornerRadius) {
                    roundParam.setCornersRadius(ta.getDimensionPixelSize(attr, 0));
                } else if (attr == R.styleable.GenericDraweeHierarchy_roundingBorderColor) {
                    roundParam.setBorderColor(ta.getColor(attr, 0));
                } else if (attr == R.styleable.GenericDraweeHierarchy_roundingBorderWidth) {
                    roundParam.setBorderWidth(ta.getDimensionPixelSize(attr, 0));
                }
            }
        } finally {
            ta.recycle();
        }

        // 设置图层的Drawable
        draweeDrawable.setBackgroundImage(backgroundDrawable);
        draweeDrawable.setPlaceholderImage(placeholderDrawable, placeholderScaleType);
        draweeDrawable.setFailureImage(failureDrawable, failureScaleType);
        draweeDrawable.setRetryImage(retryDrawable, retryScaleType);
        draweeDrawable.setProgressBarImage(progressBarDrawable, progressBarScaleType);
        draweeDrawable.setOverlayImage(overlayDrawable);
        draweeDrawable.setPressedStateOverlayImage(pressedStateOverlayDrawable);
        draweeDrawable.setActualImage(null, actualScaleType);
        // 更新圆角属性
        draweeDrawable.setFadeDuration(fadeDuration);
        draweeDrawable.setRoundingParam(roundParam);

        return draweeDrawable;
    }


    private static ImageView.ScaleType getScaleTypeFromXml(TypedArray ta, int attr, ImageView.ScaleType defValue) {
        final int index = ta.getInt(attr, -1);
        ImageView.ScaleType scaleType = defValue;
        if (index >= 0 && index < sScaleTypeArray.length) {
            scaleType = sScaleTypeArray[index];
        }

        if (scaleType == ImageView.ScaleType.MATRIX) {
            scaleType = ImageView.ScaleType.CENTER_CROP;
        }

        return scaleType;
    }
}
