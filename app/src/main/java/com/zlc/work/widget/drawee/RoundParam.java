package com.zlc.work.widget.drawee;

import android.graphics.Color;

import java.util.Arrays;

/**
 * author: liuchun
 * date: 2017/11/7
 */
public class RoundParam {

    private boolean mRoundAsCircle = false;
    private float[] mCornersRadii = null;
    private int mOverlayColor = 0;
    private float mBorderWidth = 0;
    private int mBorderColor = Color.TRANSPARENT;
    private boolean mRadiiNonZero = false;

    public RoundParam setRoundAsCircle(boolean roundAsCircle) {
        mRoundAsCircle = roundAsCircle;
        return this;
    }

    public boolean getRoundAsCircle() {
        return mRoundAsCircle;
    }

    public RoundParam setCornersRadius(float radius) {
        if (mCornersRadii == null) {
            mCornersRadii = new float[8];
        }
        Arrays.fill(mCornersRadii, radius);
        mRadiiNonZero = radius > 0;

        return this;
    }

    public RoundParam setCornersRadii(float[] radii) {
        if (radii.length != 8) {
            throw new IllegalArgumentException("length not match 8");
        }
        if (mCornersRadii == null) {
            mCornersRadii = new float[8];
        }
        System.arraycopy(radii, 0, mCornersRadii, 0, 8);
        mRadiiNonZero = false;
        for (int i = 0; i < 8; i++) {
            mRadiiNonZero |= (mCornersRadii[i] > 0);
        }

        return this;
    }

    public float[] getCornersRadii() {
        return mCornersRadii;
    }

    public RoundParam setOverlayColor(int overlayColor) {
        mOverlayColor = overlayColor;
        return this;
    }

    public int getOverlayColor() {
        return mOverlayColor;
    }

    public RoundParam setBorderWidth(float width) {
        mBorderWidth = width;
        return this;
    }

    public float getBorderWidth() {
        return mBorderWidth;
    }

    public RoundParam setBorderColor(int color) {
        mBorderColor = color;
        return this;
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public RoundParam setBorder(int color, float width) {
        mBorderColor = color;
        mBorderWidth = width;
        return this;
    }


    public boolean isValid() {
        return mRoundAsCircle ||
                mRadiiNonZero ||
                mBorderWidth > 0 ||
                mBorderColor != Color.TRANSPARENT ||
                mOverlayColor != Color.TRANSPARENT;
    }
}
