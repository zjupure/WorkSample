package com.zlc.work.widget.drawee;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.Nullable;
import android.widget.ImageView.*;


import com.zlc.work.widget.drawee.drawable.DrawableWrapper;
import com.zlc.work.widget.drawee.drawable.Rounded;
import com.zlc.work.widget.drawee.drawable.RoundedBitmapDrawable;
import com.zlc.work.widget.drawee.drawable.RoundedColorDrawable;
import com.zlc.work.widget.drawee.drawable.RoundedDrawable;
import com.zlc.work.widget.drawee.drawable.ScaleTypeDrawable;


/**
 * author: liuchun
 * date: 2017/11/6
 */
public class DraweeDrawable extends DrawableWrapper implements Drawable.Callback{

    private static final int BACKGROUND_IMAGE_INDEX = 0;
    private static final int PLACEHOLDER_IMAGE_INDEX = 1;
    private static final int ACTUAL_IMAGE_INDEX = 2;
    private static final int PROGRESS_BAR_IMAGE_INDEX = 3;
    private static final int RETRY_IMAGE_INDEX = 4;
    private static final int FAILURE_IMAGE_INDEX = 5;
    private static final int OVERLAY_IMAGE_INDEX = 6;
    private static final int PRESSED_OVERLAY_IMAGE_INDEX = 7;

    private final Resources mResources;
    private RoundParam mRoundParam;

    private final Drawable mEmptyActualImageDrawable = new ColorDrawable(Color.TRANSPARENT);

    private final Drawable mRootDrawable;        //最顶层的Drawable对象
    private final FadeDrawable mFadeDrawable;    //支持动画的Drawable图层
    private final DrawableWrapper mActualImageDrawable;

    public DraweeDrawable(Context context) {
        super(new ColorDrawable(Color.TRANSPARENT));
        mResources = context.getResources();
        // construct drawable layers
        int numLayers = PRESSED_OVERLAY_IMAGE_INDEX + 1;
        Drawable[] layers = new Drawable[numLayers];
        for (int i = 0; i < layers.length; i++) {
            // fill all with empty ColorDrawable
            layers[i] = new ColorDrawable(Color.TRANSPARENT);
        }
        mActualImageDrawable = new DrawableWrapper(mEmptyActualImageDrawable);
        layers[ACTUAL_IMAGE_INDEX] = mActualImageDrawable;
        // fade drawable composed of layers
        mFadeDrawable = new FadeDrawable(layers);
        // root drawable
        mRootDrawable = maybeUpdateRounding(mFadeDrawable, mRoundParam, mResources);
        mRootDrawable.mutate();

        setWrappedDrawable(mRootDrawable);
        resetFade();
    }


    @Override
    public int getIntrinsicWidth() {
        // consume all the view bounds
        return super.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        // consume all the view bounds
        return super.getIntrinsicHeight();
    }


    public void reset() {
        resetActualImages();
        resetFade();
    }

    private void resetActualImages() {
        mActualImageDrawable.setWrappedDrawable(mEmptyActualImageDrawable);
    }

    private void resetFade() {
        if (mFadeDrawable != null) {
            mFadeDrawable.beginBatchMode();

            mFadeDrawable.fadeInAllLayers();
            fadeOutBranches();
            mFadeDrawable.fadeInLayer(PLACEHOLDER_IMAGE_INDEX);

            mFadeDrawable.finishTransitionImmediately();
            mFadeDrawable.endBatchMode();
        }
        // make actual image layer always valid
        setChildDrawableAtIndex(ACTUAL_IMAGE_INDEX, mActualImageDrawable);
    }

    private void fadeOutBranches() {
        if (mFadeDrawable != null) {
            mFadeDrawable.fadeOutLayer(PLACEHOLDER_IMAGE_INDEX);
            mFadeDrawable.fadeOutLayer(ACTUAL_IMAGE_INDEX);
            mFadeDrawable.fadeOutLayer(PROGRESS_BAR_IMAGE_INDEX);
            mFadeDrawable.fadeOutLayer(RETRY_IMAGE_INDEX);
            mFadeDrawable.fadeOutLayer(FAILURE_IMAGE_INDEX);
        }
    }

    public void setFadeDuration(int fadeDuration) {
        if (mFadeDrawable != null) {
            mFadeDrawable.setTransitionDuration(fadeDuration);
        }
    }

    public void setRoundingParam(RoundParam roundParam) {
        mRoundParam = roundParam;
        for (int i = 0; i < mFadeDrawable.getNumberOfLayers(); i++) {
            Drawable drawable = mFadeDrawable.getDrawable(i);
            mFadeDrawable.setDrawable(i, null);
            Drawable rounded = maybeUpdateRounding(drawable, mRoundParam, mResources);
            mFadeDrawable.setDrawable(i, rounded);
        }
    }

    public RoundParam getRoundingParam() {
        return mRoundParam;
    }

