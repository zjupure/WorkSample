package com.zlc.work.widget.drawee.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;



import java.util.Arrays;

/**
 * author: liuchun
 * date: 2017/11/6
 */
public class RoundedDrawable extends DrawableWrapper implements Rounded {

    public enum Type {
        /**
         * Draws rounded corners on top of the underlying drawable by overlaying a solid color which
         * is specified by {@code setOverlayColor}. This option should only be used when the
         * background beneath the underlying drawable is static and of the same solid color.
         */
        OVERLAY_COLOR,

        /**
         * Clips the drawable to be rounded. This option is not supported right now but is expected to
         * be made available in the future.
         */
        CLIPPING
    }

    private Type mType = Type.OVERLAY_COLOR;
    private final float[] mRadii = new float[8];
    final float[] mBorderRadii = new float[8];
    final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean mIsCircle = false;
    private float mBorderWidth = 0;
    private int mBorderColor = Color.TRANSPARENT;
    private int mOverlayColor = Color.TRANSPARENT;
    //private float mPadding = 0;
    private final Path mPath = new Path();
    private final Path mBorderPath = new Path();
    private final RectF mTempRectangle = new RectF();

    public RoundedDrawable(Drawable drawable) {
        super(drawable);
    }

    @Override
    public void setCircle(boolean isCircle) {
        mIsCircle = isCircle;
        updatePath();
        invalidateSelf();
    }


    public boolean isCircle() {
        return mIsCircle;
    }


    @Override
    public void setRadius(float radius) {
        Arrays.fill(mRadii, radius);
        updatePath();
        invalidateSelf();
    }


    @Override
    public void setRadii(float[] radii) {
        if (radii == null) {
            Arrays.fill(mRadii, 0);
        } else {
            //Preconditions.checkArgument(radii.length == 8, "radii should have exactly 8 values");
            System.arraycopy(radii, 0, mRadii, 0, 8);
        }
        updatePath();
        invalidateSelf();
    }

    public float[] getRadii() {
        return mRadii;
    }

    public void setOverlayColor(int overlayColor) {
        mOverlayColor = overlayColor;
        invalidateSelf();
    }

    public int getOverlayColor() {
        return mOverlayColor;
    }


    @Override
    public void setBorder(int color, float width) {
        mBorderColor = color;
        mBorderWidth = width;
        updatePath();
        invalidateSelf();
    }


    public int getBorderColor() {
        return mBorderColor;
    }

    public float getBorderWidth() {
        return mBorderWidth;
    }


    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updatePath();
    }

    private void updatePath() {
        mPath.reset();
        mBorderPath.reset();
        mTempRectangle.set(getBounds());

        //mTempRectangle.inset(mPadding, mPadding);
        if (mIsCircle) {
            mPath.addCircle(
                    mTempRectangle.centerX(),
                    mTempRectangle.centerY(),
                    Math.min(mTempRectangle.width(), mTempRectangle.height()) / 2,
                    Path.Direction.CW);
        } else {
            mPath.addRoundRect(mTempRectangle, mRadii, Path.Direction.CW);
        }
        //mTempRectangle.inset(-mPadding, -mPadding);

        mTempRectangle.inset(mBorderWidth / 2, mBorderWidth / 2);
        if (mIsCircle) {
            float radius = Math.min(mTempRectangle.width(), mTempRectangle.height()) / 2;
            mBorderPath.addCircle(
                    mTempRectangle.centerX(), mTempRectangle.centerY(), radius, Path.Direction.CW);
        } else {
            for (int i = 0; i < mBorderRadii.length; i++) {
                mBorderRadii[i] = mRadii[i] - mBorderWidth / 2;  // + mPadding
            }
            mBorderPath.addRoundRect(mTempRectangle, mBorderRadii, Path.Direction.CW);
        }
        mTempRectangle.inset(-mBorderWidth / 2, -mBorderWidth / 2);
    }


    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        switch (mType) {
            case CLIPPING:
                int saveCount = canvas.save();
                // clip, note: doesn't support anti-aliasing
                mPath.setFillType(Path.FillType.EVEN_ODD);
                canvas.clipPath(mPath);
                super.draw(canvas);
                canvas.restoreToCount(saveCount);
                break;
            case OVERLAY_COLOR:
                super.draw(canvas);
                mPaint.setColor(mOverlayColor);
                mPaint.setStyle(Paint.Style.FILL);
                mPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
                canvas.drawPath(mPath, mPaint);

                if (mIsCircle) {
                    // INVERSE_EVEN_ODD will only draw inverse circle within its bounding box, so we need to
                    // fill the rest manually if the bounds are not square.
                    float paddingH = (bounds.width() - bounds.height() + mBorderWidth) / 2f;
                    float paddingV = (bounds.height() - bounds.width() + mBorderWidth) / 2f;
                    if (paddingH > 0) {
                        canvas.drawRect(bounds.left, bounds.top, bounds.left + paddingH, bounds.bottom, mPaint);
                        canvas.drawRect(
                                bounds.right - paddingH,
                                bounds.top,
                                bounds.right,
                                bounds.bottom,
                                mPaint);
                    }
                    if (paddingV > 0) {
                        canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.top + paddingV, mPaint);
                        canvas.drawRect(
                                bounds.left,
                                bounds.bottom - paddingV,
                                bounds.right,
                                bounds.bottom,
                                mPaint);
                    }
                }
                break;
        }

        if (mBorderColor != Color.TRANSPARENT) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mBorderColor);
            mPaint.setStrokeWidth(mBorderWidth);
            mPath.setFillType(Path.FillType.EVEN_ODD);
            canvas.drawPath(mBorderPath, mPaint);
        }
    }
}
