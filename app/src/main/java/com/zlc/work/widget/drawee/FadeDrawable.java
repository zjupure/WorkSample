package com.zlc.work.widget.drawee;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.zlc.work.widget.drawee.drawable.TransformCallback;
import com.zlc.work.widget.drawee.drawable.TransfromableDrawable;

import java.util.Arrays;

/**
 * author: liuchun
 * date: 2017/11/6
 */
public class FadeDrawable extends LayerDrawable
        implements TransformCallback, TransfromableDrawable{
    private static final String TAG = "FadeDrawable";

    private TransformCallback mTransformCallback;

    private static final int TRANSITION_STARTING = 0;
    private static final int TRANSITION_RUNNING = 1;
    private static final int TRANSITION_NONE = 2;

    private int mTransitionState;
    private int mDurationMs;
    private long mStartTimeMs;
    private int[] mStartAlphas;
    private int[] mAlphas;
    private int mAlpha = 255;

    private final Drawable[] mLayers;
    private boolean[] mIsLayerOn;

    private volatile int mInvalidateCount = 0;

    public FadeDrawable(Drawable[] layers) {
        super(layers);
        mLayers = layers;
        for (int index = 0; index < layers.length; index++) {
            setId(index, index);  // make the layer id same as index in LayerDrawable
            final Drawable drawable = layers[index];
            if (drawable != null && drawable instanceof TransfromableDrawable) {
                ((TransfromableDrawable) drawable).setTransformCallback(this);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPaddingMode(LayerDrawable.PADDING_MODE_STACK);
        }

        mStartAlphas = new int[layers.length];
        mAlphas = new int[layers.length];
        mIsLayerOn = new boolean[layers.length];
        resetInternal();
    }


    private void resetInternal() {
        mTransitionState = TRANSITION_NONE;
        Arrays.fill(mStartAlphas, 0);
        mStartAlphas[0] = 255;
        Arrays.fill(mAlphas, 0);
        mAlphas[0] = 255;
        Arrays.fill(mIsLayerOn, false);
        mIsLayerOn[0] = true;
    }


    public void setTransitionDuration(int duration) {
        mDurationMs = duration;
        if (mTransitionState == TRANSITION_RUNNING) {
            mTransitionState = TRANSITION_STARTING;
        }
    }

    public void fadeInLayer(int index) {
        mTransitionState = TRANSITION_STARTING;
        mIsLayerOn[index] = true;
        invalidateSelf();
    }

    public void fadeOutLayer(int index) {
        mTransitionState = TRANSITION_STARTING;
        mIsLayerOn[index] = false;
        invalidateSelf();
    }

    public void fadeInAllLayers() {
        mTransitionState = TRANSITION_STARTING;
        Arrays.fill(mIsLayerOn, true);
        invalidateSelf();
    }

    public void fadeOutAllLayers() {
        mTransitionState = TRANSITION_STARTING;
        Arrays.fill(mIsLayerOn, false);
        invalidateSelf();
    }


    public void finishTransitionImmediately() {
        mTransitionState = TRANSITION_NONE;
        for (int i = 0; i < mLayers.length; i++) {
            mAlphas[i] = mIsLayerOn[i] ? 255 : 0;
        }
        invalidateSelf();
    }

    private boolean updateAlphas(float ratio) {
        boolean done = true;

        for (int i = 0; i < mLayers.length; i++) {
            int dir = mIsLayerOn[i] ? +1 : -1;
            // determines alpha value and clamps it to [0, 255]
            mAlphas[i] = (int) (mStartAlphas[i] + dir * 255 * ratio);
            if (mAlphas[i] < 0) {
                mAlphas[i] = 0;
            }
            if (mAlphas[i] > 255) {
                mAlphas[i] = 255;
            }
            // determines whether the layer has reached its target opacity
            if (mIsLayerOn[i] && mAlphas[i] < 255) {
                done = false;
            }
            if (!mIsLayerOn[i] && mAlphas[i] > 0) {
                done = false;
            }
        }

        return done;
    }


    @Override
    public void invalidateSelf() {
        if (mInvalidateCount == 0) {
            super.invalidateSelf();
        }
    }


    public void beginBatchMode() {
        mInvalidateCount++;
    }

    public void endBatchMode() {
        mInvalidateCount--;
        invalidateSelf();
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        boolean done = true;
        float ratio;

        switch (mTransitionState) {
            case TRANSITION_STARTING:
                // initialize start alphas and start time
                System.arraycopy(mAlphas, 0, mStartAlphas, 0, mLayers.length);
                mStartTimeMs = SystemClock.uptimeMillis();
                // if the duration is 0, update alphas to the target opacities immediately
                ratio = (mDurationMs == 0) ? 1.0f : 0.0f;
                // if all the layers have reached their target opacity, transition is done
                done = updateAlphas(ratio);
                mTransitionState = done ? TRANSITION_NONE : TRANSITION_RUNNING;
                break;

            case TRANSITION_RUNNING:
                // determine ratio based on the elapsed time
                ratio = (float) (SystemClock.uptimeMillis() - mStartTimeMs) / mDurationMs;
                // if all the layers have reached their target opacity, transition is done
                done = updateAlphas(ratio);
                mTransitionState = done ? TRANSITION_NONE : TRANSITION_RUNNING;
                break;

            case TRANSITION_NONE:
                // there is no transition in progress and mAlphas should be left as is.
                done = true;
                break;
        }

        for (int i = 0; i < mLayers.length; i++) {
            //Log.i(TAG, "trying to draw layer " + i + ", mAlpha=" + mAlphas[i]);
            Drawable layer = getDrawable(i);
            drawDrawableWithAlpha(canvas, layer, mAlphas[i] * mAlpha / 255);
        }

        if (!done) {
            invalidateSelf();
        }
    }

    private void drawDrawableWithAlpha(Canvas canvas, Drawable drawable, int alpha) {
        if (drawable != null && alpha > 0) {
            //Log.i(TAG, "drawDrawableWithAlpha, alpha=" + alpha);
            mInvalidateCount++;
            drawable.mutate().setAlpha(alpha);
            mInvalidateCount--;
            drawable.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        super.setAlpha(alpha);
        mAlpha = alpha;
    }



    @Override
    public void setDrawable(int index, Drawable drawable) {
        setDrawableInternal(index, drawable);
        if (drawable == null) {
            drawable = new ColorDrawable(Color.TRANSPARENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            super.setDrawable(index, drawable);
        } else {
            super.setDrawableByLayerId(index, drawable);
        }
    }


    private void setDrawableInternal(int index, Drawable drawable) {
        final Drawable oldDrawable = mLayers[index];
        if (oldDrawable != null && oldDrawable instanceof TransfromableDrawable) {
            ((TransfromableDrawable) oldDrawable).setTransformCallback(null);
        }
        //更新Drawable
        mLayers[index] = drawable;
        if (drawable != null && drawable instanceof TransfromableDrawable) {
            ((TransfromableDrawable) drawable).setTransformCallback(this);
        }
    }


    @Override
    public Drawable getDrawable(int index) {
        return mLayers[index];
    }

    @Override
    public void setTransformCallback(TransformCallback transformCallback) {
        mTransformCallback = transformCallback;
    }

    @Override
    public void getTransform(Matrix transform) {
        if (mTransformCallback != null) {
            mTransformCallback.getTransform(transform);
        } else {
            transform.reset();
        }
    }

    @Override
    public void getRootBounds(RectF bounds) {
        if (mTransformCallback != null) {
            mTransformCallback.getRootBounds(bounds);
        } else {
            bounds.set(getBounds());
        }
    }
}