    // OPEN API
    public void setImage(@Nullable Drawable drawable, boolean immediate) {
        setActualImage(drawable);

        mFadeDrawable.beginBatchMode();
        fadeOutBranches();
        if (drawable == null) {
            mFadeDrawable.fadeInLayer(PLACEHOLDER_IMAGE_INDEX);
        } else {
            mFadeDrawable.fadeInLayer(ACTUAL_IMAGE_INDEX);
        }
        if (immediate) {
            mFadeDrawable.finishTransitionImmediately();
        }
        mFadeDrawable.endBatchMode();
    }

    public void setFailure(Throwable throwable) {
        mFadeDrawable.beginBatchMode();
        fadeOutBranches();
        if (mFadeDrawable.getDrawable(FAILURE_IMAGE_INDEX) != null) {
            mFadeDrawable.fadeInLayer(FAILURE_IMAGE_INDEX);
        } else if (mFadeDrawable.getDrawable(PLACEHOLDER_IMAGE_INDEX) != null){
            mFadeDrawable.fadeInLayer(PLACEHOLDER_IMAGE_INDEX);
        } else {
            mFadeDrawable.fadeInLayer(ACTUAL_IMAGE_INDEX);
        }
        mFadeDrawable.endBatchMode();
    }


    public void setRetry(Throwable throwable) {
        mFadeDrawable.beginBatchMode();
        fadeOutBranches();
        if (mFadeDrawable.getDrawable(RETRY_IMAGE_INDEX) != null) {
            mFadeDrawable.fadeInLayer(RETRY_IMAGE_INDEX);
        } else if (mFadeDrawable.getDrawable(PLACEHOLDER_IMAGE_INDEX) != null) {
            mFadeDrawable.fadeInLayer(PLACEHOLDER_IMAGE_INDEX);
        } else {
            mFadeDrawable.fadeInLayer(ACTUAL_IMAGE_INDEX);
        }
        mFadeDrawable.endBatchMode();
    }


    public boolean hasPressedStateOverlayImage() {
        // 是否有PressedStateOverlay
        return mFadeDrawable != null && mFadeDrawable.getDrawable(PRESSED_OVERLAY_IMAGE_INDEX) != null;
    }


    // Internal API

    public void setActualImage(@Nullable Drawable drawable) {
        Drawable desiredDrawable = maybeUpdateRounding(drawable, mRoundParam, mResources);
        if (desiredDrawable != null) {
            desiredDrawable.mutate();
        } else {
            desiredDrawable = new ColorDrawable(Color.TRANSPARENT);
        }
        mActualImageDrawable.setWrappedDrawable(desiredDrawable);
    }

    public void setActualImage(@Nullable Drawable drawable, @Nullable ScaleType scaleType) {
        setActualImage(drawable);
        wrapChildDrawableAtIndexWithScaleType(ACTUAL_IMAGE_INDEX, scaleType);
    }

    public void setActualImageScaleType(ScaleType scaleType) {
        wrapChildDrawableAtIndexWithScaleType(ACTUAL_IMAGE_INDEX, scaleType);
    }

    public void setPlaceholderImage(@Nullable Drawable drawable) {
        setChildDrawableAtIndex(PLACEHOLDER_IMAGE_INDEX, drawable);
    }

    public void setPlaceholderImage(@Nullable Drawable drawable, @Nullable ScaleType scaleType) {
        setChildDrawableAtIndex(PLACEHOLDER_IMAGE_INDEX, drawable);
        wrapChildDrawableAtIndexWithScaleType(PLACEHOLDER_IMAGE_INDEX, scaleType);
    }

    public void setFailureImage(@Nullable Drawable drawable) {
        setChildDrawableAtIndex(FAILURE_IMAGE_INDEX, drawable);
    }

    public void setFailureImage(@Nullable Drawable drawable, @Nullable ScaleType scaleType) {
        setChildDrawableAtIndex(FAILURE_IMAGE_INDEX, drawable);
        wrapChildDrawableAtIndexWithScaleType(FAILURE_IMAGE_INDEX, scaleType);
    }

    public void setOverlayImage(@Nullable Drawable drawable) {
        setChildDrawableAtIndex(OVERLAY_IMAGE_INDEX, drawable);
    }

    public void setOverlayImage(@Nullable Drawable drawable, @Nullable ScaleType scaleType) {
        setChildDrawableAtIndex(OVERLAY_IMAGE_INDEX, drawable);
        wrapChildDrawableAtIndexWithScaleType(OVERLAY_IMAGE_INDEX, scaleType);
    }

    public void setPressedStateOverlayImage(@Nullable Drawable drawable) {
        if (drawable != null && !(drawable instanceof StateListDrawable)) {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, drawable);
            drawable = stateListDrawable;
        }

