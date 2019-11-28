package com.zlc.work.widget.bubble;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;


public class BubblePopupWindow extends PopupWindow {

    private boolean mCreated = false;
    protected BubbleLinearLayout mBubbleView;
    protected Context mContext;
    private TextView mTextView;
    private int mWidth;
    private int mHeight;
    // 是否显示虚拟键盘
    private boolean isHideVirtualKey;
    private int mMargin;
    //默认箭头中间，可以自己设定偏移量
    private int mXOffset;
    //默认箭头中间，可以自己设定偏移量
    private int mYOffset;
    private boolean isAlreadyDismiss;
    private int mGravity;
    private int mArrowGravity = Gravity.CENTER;
    private float mArrowOffset = 0;
    private boolean mAutoDismiss = true;
    private long mDisplayTime = 5000L;

    private DisplayMetrics mDisplay = new DisplayMetrics();

    private AnimatorSet set;

    private Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            animatorEasyInOut(false, mGravity);
        }
    };

    public BubblePopupWindow(Context context) {
        super(context);
        this.mContext = context;
        mMargin = (int) dip2px(context, 3);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(false);
        setOutsideTouchable(false);
        setClippingEnabled(false);

        ColorDrawable dw = new ColorDrawable(0);
        setBackgroundDrawable(dw);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(mDisplay);
    }

    protected void onCreate() {
        View contentView = createContentView();
        setContentView(contentView);
    }

    protected View createContentView() {
        View child = createBubbleView();

        mBubbleView = new BubbleLinearLayout(mContext);
        mBubbleView.setBackgroundColor(Color.TRANSPARENT);
        mBubbleView.addView(child);
        mBubbleView.setGravity(Gravity.CENTER);
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mBubbleView.setLayoutParams(layoutParams);
        mBubbleView.setVisibility(View.GONE);
        mBubbleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return mBubbleView;
    }

    /**
     * 默认的布局，就一个textview，添加文字
     */
    protected View createBubbleView() {
        mTextView = new TextView(mContext);
        mTextView.setTextColor(0xffffffff);
        mTextView.setTextSize(13);
        mTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mTextView.setLines(1);
        mTextView.setGravity(Gravity.CENTER);
        isHideVirtualKey = true;
        return mTextView;
    }

    /**
     * 设置显示的view
     *
     * @param view 气泡中需要显示的view
     */
    public void setBubbleView(View view) {
        mBubbleView.removeAllViews();
        mBubbleView.addView(view);
    }

    private void measureContent() {
        int widthMeasureSpec, heightMeasureSpec;
        if (mWidth > 0) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(mWidth, View.MeasureSpec.EXACTLY);
        } else {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(mDisplay.widthPixels, View.MeasureSpec.AT_MOST);
        }
        if (mHeight > 0) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(mHeight, View.MeasureSpec.EXACTLY);
        } else {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(mDisplay.heightPixels, View.MeasureSpec.AT_MOST);
        }
        View contentView = getContentView();
        if (contentView != null) {
            contentView.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 隐藏虚拟键盘
     */
    private void hideStatusBar() {
        if (!isHideVirtualKey) {
            return;
        }
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT < 19) { // lower api
            getContentView().setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            getContentView().setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * @param b 是否显示虚拟键盘
     */
    public void setHideVirtualKey(boolean b) {
        isHideVirtualKey = b;
    }

    /**
     * 设置布局的大小
     *
     * @param width  宽度
     * @param height 高度
     */
    public void setLayoutSize(int width, int height) {
        mWidth = width;
        mHeight = height;
        setWidth(width);
        setHeight(height);
    }

    public void setBubbleText(String showText) {
        if (mTextView != null) {
            mTextView.setText(showText);
        }
    }

    public void setBubbleText(int showTextResId) {
        if (mTextView != null) {
            mTextView.setText(showTextResId);
        }
    }

    public void setAutoDismiss(boolean autoDismiss) {
        this.mAutoDismiss = autoDismiss;
    }

    public void setDisplayTime(long time) {
        this.mDisplayTime = time;
    }

    public void show(View parent) {
        show(parent, Gravity.BOTTOM, mArrowGravity, mArrowOffset);
    }

    public void show(View parent, int gravity) {
        show(parent, gravity, mArrowGravity, mArrowOffset);
    }

    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }

    public void setArrowParam(int gravity, float offset) {
        this.mArrowGravity = gravity;
        this.mArrowOffset = offset;
    }

    public void setOffset(int xOffset, int yOffset) {
        this.mXOffset = xOffset;
        this.mYOffset = yOffset;
    }

    public void setXOffset(int xOffset) {
        this.mXOffset = xOffset;
    }

    public void setYOffset(int yOffset) {
        this.mYOffset = yOffset;
    }

    public void setMargin(int margin) {
        this.mMargin = margin;
    }

    void dispatchOnCreate() {
        if (!mCreated) {
            onCreate();
            mCreated = true;
        }
        measureContent();
    }

    /**
     * 根据箭头的方向调整其他布局的位置
     * 比如带icon图的ImageView需要调整下面的margin，保证对齐
     */
    protected void adjustLayout() {

    }

    public int getBubbleOrientation() {
        int orientation = BubbleLinearLayout.ORIENT_BOTTOM;
        switch (mGravity) {
            case Gravity.BOTTOM:
                orientation = BubbleLinearLayout.ORIENT_TOP;
                break;
            case Gravity.TOP:
                orientation = BubbleLinearLayout.ORIENT_BOTTOM;
                break;
            case Gravity.RIGHT:
            case Gravity.END:
                orientation = BubbleLinearLayout.ORIENT_LEFT;
                break;
            case Gravity.LEFT:
            case Gravity.START:
                orientation = BubbleLinearLayout.ORIENT_RIGHT;
                break;
            default:
                break;
        }
        return orientation;
    }

    private void reset() {
        if (set != null) {
            set.cancel();
            set.removeAllListeners();
        }
        View contentView = getContentView();
        if (contentView != null) {
            contentView.removeCallbacks(mDismissRunnable);
        }
    }

    /**
     * @param parent       展示的锚点view
     * @param gravity      相对锚点view的方向
     * @param arrowGravity 箭头对齐的方向
     * @param arrowOffset  相对arrowGravity的偏移量
     */
    public void show(View parent, int gravity, int arrowGravity, float arrowOffset) {
        reset();
        setGravity(gravity);
        setArrowParam(arrowGravity, arrowOffset);
        if (isShowing()) {
            dismissInternal();
            return;
        }

        dispatchOnCreate();

        int orientation = getBubbleOrientation();
        mBubbleView.setArrowOrientation(orientation);
        mBubbleView.setArrowPosition(arrowGravity, arrowOffset);
        // 调整其他布局
        adjustLayout();
        // 设置箭头方向之后需要重新measure, 因为padding变化了
        measureContent();

        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        hideStatusBar();
        float middleSize = 0f;
        float realOffset = mBubbleView.getBubbleOffset();
        switch (gravity) {
            case Gravity.BOTTOM:
                //设置进入退出动画
                middleSize = (parent.getMeasuredWidth() - getMeasuredWidth()) / 2.0f;
                middleSize -= (realOffset - getMeasuredWidth() / 2.0f);

                //showAsDropDown(parent, mXOffset + (int)middleSize, mMargin + mYOffset);
                showAtLocation(parent, Gravity.NO_GRAVITY, location[0] + mXOffset + (int) middleSize, location[1] + parent.getMeasuredHeight() + mYOffset + mMargin);
                animatorEasyInOut(true, gravity);
                break;
            case Gravity.TOP:
                middleSize = (parent.getMeasuredWidth() - getMeasuredWidth()) / 2.0f;
                middleSize -= (realOffset - getMeasuredWidth() / 2.0f);

                showAtLocation(parent, Gravity.NO_GRAVITY, location[0] + mXOffset + (int) middleSize, location[1] - getMeasureHeight() + mYOffset - mMargin);
                animatorEasyInOut(true, gravity);
                break;
            case Gravity.RIGHT:
            case Gravity.END:
                middleSize = (parent.getMeasuredHeight() - getMeasureHeight()) / 2.0f;
                middleSize -= (realOffset - getMeasureHeight() / 2.0f);

                showAtLocation(parent, Gravity.NO_GRAVITY, location[0] + mXOffset + parent.getMeasuredWidth() + mMargin, location[1] + mYOffset + (int) middleSize);
                animatorEasyInOut(true, gravity);
                break;
            case Gravity.LEFT:
            case Gravity.START:
                middleSize = (parent.getMeasuredHeight() - getMeasureHeight()) / 2.0f;
                middleSize -= (realOffset - getMeasureHeight() / 2.0f);

                showAtLocation(parent, Gravity.NO_GRAVITY, location[0] + mXOffset - getMeasuredWidth() - mMargin, location[1] + mYOffset + (int) middleSize);
                animatorEasyInOut(true, gravity);
                break;
            default:
                break;
        }
        isAlreadyDismiss = false;
        if (mAutoDismiss) {
            getContentView().postDelayed(mDismissRunnable, mDisplayTime);
        }
    }

    public void showAtLocation(View parent, int gravity) {
        showAtLocation(parent, gravity, mArrowGravity, mArrowOffset);
    }

    /**
     * @param parent       展示的锚点view, 可以是任意位置
     * @param gravity      相对锚点view的方向
     * @param arrowGravity 箭头对齐的方向
     * @param arrowOffset  相对arrowGravity的偏移量
     */
    public void showAtLocation(View parent, int gravity, int arrowGravity, float arrowOffset) {
        reset();
        setArrowParam(arrowGravity, arrowOffset);
        if (isShowing()) {
            dismissInternal();
            return;
        }

        dispatchOnCreate();

        int orientation = getBubbleOrientation();
        mBubbleView.setArrowOrientation(orientation);
        mBubbleView.setArrowPosition(arrowGravity, arrowOffset);
        // 调整其他布局
        adjustLayout();
        // 设置箭头方向之后需要重新measure, 因为padding变化了
        measureContent();

        showAtLocation(parent, gravity, mXOffset, mYOffset);
        animatorEasyInOut(true, mGravity);

        isAlreadyDismiss = false;
        if (mAutoDismiss) {
            getContentView().postDelayed(mDismissRunnable, mDisplayTime);
        }
    }

    private void animatorEasyInOut(final boolean isIn, final int gravity) {
        if (!isShowing()) {
            return;
        }
        final View contentView = getContentView();
        if (!isIn) {
            isAlreadyDismiss = true;
        }
        if (set == null) {
            set = new AnimatorSet();
        }
        contentView.post(new Runnable() {
            @Override
            public void run() {
                if (!isShowing() || set == null) {
                    return;
                }
                final View view = mBubbleView;
                int pivotX = 0;
                int pivotY = 0;
                switch (gravity) {
                    case Gravity.BOTTOM:
                        pivotX = (int) (view.getX() + mBubbleView.getBubbleOffset());
                        pivotY = (int) (view.getY());
                        break;
                    case Gravity.TOP:
                        pivotX = (int) (view.getX() + mBubbleView.getBubbleOffset());
                        pivotY = (int) (view.getY() + view.getMeasuredHeight());
                        break;
                    case Gravity.RIGHT:
                        pivotX = (int) view.getX();
                        pivotY = (int) (view.getY() + mBubbleView.getBubbleOffset());
                        break;
                    case Gravity.LEFT:
                        pivotX = (int) (view.getX() + view.getMeasuredWidth());
                        pivotY = (int) (view.getY() + mBubbleView.getBubbleOffset());
                        break;
                    default:
                        break;
                }
                contentView.setPivotY(pivotY);
                contentView.setPivotX(pivotX);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(contentView, "scaleX", isIn ? 0 : 1f, isIn ? 1.00f : 0, isIn ? 1f : 0);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(contentView, "scaleY", isIn ? 0 : 1f, isIn ? 1.00f : 0, isIn ? 1f : 0);

                set.play(scaleX).with(scaleY);
                set.setDuration(isIn ? 800 : 200);
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isIn) {
                            contentView.setVisibility(View.GONE);
                            dismissInternal();
                        }
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        if (isIn) {
                            contentView.setVisibility(View.VISIBLE);
                        }
                    }
                });
                set.start();
            }
        });
    }

    private void dismissInternal() {
        // 多次调用dismiss导致已经dettach了
        if (!isShowing()) {
            return;
        }
        try {
            super.dismiss();
        } catch (WindowManager.BadTokenException e) {
            // ignore
        } catch (IllegalArgumentException e) {
            // java.lang.IllegalArgumentException: View=android.widget.PopupWindow$PopupViewContainer{c727b76 V.E..... ......ID 0,0-267,65} not attached to window manager
        }
    }

    @Override
    public void dismiss() {
        if (!isShowing()) {
            return;
        }

        if (!isAlreadyDismiss) {
            animatorEasyInOut(false, mGravity);
            getContentView().removeCallbacks(mDismissRunnable);
            mXOffset = 0;
            mYOffset = 0;
        }
    }

    private int getBubbleHeight() {
        return mBubbleView.getMeasuredHeight();
    }

    private int getBubbleWidth() {
        return mBubbleView.getMeasuredWidth();
    }

    public int getArrowHeight() {
        if (mBubbleView != null) {
            return mBubbleView.getArrowHeight();
        }
        return 0;
    }

    /**
     * @return 测量高度
     */
    public int getMeasureHeight() {
        return getContentView().getMeasuredHeight();
    }

    /**
     * @return 测量宽度
     */
    public int getMeasuredWidth() {
        return getContentView().getMeasuredWidth();
    }

    /**
     * 在结束时，需要调用此方法，防止内崔泄漏
     */
    public void onDestroy() {
        if (set != null) {
            set.cancel();
            set = null;
        }
        dismissInternal();
    }

    public static float dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }
}