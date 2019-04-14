package com.zlc.work.widget.drawee.drawable;

/**
 * author: liuchun
 * date: 2017/11/7
 */
public interface Rounded {

    void setCircle(boolean isCircle);

    void setRadius(float radius);
    void setRadii(float[] radii);

    void setBorder(int color, float width);
}
