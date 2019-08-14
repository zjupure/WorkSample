package android.support.v4.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * a workaround ViewPager to support set mOffscreenPageLimit to zero
 * copy some code from {@link ViewPager} and override some logic
 *
 * author: liuchun
 * date: 2019/8/8
 */
public class NoCacheViewPager extends ViewPager {
    private static final String TAG = "ViewPager";
    private static final boolean DEBUG = false;

    private static final int DEFAULT_OFFSCREEN_PAGES = 0;

    private static Field mOffscreenPageLimitField = null;
    private static Field mPopulatePendingField = null;
    private static Field mItemsField = null;
    private static Method mSortChildDrawingOrderMethod = null;
    private static Method mCalculatePageOffsetsMethod = null;

    private ArrayList<ItemInfo> mItems;
    private int mScrollState = SCROLL_STATE_IDLE;
    private int mExpectedAdapterCount;

    private int mTouchSlop;

    private float mLastMotionX;
    private float mLastMotionY;
    private float mInitialMotionX;
    private float mInitialMotionY;

    public NoCacheViewPager(@NonNull Context context) {
        super(context);
        init(context);
    }

    public NoCacheViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mItems = getItems();
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledPagingTouchSlop();
    }

    private ArrayList<ItemInfo> getItems() {
        try {
            if (mItemsField == null) {
                mItemsField = ViewPager.class.getDeclaredField("mItems");
                mItemsField.setAccessible(true);
            }
            return (ArrayList<ItemInfo>) mItemsField.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public void setOffscreenPageLimit(int limit) {
        if (limit > 0) {
            super.setOffscreenPageLimit(limit);
        } else {
            limit = DEFAULT_OFFSCREEN_PAGES;
            try {
                int oldLimit = getOffscreenPageLimit();
                if (limit != oldLimit) {
                    setOffscreenPageLimitByReflect(limit);
                    populate();
                }
            } catch (Exception e) {
                super.setOffscreenPageLimit(limit);
            }
        }
    }

    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        mExpectedAdapterCount = adapter != null ? adapter.getCount() : 0;
        super.setAdapter(adapter);
    }

    @Override
    void dataSetChanged() {
        mExpectedAdapterCount = mAdapter.getCount();
        super.dataSetChanged();
    }

    @Override
    void populate(int newCurrentItem) {
        int offscreenPageLimit = getOffscreenPageLimit();
        if (offscreenPageLimit > 0) {
            super.populate(newCurrentItem);
        } else {
            fixPopulate(newCurrentItem);
        }
    }

    @Override
    void setScrollState(int newState) {
        super.setScrollState(newState);
        if (mScrollState == newState) {
            return;
        }
        mScrollState = newState;
        onScrollStateChanged();
    }

    private void onScrollStateChanged() {
        int pageLimit = getOffscreenPageLimit();
        if (isBeingDragged() && pageLimit <= 0) {
            // 进入拖拽状态，触发一次populate
            populate();
        }
    }

    /**
     * copy from {@link ViewPager#populate(int)}, make some changes to support offScreenLimit zero
     * @param newCurrentItem
     */
    private void fixPopulate(int newCurrentItem) {
        ItemInfo oldCurInfo = null;
        if (mCurItem != newCurrentItem) {
            oldCurInfo = infoForPosition(mCurItem);
            mCurItem = newCurrentItem;
        }

        if (mAdapter == null) {
            sortChildDrawingOrder();
            return;
        }

        // Bail now if we are waiting to populate.  This is to hold off
        // on creating views from the time the user releases their finger to
        // fling to a new position until we have finished the scroll to
        // that position, avoiding glitches from happening at that point.
        if (isPopulatePending()) {
            if (DEBUG) Log.i(TAG, "populate is pending, skipping for now...");
            sortChildDrawingOrder();
            return;
        }

        // Also, don't populate until we are attached to a window.  This is to
        // avoid trying to populate before we have restored our view hierarchy
        // state and conflicting with what is restored.
        if (getWindowToken() == null) {
            return;
        }

        mAdapter.startUpdate(this);

        final int pageLimit = getOffscreenPageLimit();
        final boolean isBeingDragged = isBeingDragged();
        final int startPos = Math.max(0, mCurItem - pageLimit);
        final int N = mAdapter.getCount();
        final int endPos = Math.min(N - 1, mCurItem + pageLimit);

        if (N != mExpectedAdapterCount) {
            String resName;
            try {
                resName = getResources().getResourceName(getId());
            } catch (Resources.NotFoundException e) {
                resName = Integer.toHexString(getId());
            }
            throw new IllegalStateException("The application's PagerAdapter changed the adapter's"
                    + " contents without calling PagerAdapter#notifyDataSetChanged!"
                    + " Expected adapter item count: " + mExpectedAdapterCount + ", found: " + N
                    + " Pager id: " + resName
                    + " Pager class: " + getClass()
                    + " Problematic adapter: " + mAdapter.getClass());
        }

        // Locate the currently focused item or add it if needed.
        int curIndex = -1;
        ItemInfo curItem = null;
        for (curIndex = 0; curIndex < mItems.size(); curIndex++) {
            final ItemInfo ii = mItems.get(curIndex);
            if (ii.position >= mCurItem) {
                if (ii.position == mCurItem) curItem = ii;
                break;
            }
        }

        if (curItem == null && N > 0) {
            curItem = addNewItem(mCurItem, curIndex);
        }

        // Fill 3x the available width or up to the number of offscreen
        // pages requested to either side, whichever is larger.
        // If we have no current item we have no work to do.
        if (curItem != null) {
            float extraWidthLeft = 0.f;
            int itemIndex = curIndex - 1;
            ItemInfo ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
            final int clientWidth = getClientWidth();
            final float maxWidth = pageLimit > 0 || isBeingDragged ? 2.f : 1.f;
            final float leftWidthNeeded = clientWidth <= 0 ? 0 :
                    maxWidth - curItem.widthFactor + (float) getPaddingLeft() / (float) clientWidth;
            for (int pos = mCurItem - 1; pos >= 0; pos--) {
                if (extraWidthLeft >= leftWidthNeeded && pos < startPos) {
                    if (ii == null) {
                        break;
                    }
                    if (pos == ii.position && !ii.scrolling) {
                        mItems.remove(itemIndex);
                        mAdapter.destroyItem(this, pos, ii.object);
                        if (DEBUG) {
                            Log.i(TAG, "populate() - destroyItem() with pos: " + pos
                                    + " instance: " + (ii.object));
                        }
                        itemIndex--;
                        curIndex--;
                        ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
                    }
                } else if (ii != null && pos == ii.position) {
                    extraWidthLeft += ii.widthFactor;
                    itemIndex--;
                    ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
                } else if (pageLimit > 0 || isScrollToRight()){
                    // 只有在pageLimit > 0或者向右滑加载左侧的Item
                    ii = addNewItem(pos, itemIndex + 1);
                    extraWidthLeft += ii.widthFactor;
                    curIndex++;
                    ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
                }
            }

            float extraWidthRight = curItem.widthFactor;
            itemIndex = curIndex + 1;
            if (extraWidthRight <= maxWidth) {
                ii = itemIndex < mItems.size() ? mItems.get(itemIndex) : null;
                final float rightWidthNeeded = clientWidth <= 0 ? 0 :
                        (float) getPaddingRight() / (float) clientWidth + maxWidth;
                for (int pos = mCurItem + 1; pos < N; pos++) {
                    if (extraWidthRight >= rightWidthNeeded && pos > endPos) {
                        if (ii == null) {
                            break;
                        }
                        if (pos == ii.position && !ii.scrolling) {
                            mItems.remove(itemIndex);
                            mAdapter.destroyItem(this, pos, ii.object);
                            if (DEBUG) {
                                Log.i(TAG, "populate() - destroyItem() with pos: " + pos
                                        + " instance: " + (ii.object));
                            }
                            ii = itemIndex < mItems.size() ? mItems.get(itemIndex) : null;
                        }
                    } else if (ii != null && pos == ii.position) {
                        extraWidthRight += ii.widthFactor;
                        itemIndex++;
                        ii = itemIndex < mItems.size() ? mItems.get(itemIndex) : null;
                    } else if (pageLimit > 0 || isScrollToLeft()){
                        // 只有在pageLimit > 0或者向左滑加载右侧的Item
                        ii = addNewItem(pos, itemIndex);
                        itemIndex++;
                        extraWidthRight += ii.widthFactor;
                        ii = itemIndex < mItems.size() ? mItems.get(itemIndex) : null;
                    }
                }
            }

            calculatePageOffsets(curItem, curIndex, oldCurInfo);

            mAdapter.setPrimaryItem(this, mCurItem, curItem.object);
        }

        if (DEBUG) {
            Log.i(TAG, "Current page list:");
            for (int i = 0; i < mItems.size(); i++) {
                Log.i(TAG, "#" + i + ": page " + mItems.get(i).position);
            }
        }

        mAdapter.finishUpdate(this);

        // Check width measurement of current pages and drawing sort order.
        // Update LayoutParams as needed.
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.childIndex = i;
            if (!lp.isDecor && lp.widthFactor == 0.f) {
                // 0 means requery the adapter for this, it doesn't have a valid width.
                final ItemInfo ii = infoForChild(child);
                if (ii != null) {
                    lp.widthFactor = ii.widthFactor;
                    lp.position = ii.position;
                }
            }
        }
        sortChildDrawingOrder();

        if (hasFocus()) {
            View currentFocused = findFocus();
            ItemInfo ii = currentFocused != null ? infoForAnyChild(currentFocused) : null;
            if (ii == null || ii.position != mCurItem) {
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    ii = infoForChild(child);
                    if (ii != null && ii.position == mCurItem) {
                        if (child.requestFocus(View.FOCUS_FORWARD)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private void setOffscreenPageLimitByReflect(int limit) throws Exception {
        if (mOffscreenPageLimitField == null) {
            mOffscreenPageLimitField = ViewPager.class.getDeclaredField("mOffscreenPageLimit");
            mOffscreenPageLimitField.setAccessible(true);
        }
        mOffscreenPageLimitField.set(this, limit);
    }

    private boolean isScrollToLeft() {
        return isBeingDragged() && (mInitialMotionX - mLastMotionX > mTouchSlop);
    }

    private boolean isScrollToRight() {
        return isBeingDragged() && (mLastMotionX - mInitialMotionX > mTouchSlop);
    }

    private boolean isBeingDragged() {
        return mScrollState == SCROLL_STATE_DRAGGING;
    }

    private boolean isPopulatePending() {
        try {
            if (mPopulatePendingField == null) {
                mPopulatePendingField = ViewPager.class.getDeclaredField("mPopulatePending");
                mPopulatePendingField.setAccessible(true);
            }
            return mPopulatePendingField.getBoolean(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void sortChildDrawingOrder() {
        try {
            if (mSortChildDrawingOrderMethod == null) {
                mSortChildDrawingOrderMethod = ViewPager.class.getDeclaredMethod("sortChildDrawingOrder");
                mSortChildDrawingOrderMethod.setAccessible(true);
            }
            mSortChildDrawingOrderMethod.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculatePageOffsets(ItemInfo curItem, int curIndex, ItemInfo oldCurInfo) {
        try {
            if (mCalculatePageOffsetsMethod == null) {
                Class<?>[] paramTypes = new Class<?>[]{ItemInfo.class, int.class, ItemInfo.class};
                mCalculatePageOffsetsMethod = ViewPager.class.getDeclaredMethod("calculatePageOffsets", paramTypes);
                mCalculatePageOffsetsMethod.setAccessible(true);
            }
            mCalculatePageOffsetsMethod.invoke(this, curItem, curIndex, oldCurInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getClientWidth() {
        return getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            // super调用之前记录ACTION_DOWNLOAD的坐标，判断这次是左滑还是右滑事件
            recordTouchEvent(ev);
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            recordTouchEvent(ev);
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void recordTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                break;
            }
        }
    }
}
