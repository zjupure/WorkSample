package com.zlc.work.widget.drawee.drawable;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * copy from android support library and modify some implementation
 *
 * author: liuchun
 * date: 2017/11/6
 */
public class DrawableWrapper extends Drawable
        implements Drawable.Callback, TransfromableDrawable, TransformCallback{

    private Drawable mDrawable;

    protected TransformCallback mTransformCallback;

    public DrawableWrapper(Drawable drawable) {
        mDrawable = drawable;
        if (drawable != null) {
            mDrawable.setCallback(this);
            if (drawable instanceof TransfromableDrawable) {
                ((TransfromableDrawable)drawable).setTransformCallback(this);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mDrawable.setBounds(bounds);
    }

    @Override
    public void setChangingConfigurations(int configs) {
        mDrawable.setChangingConfigurations(configs);
    }

    @Override
    public int getChangingConfigurations() {
        return mDrawable.getChangingConfigurations();
    }

    @Override
    public void setDither(boolean dither) {
        mDrawable.setDither(dither);
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        mDrawable.setFilterBitmap(filter);
    }

    @Override
    public void setAlpha(int alpha) {
        mDrawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mDrawable.setColorFilter(cf);
    }

    @Override
    public boolean isStateful() {
        return mDrawable.isStateful();
    }

    @Override
    public boolean setState(final int[] stateSet) {
        return mDrawable.setState(stateSet);
    }

    @Override
    public int[] getState() {
        return mDrawable.getState();
    }

    @Override
    public void jumpToCurrentState() {
        DrawableCompat.jumpToCurrentState(mDrawable);
    }

    @Override
    public Drawable getCurrent() {
        return mDrawable.getCurrent();
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        return super.setVisible(visible, restart) || mDrawable.setVisible(visible, restart);
    }

    @Override
    public int getOpacity() {
        return mDrawable.getOpacity();
    }

    @Override
    public Region getTransparentRegion() {
        return mDrawable.getTransparentRegion();
    }

    @Override
    public int getIntrinsicWidth() {
        return mDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mDrawable.getIntrinsicHeight();
    }

    @Override
    public int getMinimumWidth() {
        return mDrawable.getMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        return mDrawable.getMinimumHeight();
    }

    @Override
    public boolean getPadding(Rect padding) {
        return mDrawable.getPadding(padding);
    }

    @NonNull
    @Override
    public Drawable mutate() {
        mDrawable.mutate();
        return this;
    }


    @Nullable
    @Override
    public ConstantState getConstantState() {
        return mDrawable.getConstantState();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        return mDrawable.setState(state);
    }

    @Override
    protected boolean onLevelChange(int level) {
        return mDrawable.setLevel(level);
    }

    @Override
    public void setAutoMirrored(boolean mirrored) {
        DrawableCompat.setAutoMirrored(mDrawable, mirrored);
    }

    @Override
    public boolean isAutoMirrored() {
        return DrawableCompat.isAutoMirrored(mDrawable);
    }

    @Override
    public void setTint(int tint) {
        DrawableCompat.setTint(mDrawable, tint);
    }

    @Override
    public void setTintList(ColorStateList tint) {
        DrawableCompat.setTintList(mDrawable, tint);
    }

    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        DrawableCompat.setTintMode(mDrawable, tintMode);
    }

    @Override
    public void setHotspot(float x, float y) {
        DrawableCompat.setHotspot(mDrawable, x, y);
    }

    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        DrawableCompat.setHotspotBounds(mDrawable, left, top, right, bottom);
    }

    // Drawable.Callback methods

    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }


    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }


    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

    // public method

    public Drawable getWrappedDrawable() {
        return mDrawable;
    }

    public void setWrappedDrawable(Drawable drawable) {
        final Drawable oldDrawable = mDrawable;
        if (oldDrawable != null) {
            oldDrawable.setCallback(null);
            if (oldDrawable instanceof TransfromableDrawable) {
                ((TransfromableDrawable) oldDrawable).setTransformCallback(null);
            }
        }

        mDrawable = drawable;

        if (drawable != null) {
            drawable.setCallback(this);
            if (drawable instanceof TransfromableDrawable) {
                ((TransfromableDrawable) drawable).setTransformCallback(this);
            }
            // 更新底层Drawable的绘制区域
            drawable.setBounds(getBounds());
        }
        invalidateSelf();
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
