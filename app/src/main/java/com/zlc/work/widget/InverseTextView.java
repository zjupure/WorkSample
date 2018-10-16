package com.zlc.work.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;


import com.zlc.work.R;

import java.lang.reflect.Field;

/**
 * author: liuchun
 * date: 2017/12/4
 */
public class InverseTextView extends TextView {

    private static Field sTextColorField;

    private final RectF mDrawRect = new RectF();

    private int mLeftColor;
    private int mRightColor;

    @IntRange(from = 0, to = 100)
    private int mProgress = 0;

    public InverseTextView(Context context) {
        super(context);
        init(context, null);
    }

    public InverseTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public InverseTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InverseTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {

        final int textColor = getCurrentTextColor();
        mLeftColor = mRightColor = textColor; // default value
        int progress = 0;
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.InverseTextView);

            mLeftColor = ta.getColor(R.styleable.InverseTextView_itv_leftColor, textColor);
            mRightColor = ta.getColor(R.styleable.InverseTextView_itv_rightColor, textColor);
            progress = ta.getInt(R.styleable.InverseTextView_itv_progress, 0);

            ta.recycle();
        }

        setProgress(progress);
    }


    private void disableHardwareAcceleratedIfNeed() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (mProgress <= 0 || mProgress >= 100) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // disable Hardware Accelerated
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

    }


    public void setLeftColor(@ColorInt int color) {
        mLeftColor = color;
        invalidate();
    }

    public void setRightColor(@ColorInt int color) {
        mRightColor = color;
        invalidate();
    }

    /**
     * 设置进度
     * @param progress
     */
    public void setProgress(int progress) {
        if (progress != mProgress) {

            mProgress = progress;
            disableHardwareAcceleratedIfNeed();
            if (mProgress <= 0) {
                setTextColor(mRightColor);
            } else if (mProgress >= 100) {
                setTextColor(mLeftColor);
            } else {
                invalidate();
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (mProgress <= 0 || mProgress >= 100) {
            super.onDraw(canvas);
            return;
        }

        final int width = getWidth();
        final int height = getHeight();
        final float divider = width * mProgress / 100f;
        // 绘制左侧文本
        setTextColorByReflect(mLeftColor);
        mDrawRect.set(0, 0, divider, height);
        int saveCount = canvas.save();
        canvas.clipRect(mDrawRect);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
        // 绘制右侧文本
        setTextColorByReflect(mRightColor);
        mDrawRect.set(divider, 0, width, height);
        saveCount = canvas.save();
        canvas.clipRect(mDrawRect);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }


    /**
     * 使用反射修改TextView内部的textColor值，防止在onDraw()中调用{@link #setTextColor(int)}造成无限递归
     * @param color
     */
    private void setTextColorByReflect(@ColorInt int color) {

        try {
            if (sTextColorField == null) {
                sTextColorField = TextView.class.getDeclaredField("mCurTextColor");
                sTextColorField.setAccessible(true);
            }

            sTextColorField.set(this, color);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        getPaint().setColor(color);
    }
}