        setChildDrawableAtIndex(PRESSED_OVERLAY_IMAGE_INDEX, drawable);
    }

    public void setRetryImage(@Nullable Drawable drawable) {
        setChildDrawableAtIndex(RETRY_IMAGE_INDEX, drawable);
    }

    public void setRetryImage(@Nullable Drawable drawable, @Nullable ScaleType scaleType) {
        setChildDrawableAtIndex(RETRY_IMAGE_INDEX, drawable);
        wrapChildDrawableAtIndexWithScaleType(RETRY_IMAGE_INDEX, scaleType);
    }

    public void setProgressBarImage(@Nullable Drawable drawable) {
        setChildDrawableAtIndex(PROGRESS_BAR_IMAGE_INDEX, drawable);
    }

    public void setProgressBarImage(@Nullable Drawable drawable, @Nullable ScaleType scaleType) {
        setChildDrawableAtIndex(PROGRESS_BAR_IMAGE_INDEX, drawable);
        wrapChildDrawableAtIndexWithScaleType(PROGRESS_BAR_IMAGE_INDEX, scaleType);
    }

    public void setBackgroundImage(@Nullable Drawable drawable) {
        setChildDrawableAtIndex(BACKGROUND_IMAGE_INDEX, drawable);
    }

    private void setChildDrawableAtIndex(int index, @Nullable Drawable drawable) {
        mFadeDrawable.setDrawable(index, null);
        Drawable desiredDrawable = maybeUpdateRounding(drawable, mRoundParam, mResources);
        mFadeDrawable.setDrawable(index, desiredDrawable);
    }

    private void wrapChildDrawableAtIndexWithScaleType(int index, @Nullable ScaleType scaleType) {
        Drawable drawable = mFadeDrawable.getDrawable(index);
        if (drawable == null || scaleType == null) {
            return;
        }
        //需要把LayerDrawable的图层置空, 否则会影响后续的Callback链
        mFadeDrawable.setDrawable(index, null);
        Drawable desiredDrawable = maybeWrapWithScaleType(drawable, scaleType);
        if (desiredDrawable != null) {
            desiredDrawable.mutate();
        }
        mFadeDrawable.setDrawable(index, desiredDrawable);
    }


    private static Drawable maybeUpdateRounding(Drawable drawable, RoundParam roundParam, Resources resources) {
        if (roundParam != null && roundParam.isValid()) {
            if (drawable instanceof Rounded) {
                // update param
                applyRoundingParams((Rounded)drawable, roundParam);
                return drawable;
            } else if (drawable instanceof DrawableWrapper) {
                Drawable wrapped = ((DrawableWrapper)drawable).getWrappedDrawable();
                ((DrawableWrapper)drawable).setWrappedDrawable(maybeUpdateRounding(wrapped, roundParam, resources));
                return drawable;
            } else if (drawable != null) {
                Drawable wrappedRound = wrapperDrawableRounded(drawable, resources);
                applyRoundingParams((Rounded)wrappedRound, roundParam);
                return wrappedRound;
            }
        } else if (drawable instanceof Rounded) {
            // reset to normal
            resetRoundingParam((Rounded)drawable);
        }

        return drawable;
    }


    private static Drawable wrapperDrawableRounded(Drawable drawable, Resources resources) {

        if (drawable instanceof Rounded) {
            return drawable;
        } else if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
            RoundedBitmapDrawable rounded = new RoundedBitmapDrawable(resources,
                    bitmapDrawable.getBitmap(), bitmapDrawable.getPaint());
            return rounded;
        } else if (drawable instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable)drawable;
            RoundedColorDrawable rounded = new RoundedColorDrawable(colorDrawable.getColor());
            return rounded;
        } else if (drawable != null) {
            RoundedDrawable rounded = new RoundedDrawable(drawable);
            return rounded;
        }

        return null;
    }

    private static Drawable maybeWrapWithScaleType(Drawable drawable, ScaleType scaleType) {
        if (drawable == null || scaleType == null) {
            return drawable;
        }

        ScaleTypeDrawable scaleTypeDrawable;
        if (drawable instanceof ScaleTypeDrawable) {
            scaleTypeDrawable = (ScaleTypeDrawable)drawable;
            scaleTypeDrawable.setScaleType(scaleType);
        } else {
            scaleTypeDrawable = new ScaleTypeDrawable(drawable, scaleType);
        }
        return scaleTypeDrawable;
    }

    private static void applyRoundingParams(Rounded rounded, RoundParam roundParam) {
        rounded.setCircle(roundParam.getRoundAsCircle());
        rounded.setRadii(roundParam.getCornersRadii());
        rounded.setBorder(roundParam.getBorderColor(), roundParam.getBorderWidth());
        if (rounded instanceof RoundedDrawable) {
            ((RoundedDrawable) rounded).setOverlayColor(roundParam.getOverlayColor());
        }
    }


    private static void resetRoundingParam(Rounded rounded) {
        rounded.setCircle(false);
        rounded.setRadius(0);
        rounded.setBorder(Color.TRANSPARENT, 0);
    }
}
