package com.zlc.work.widget.drawee.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;

import java.util.Arrays;


/**
 * author: liuchun
 * date: 2017/11/7
 */
public class RoundedColorDrawable extends ColorDrawable implements Rounded {

    private final float[] mRadii = new float[8];
    final float[] mBorderRadii = new float[8];
    final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean mIsCircle = false;
    private float mBorderWidth = 0;
    //private float mPadding = 0;
    private int mBorderColor = Color.TRANSPARENT;
    final Path mPath = new Path();
    final Path mBorderPath = new Path();
    //private int mColor = Color.TRANSPARENT;
    private final RectF mTempRect = new RectF();
    //private int mAlpha = 255;

    public RoundedColorDrawable(){
        super();
    }

    public RoundedColorDrawable(int color) {
        super(color);
    }

    public RoundedColorDrawable(float[] radii, int color) {
        super(color);
        setRadii(radii);
    }

    public RoundedColorDrawable(float radius, int color) {
        this(color);
        setRadius(radius);
    }

    public static RoundedColorDrawable fromColorDrawable(ColorDrawable colorDrawable) {
        return new RoundedColorDrawable(colorDrawable.getColor());
    }


    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updatePath();
    }


    @Override
    public void draw(Canvas canvas) {
        int mColor = getColor();
        int mAlpha = getAlpha();
        mPaint.setColor(multiplyColorAlpha(mColor, mAlpha));
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mPath, mPaint);
        if (mBorderWidth != 0) {
            mPaint.setColor(multiplyColorAlpha(mBorderColor, mAlpha));
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mBorderWidth);
            canvas.drawPath(mBorderPath, mPaint);
        }
    }


    @Override
    public void setCircle(boolean isCircle) {
        mIsCircle = isCircle;
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


    @Override
    public void setRadius(float radius) {
        //Preconditions.checkArgument(radius >= 0, "radius should be non negative");
        Arrays.fill(mRadii, radius);
        updatePath();
        invalidateSelf();
    }

    @Override
    public void setBorder(int color, float width) {
        if (mBorderColor != color) {
            mBorderColor = color;
            invalidateSelf();
        }

        if (mBorderWidth != width) {
            mBorderWidth = width;
            updatePath();
            invalidateSelf();
        }
    }


    private void updatePath() {
        mPath.reset();
        mBorderPath.reset();
        mTempRect.set(getBounds());

        mTempRect.inset(mBorderWidth/2, mBorderWidth/2);
        if (mIsCircle) {
            float radius = Math.min(mTempRect.width(), mTempRect.height())/2;
            mBorderPath.addCircle(mTempRect.centerX(), mTempRect.centerY(), radius, Path.Direction.CW);
        } else {
            for (int i = 0; i < mBorderRadii.length; i++) {
                mBorderRadii[i] = mRadii[i] - mBorderWidth/2;  // + mPadding
            }
            mBorderPath.addRoundRect(mTempRect, mBorderRadii, Path.Direction.CW);
        }
        mTempRect.inset(-mBorderWidth/2, -mBorderWidth/2);

        mTempRect.inset(mBorderWidth/2, mBorderWidth/2);
        if (mIsCircle) {
            float radius = Math.min(mTempRect.width(), mTempRect.height())/2;
            mPath.addCircle(mTempRect.centerX(), mTempRect.centerY(), radius, Path.Direction.CW);
        } else {
            mPath.addRoundRect(mTempRect, mRadii, Path.Direction.CW);
        }
        mTempRect.inset(-mBorderWidth/2, -mBorderWidth/2);
    }


    private int multiplyColorAlpha(int color, int alpha) {
        if (alpha == 255) {
            return color;
        }
        if (alpha == 0) {
            return color & 0x00FFFFFF;
        }
        alpha = alpha + (alpha >> 7); // make it 0..256
        int colorAlpha = color >>> 24;
        int multipliedAlpha = colorAlpha * alpha >> 8;
        return (multipliedAlpha << 24) | (color & 0x00FFFFFF);
    }
}
