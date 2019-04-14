package com.zlc.work.widget.drawee.drawable;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.widget.ImageView.ScaleType;

/**
 * author: liuchun
 * date: 2017/11/8
 */
public class ScaleTypeDrawable extends DrawableWrapper {

    private Matrix mMatrix;
    private ScaleType mScaleType;

    private int mWrappedWidth = 0;
    private int mWrappedHeight = 0;

    private Matrix mDrawMatrix = null;

    // Avoid allocations...
    private final RectF mTempSrc = new RectF();
    private final RectF mTempDst = new RectF();

    public ScaleTypeDrawable(Drawable drawable) {
        this(drawable, ScaleType.FIT_CENTER);
    }

    public ScaleTypeDrawable(Drawable drawable, ScaleType scaleType) {
        super(drawable);
        mMatrix = new Matrix();
        mScaleType = scaleType;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            configureBounds();
            invalidateSelf();
        }
    }


    public void setMatrix(Matrix matrix) {
        if (matrix != null && matrix.isIdentity()) {
            matrix = null;
        }

        // don't invalidate unless we're actually changing our matrix
        if (matrix == null && !mMatrix.isIdentity() ||
                matrix != null && !mMatrix.equals(matrix)) {
            mMatrix.set(matrix);
            configureBounds();
            invalidateSelf();
        }
    }


    @Override
    public void draw(Canvas canvas) {
        configureBoundsIfUnderlyingChanged();
        if (mDrawMatrix != null) {
            int saveCount = canvas.save();
            canvas.clipRect(getBounds());
            canvas.concat(mDrawMatrix);
            super.draw(canvas);
            canvas.restoreToCount(saveCount);
        } else {
            // mDrawMatrix == null
            super.draw(canvas);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        configureBounds();
    }

    @Override
    public void setWrappedDrawable(Drawable drawable) {
        super.setWrappedDrawable(drawable);
        configureBounds();
    }


    private void configureBoundsIfUnderlyingChanged() {
        Drawable wrapped = getWrappedDrawable();
        if (mWrappedWidth != wrapped.getIntrinsicWidth() ||
                mWrappedHeight != wrapped.getIntrinsicHeight()) {
            configureBounds();
        }
    }


    private void configureBounds() {
        Drawable wrapped = getWrappedDrawable();
        Rect bounds = getBounds();
        int vwidth = bounds.width();
        int vheight = bounds.height();
        int dwidth = mWrappedWidth = wrapped.getIntrinsicWidth();
        int dheight = mWrappedHeight = wrapped.getIntrinsicHeight();

        if (dwidth <= 0 || dheight <= 0 || mScaleType == ScaleType.FIT_XY) {
            /* If the drawable has no intrinsic size, or we're told to
                scaletofit, then we just fill our entire view.
            */
            wrapped.setBounds(bounds);
            mDrawMatrix = null;
            return;
        }

        final boolean fits = (dwidth <= 0 || vwidth == dwidth)
                && (dheight <= 0 || vheight == dheight);
        if (fits) {
            // The bitmap fits exactly, no transform needed.
            wrapped.setBounds(bounds);
            mDrawMatrix = null;
            return;
        }

        // We need to do the scaling ourself, so have the drawable
        // use its native size.
        wrapped.setBounds(0, 0, dwidth, dheight);
        if (mScaleType == ScaleType.MATRIX) {
            mDrawMatrix = mMatrix.isIdentity() ? null : mMatrix;
        } else if (mScaleType == ScaleType.CENTER) {
            // Center bitmap in view, no scaling.
            mDrawMatrix = mMatrix;
            mDrawMatrix.setTranslate(Math.round((vwidth - dwidth) * 0.5f),
                    Math.round((vheight - dheight) * 0.5f));
        } else if (mScaleType == ScaleType.CENTER_CROP) {
            mDrawMatrix = mMatrix;

            float scale;
            float dx = 0, dy = 0;

            if (dwidth * vheight > vwidth * dheight) {
                scale = (float) vheight / (float) dheight;
                dx = (vwidth - dwidth * scale) * 0.5f;
            } else {
                scale = (float) vwidth / (float) dwidth;
                dy = (vheight - dheight * scale) * 0.5f;
            }

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate(Math.round(dx), Math.round(dy));
        } else if (mScaleType == ScaleType.CENTER_INSIDE) {
            mDrawMatrix = mMatrix;
            float scale;
            float dx;
            float dy;

            if (dwidth <= vwidth && dheight <= vheight) {
                scale = 1.0f;
            } else {
                scale = Math.min((float) vwidth / (float) dwidth,
                        (float) vheight / (float) dheight);
            }

            dx = Math.round((vwidth - dwidth * scale) * 0.5f);
            dy = Math.round((vheight - dheight * scale) * 0.5f);

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate(dx, dy);
        } else if (mScaleType != null){
            // Generate the required transform.
            mTempSrc.set(0, 0, dwidth, dheight);
            mTempDst.set(0, 0, vwidth, vheight);

            mDrawMatrix = mMatrix;
            mDrawMatrix.setRectToRect(mTempSrc, mTempDst, scaleTypeToScaleToFit(mScaleType));
        }
    }


    private static Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType st)  {
        // ScaleToFit enum to their corresponding Matrix.ScaleToFit values
        Matrix.ScaleToFit scaleToFit = null;
        if (st == ScaleType.FIT_XY) {
            scaleToFit = Matrix.ScaleToFit.FILL;
        } else if (st == ScaleType.FIT_START) {
            scaleToFit = Matrix.ScaleToFit.START;
        } else if (st == ScaleType.FIT_CENTER) {
            scaleToFit = Matrix.ScaleToFit.CENTER;
        } else if (st == ScaleType.FIT_END) {
            scaleToFit = Matrix.ScaleToFit.END;
        }
        return scaleToFit;
    }


    @Override
    public void getTransform(Matrix transform) {
        super.getTransform(transform);
        configureBoundsIfUnderlyingChanged();
        if (mDrawMatrix != null) {
            transform.preConcat(mDrawMatrix);
        }
    }
}
