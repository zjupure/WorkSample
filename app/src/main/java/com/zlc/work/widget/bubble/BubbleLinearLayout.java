package com.zlc.work.widget.bubble;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.zlc.work.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 气泡布局
 */
public class BubbleLinearLayout extends LinearLayout {
    //气泡相关常量
    public static final int ORIENT_TOP = 0;
    public static final int ORIENT_LEFT = 1;
    public static final int ORIENT_RIGHT = 2;
    public static final int ORIENT_BOTTOM = 3;
    public static final int ORIENT_NONE = 4;

    public static final int ARROW_LEFT = 0;
    public static final int ARROW_CENTER = 1;
    public static final int ARROW_RIGHT = 2;
    public static final int ARROW_TOP = 3;
    public static final int ARROW_BOTTOM = 4;

    /**
     * 气泡尖角方向
     */
    @IntDef({ORIENT_TOP, ORIENT_LEFT, ORIENT_RIGHT, ORIENT_BOTTOM, ORIENT_NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ArrowOrientation {
    }

    /**
     * 气泡尖角样式
     */
    @IntDef({ARROW_LEFT, ARROW_CENTER, ARROW_RIGHT, ARROW_TOP, ARROW_BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ArrowStyle {

    }

    public static float dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }

    private int mMinPadding;
    private int mMinWidth;
    private int mMinHeight;

    private int mCornerRadius;
    // 箭头的样式
    @ArrowStyle
    private int mArrowStyle = ARROW_CENTER;
    // 箭头的方向
    @ArrowOrientation
    private int mArrowOrientation = ORIENT_TOP;
    // 箭头的高度
    private int mArrowHeight;
    // 箭头对齐的位置
    private int mArrowGravity = Gravity.CENTER;
    // 箭头的偏移量
    private float mArrowOffset = 0f;

    private int mGradientOrientation = HORIZONTAL;
    private int mStartColor = 0xff435570;
    private int mEndColor = 0xff435570;
    private float mStorkWidth = 2.0f;
    private Paint mFillPaint = null;
    private final Path mPath = new Path();
    private final Path mBubbleArrowPath = new Path();

    private RectF mRoundRect;
    private float mWidth;
    private float mHeight;

    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;

    public BubbleLinearLayout(Context context) {
        this(context, null);
    }

    public BubbleLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void initBubble(Context context) {
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();
        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();

        mArrowHeight = (int) dip2px(context, 6);
        mCornerRadius = (int) dip2px(context, 2);

        mMinWidth = (int) dip2px(context, 50);
        mMinHeight = (int) dip2px(context, 35);
        mMinPadding = (int) dip2px(context, 4);
    }

    private void init(final Context context, final AttributeSet attrs) {

        initBubble(context);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BubbleLinearLayout);
            mArrowStyle = ta.getInt(R.styleable.BubbleLinearLayout_bll_arrow_style, ARROW_CENTER);
            mArrowHeight = ta.getDimensionPixelSize(R.styleable.BubbleLinearLayout_bll_arrow_height, mArrowHeight);
            mArrowOrientation = ta.getInt(R.styleable.BubbleLinearLayout_bll_arrow_orientation, ORIENT_TOP);
            mArrowGravity = ta.getInt(R.styleable.BubbleLinearLayout_bll_gravity, Gravity.CENTER);
            mArrowOffset = ta.getDimensionPixelSize(R.styleable.BubbleLinearLayout_bll_arrow_offset, 0);

            mCornerRadius = ta.getDimensionPixelSize(R.styleable.BubbleLinearLayout_bll_corner_radius, mCornerRadius);
            mStartColor = ta.getDimensionPixelOffset(R.styleable.BubbleLinearLayout_bll_start_color, mStartColor);
            mEndColor = ta.getDimensionPixelSize(R.styleable.BubbleLinearLayout_bll_end_color, mEndColor);
            ta.recycle();
        }

        mFillPaint = new Paint();
        mFillPaint.setStyle(Style.FILL);
        mFillPaint.setStrokeCap(Cap.BUTT);
        mFillPaint.setAntiAlias(true);
        mFillPaint.setStrokeWidth(mStorkWidth);
        mFillPaint.setStrokeJoin(Paint.Join.MITER);
        applyColor();

        setLayerType(LAYER_TYPE_SOFTWARE, mFillPaint);
        renderBubbleLegPrototype();
        setBackgroundColor(Color.TRANSPARENT);
        setClipChildren(false);

        mPaddingTop = Math.max(mMinPadding, mPaddingTop);
        mPaddingBottom = Math.max(mMinPadding, mPaddingBottom);
        mPaddingLeft = Math.max(mMinPadding, mPaddingLeft);
        mPaddingRight = Math.max(mMinPadding, mPaddingRight);
        applyBubblePadding();
    }

    private void applyColor() {
        int w = getMeasuredWidth(), h = getMeasuredHeight();
        if (mStartColor == mEndColor) {
            mFillPaint.setShader(null);
            mFillPaint.setColor(mStartColor);
        } else if (w > 0 && h > 0) {
            LinearGradient shader = mGradientOrientation == HORIZONTAL ?
                    new LinearGradient(0, h / 2, w, h / 2, mStartColor, mEndColor, Shader.TileMode.CLAMP) :
                    new LinearGradient(w / 2, 0, w / 2, h, mStartColor, mEndColor, Shader.TileMode.CLAMP);
            mFillPaint.setShader(shader);
        }
    }

    private void applyBubblePadding() {
        switch (mArrowOrientation) {
            case ORIENT_TOP:
                super.setPadding(mPaddingLeft, mPaddingTop + mArrowHeight, mPaddingRight, mPaddingBottom);
                break;
            case ORIENT_RIGHT:
                super.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight + mArrowHeight, mPaddingBottom);
                break;
            case ORIENT_BOTTOM:
                super.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom + mArrowHeight);
                break;
            case ORIENT_LEFT:
                super.setPadding(mPaddingLeft + mArrowHeight, mPaddingTop, mPaddingRight, mPaddingBottom);
                break;
            default:
                super.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
                break;
        }

    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        mPaddingLeft = left;
        mPaddingRight = right;
        mPaddingTop = top;
        mPaddingBottom = bottom;
        applyBubblePadding();
    }

    public void setPaintColor(@ColorInt int color) {
        setPaintColor(color, color);
    }

    public void setPaintColor(@ColorInt int startColor, @ColorInt int encColor) {
        setPaintColor(startColor, encColor, HORIZONTAL);
    }

    private void setPaintColor(@ColorInt int startColor, @ColorInt int endColor,
                               int orientation) {
        this.mStartColor = startColor;
        this.mEndColor = endColor;
        this.mGradientOrientation = orientation;
        applyColor();
    }

    /**
     * 尖角path
     */
    private void renderBubbleLegPrototype() {
        mBubbleArrowPath.reset();
        if (isRelativeLeft()) {
            mBubbleArrowPath.moveTo(0, 0);
            mBubbleArrowPath.lineTo(mArrowHeight, -mArrowHeight);
            mBubbleArrowPath.lineTo(2 * mArrowHeight, 0);
            mBubbleArrowPath.close();
        } else if (isRelativeRight()) {
            mBubbleArrowPath.moveTo(0, 0);
            mBubbleArrowPath.lineTo(mArrowHeight, mArrowHeight);
            mBubbleArrowPath.lineTo(2 * mArrowHeight, 0);
            mBubbleArrowPath.close();
        } else {
            // 默认箭头在中间
            mBubbleArrowPath.moveTo(0, 0);
            mBubbleArrowPath.lineTo(mArrowHeight, -mArrowHeight);
            mBubbleArrowPath.lineTo(mArrowHeight, mArrowHeight);
            mBubbleArrowPath.close();
        }
    }

    public void setArrowHeight(int height) {
        this.mArrowHeight = height;
    }

    public int getArrowHeight() {
        return mArrowHeight;
    }

    public void setArrowStyle(@ArrowStyle int arrowStyle) {
        if (arrowStyle != this.mArrowStyle) {
            this.mArrowStyle = arrowStyle;
            renderBubbleLegPrototype();
        }
    }

    public int getArrowOrientation() {
        return this.mArrowOrientation;
    }

    public void setArrowOrientation(@ArrowOrientation int arrowOrientation) {
        this.mArrowOrientation = arrowOrientation;
        applyBubblePadding();
    }

    public void setArrowPosition(int gravity, float offset) {
        this.mArrowGravity = gravity;
        this.mArrowOffset = offset;
    }

    private boolean isRelativeLeft() {
        boolean isLeft = false;
        switch (mArrowOrientation) {
            case ORIENT_TOP:
                isLeft = mArrowStyle == ARROW_LEFT || mArrowStyle == ARROW_TOP;
                break;
            case ORIENT_BOTTOM:
                isLeft = mArrowStyle == ARROW_RIGHT || mArrowStyle == ARROW_BOTTOM;
                break;
            case ORIENT_LEFT:
                isLeft = mArrowStyle == ARROW_TOP || mArrowStyle == ARROW_LEFT;
                break;
            case ORIENT_RIGHT:
                isLeft = mArrowStyle == ARROW_BOTTOM || mArrowStyle == ARROW_RIGHT;
                break;
        }
        return isLeft;
    }

    private boolean isRelativeRight() {
        boolean isRight = false;
        switch (mArrowOrientation) {
            case ORIENT_TOP:
                isRight = mArrowStyle == ARROW_RIGHT || mArrowStyle == ARROW_BOTTOM;
                break;
            case ORIENT_BOTTOM:
                isRight = mArrowStyle == ARROW_LEFT || mArrowStyle == ARROW_TOP;
                break;
            case ORIENT_LEFT:
                isRight = mArrowStyle == ARROW_BOTTOM || mArrowStyle == ARROW_RIGHT;
                break;
            case ORIENT_RIGHT:
                isRight = mArrowStyle == ARROW_TOP || mArrowStyle == ARROW_LEFT;
                break;
        }
        return isRight;
    }

    private boolean isHorizontal() {
        return mArrowOrientation == ORIENT_TOP || mArrowOrientation == ORIENT_BOTTOM;
    }

    private boolean isVertical() {
        return mArrowOrientation == ORIENT_LEFT || mArrowOrientation == ORIENT_RIGHT;
    }

    /**
     * 根据显示方向，获取尖角位置矩阵
     *
     * @param width
     * @param height
     * @return
     */
    private Matrix renderBubbleArrowMatrix(final float width, final float height) {

        float offset = getBubbleOffset();

        float dstX = 0;
        float dstY = 0;
        final Matrix matrix = new Matrix();

        switch (mArrowOrientation) {
            case ORIENT_TOP:
                dstX = Math.min(offset, width);
                dstY = 0;
                matrix.postRotate(90);
                //setPadding(0, mArrowHeight, 0, 0);
                //setGravity(Gravity.CENTER);
                mRoundRect = new RectF(0, mArrowHeight, mWidth, mHeight);
                break;

            case ORIENT_RIGHT:
                dstX = width;
                dstY = Math.min(offset, height);
                matrix.postRotate(180);
                //setPadding(0, 0, mArrowHeight, 0);
                //setGravity(Gravity.CENTER);
                mRoundRect = new RectF(0, 0, mWidth - mArrowHeight, mHeight);
                break;

            case ORIENT_BOTTOM:
                dstX = Math.min(offset, width);
                dstY = height;
                matrix.postRotate(270);
                //setPadding(mArrowHeight, 0, 0, 0);
                //setGravity(Gravity.CENTER);
                mRoundRect = new RectF(0, 0, mWidth, mHeight - mArrowHeight);
                break;

            case ORIENT_LEFT:
                dstX = 0;
                dstY = Math.min(offset, height);
                //setPadding(0, 0, 0, mArrowHeight);
                //setGravity(Gravity.CENTER);
                mRoundRect = new RectF(mArrowHeight, 0, mWidth, mHeight);
                break;
        }

        matrix.postTranslate(dstX, dstY);
        return matrix;
    }

    public float getBubbleOffset() {
        int width = getMeasuredWidth(), height = getMeasuredHeight();
        float offset = width / 2.0f;
        if (isHorizontal()) {
            // 上下气泡, 箭头水平移动, 计算偏移量
            int hgrav = mArrowGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            if (hgrav == Gravity.CENTER_HORIZONTAL) {
                offset = width / 2.0f + mArrowOffset;
            } else if (hgrav == Gravity.LEFT) {
                offset = Math.max(0, mArrowOffset);
            } else if (hgrav == Gravity.RIGHT) {
                offset = Math.min(width, width - mArrowOffset);
            } else {
                offset = width / 2.0f;
            }
        } else if (isVertical()) {
            // 左右气泡
            int vgrav = mArrowGravity & Gravity.VERTICAL_GRAVITY_MASK;
            if (vgrav == Gravity.CENTER_VERTICAL) {
                offset = height / 2.0f + mArrowOffset;
            } else if (vgrav == Gravity.TOP) {
                offset = Math.max(0, mArrowOffset);
            } else if (vgrav == Gravity.BOTTOM) {
                offset = Math.min(height, height - mArrowOffset);
            } else {
                offset = height / 2.0f;
            }
        }
        return offset;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float offset = getBubbleOffset();
        if (offset <= mArrowHeight) {
            setArrowStyle(ARROW_LEFT);
            setArrowPosition(isHorizontal() ? Gravity.LEFT : Gravity.TOP, 0);
        } else if (offset >= (isHorizontal() ? w : h) - mArrowHeight) {
            setArrowStyle(ARROW_RIGHT);
            setArrowPosition(isHorizontal() ? Gravity.RIGHT : Gravity.BOTTOM, 0);
        }
        applyColor();
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (mArrowOrientation == ORIENT_RIGHT || mArrowOrientation == ORIENT_LEFT) {
            width = Math.max(width, mMinWidth) + mArrowHeight;
            height = Math.max(height, mMinHeight);
        } else {
            width = Math.max(width, mMinWidth);
            height = Math.max(height, mMinHeight) + mArrowHeight;
        }

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, height);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, height);
        }
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Matrix matrix = renderBubbleArrowMatrix(mWidth, mHeight);
        canvas.drawRoundRect(mRoundRect, mCornerRadius, mCornerRadius, mFillPaint);
        mPath.rewind();
        mPath.addPath(mBubbleArrowPath, matrix);
        canvas.drawPath(mPath, mFillPaint);
    }
}