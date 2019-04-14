package com.zlc.work.widget.drawee;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zlc.work.R;


/**
 * 为适配Glide而参照Fresco的DraweeView重写的控件，
 * 将Drawable和图片加载逻辑分离，该控件不做加载控制，只做图片的多层展示
 *
 * author: liuchun
 * date: 2017/11/7
 */
public class GenericDraweeView extends ImageView {
    private static final String TAG = "GenericDraweeView";
    // GenericDraweeView的Drawable, 支持多个图层展示
    private DraweeDrawable mTopDrawable;
    // DraweeDrawable的宽、高
    private int mDrawableWidth;
    private int mDrawableHeight;
    // 保存父类ImageView原始的ScaleType
    private ScaleType mOriScaleType;
    // 控件尺寸宽高比
    private float mAspectRatio = 0;

    public GenericDraweeView(Context context) {
        super(context);
        init(context, null);
    }

    public GenericDraweeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GenericDraweeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GenericDraweeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * 初始化xml属性
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, @Nullable AttributeSet attrs) {

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GenericDraweeHierarchy);
            if (ta.hasValue(R.styleable.GenericDraweeHierarchy_viewAspectRatio)) {
                float ratio = ta.getFloat(R.styleable.GenericDraweeHierarchy_viewAspectRatio, 0);
                setAspectRatio(ratio);
            }
            ta.recycle();
        }

        if (mOriScaleType == null) {
            // xml没有配置ScaleType, 需要固定ImageView的ScaleType
            mOriScaleType = ScaleType.FIT_CENTER;   //默认值
            super.setScaleType(ScaleType.FIT_XY);
        }
        // 生成DraweeDrawable对象
        mTopDrawable = DraweeDrawableInflater.inflateDraweeDrawable(context, attrs, mOriScaleType);
        Drawable actualDrawable = getDrawable();
        if (isInEditMode() && actualDrawable != null) {
            // in develop tools model
            mTopDrawable.setPlaceholderImage(actualDrawable, mOriScaleType);
        }

        mTopDrawable.setImage(actualDrawable, true);
        super.setImageDrawable(mTopDrawable);
        mDrawableWidth = mTopDrawable.getIntrinsicWidth();
        mDrawableWidth = mTopDrawable.getIntrinsicHeight();
    }

    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio == mAspectRatio) {
            return;
        }
        mAspectRatio = aspectRatio;
        requestLayout();
    }

    public float getAspectRatio() {
        return mAspectRatio;
    }

    public DraweeDrawable getDraweeDrawable() {
        return mTopDrawable;
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {

        if (drawable instanceof DraweeDrawable) {
            // multi-layer drawable
            mTopDrawable = (DraweeDrawable)drawable;
            super.setImageDrawable(drawable);
            updateDrawable(drawable);
            return;
        } else if (drawable instanceof LayerDrawable) {
            // LayerDrawable such as TransitionDrawable, forbidden recursion
            LayerDrawable ld = (LayerDrawable)drawable;
            for (int i = 0; i < ld.getNumberOfLayers(); i++) {
                Drawable child = ld.getDrawable(i);
                if (child == mTopDrawable && mTopDrawable != null) {
                    super.setImageDrawable(drawable);
                    updateDrawable(drawable);
                    return;
                }
            }
        }

        if (mTopDrawable != null) {
            // replace the actual image on DraweeDrawable
            mTopDrawable.setImage(drawable, false);
            drawable = mTopDrawable;
        }
        super.setImageDrawable(drawable);
        updateDrawable(drawable);
    }

    /**
     * 由于父类{@link ImageView#setImageDrawable(Drawable)}对于同一Drawable实例不会更新layout
     * 所以需要根据Drawable的size是否变化来决定是否需要重新layout
     * wrap_content才需要重新layout，避免不必要的layout开销
     *
     * @param drawable
     */
    private void updateDrawable(Drawable drawable) {
        final int width = drawable != null ? drawable.getIntrinsicWidth() : -1;
        final int height = drawable != null ? drawable.getIntrinsicHeight() : -1;
        // update drawable size
        if (width != mDrawableWidth || height != mDrawableHeight) {
            mDrawableWidth = width;
            mDrawableHeight = height;

            final ViewGroup.LayoutParams lp = getLayoutParams();
            if (lp == null || shouldAdjust(lp)) {
                // 重新layout
                requestLayout();
            }
        }
        // Drawable改变了,始终需要重绘
        invalidate();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm == null) {
            setImageDrawable(null);
        } else {
            setImageDrawable(new BitmapDrawable(getResources(), bm));
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        setImageDrawable(super.getDrawable());
    }

    @Override
    public void setImageURI(@Nullable Uri uri) {
        super.setImageURI(uri);
        setImageDrawable(super.getDrawable());
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException("GenericDraweeView ScaleType == null");
        }
        if (scaleType == ScaleType.MATRIX) {
            throw new IllegalArgumentException("not support matrix ScaleType");
        }
        mOriScaleType = scaleType;
        if (mTopDrawable != null) {
            mTopDrawable.setActualImageScaleType(scaleType);
        }

        // fix the ImageView scaleType to FIT_XY and make the underlying drawable consume all the view bounds
        super.setScaleType(ScaleType.FIT_XY);
    }

    @Override
    public ScaleType getScaleType() {
        return mOriScaleType;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        ViewGroup.LayoutParams lp = getLayoutParams();
        if (mAspectRatio <= 0 || lp == null
                || isWrapContent(lp)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            fixWrapContentMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        final int hPadding = getPaddingLeft() + getPaddingRight();
        final int vPadding = getPaddingTop() + getPaddingBottom();
        if (shouldAdjust(lp.height)) {
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int desiredHeight = (int)((widthSpecSize - hPadding) / mAspectRatio + vPadding);
            int resolvedHeight = View.resolveSize(desiredHeight, heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(resolvedHeight, MeasureSpec.EXACTLY);
        } else if (shouldAdjust(lp.width)) {
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            int desiredWidth = (int)((heightSpecSize - vPadding) / mAspectRatio + hPadding);
            int resolvedWidth = View.resolveSize(desiredWidth, widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(resolvedWidth, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 设置了Drawable对象, 如果Drawable的大小不确定,
     * wrap_content会使ImageView的size变成1*1, 需要处理成0*0
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private void fixWrapContentMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        ViewGroup.LayoutParams lp = getLayoutParams();
        if (!shouldAdjust(lp)) {
            // No need to fix wrap_content 1*1 issue, to avoid measure twice
            return;
        }

        final Drawable drawable = getDrawable();
        if (drawable != null) {
            int w = mDrawableWidth = drawable.getIntrinsicWidth();
            int h = mDrawableHeight = drawable.getIntrinsicHeight();
            if (w > 0 && h > 0) {
                // No need to fix wrap_content 1*1 issue
                return;
            }

            if (w <= 0) w = 0;
            if (h <= 0) h = 0;
            w += getPaddingLeft() + getPaddingRight();
            h += getPaddingTop() + getPaddingBottom();

            w = Math.max(w, getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());

            int widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
            int heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    private static boolean isWrapContent(ViewGroup.LayoutParams lp) {
        return lp.width == ViewGroup.LayoutParams.WRAP_CONTENT && lp.height == ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private static boolean shouldAdjust(ViewGroup.LayoutParams lp) {
        return lp != null && (shouldAdjust(lp.width) || shouldAdjust(lp.height));
    }

    private static boolean shouldAdjust(int layout) {
        return layout == 0 || layout == ViewGroup.LayoutParams.WRAP_CONTENT;
    }
}
