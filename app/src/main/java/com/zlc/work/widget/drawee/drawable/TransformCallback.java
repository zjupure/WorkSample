package com.zlc.work.widget.drawee.drawable;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Callback that is used to pass any transformation matrix and the root bounds from a parent
 * drawable to its child.
 *
 * author: liuchun
 * date: 2018/1/30
 */
public interface TransformCallback {

    /**
     * Called when the drawable needs to get all matrices applied to it.
     *
     * @param transform Matrix that is applied to the drawable by the parent drawables.
     */
    void getTransform(Matrix transform);

    /**
     * Called when the drawable needs to get its root bounds.
     *
     * @param bounds The root bounds of the drawable.
     */
    void getRootBounds(RectF bounds);
}
