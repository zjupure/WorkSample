package com.zlc.work.widget.drawee.drawable;



/**
 * Interface that enables setting a transform callback.
 *
 * author: liuchun
 * date: 2018/1/30
 */
public interface TransfromableDrawable {

    /**
     * Sets a transform callback.
     *
     * @param transformCallback the transform callback to be set
     */
    void setTransformCallback(TransformCallback transformCallback);
}
