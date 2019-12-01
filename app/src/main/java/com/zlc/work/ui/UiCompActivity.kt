package com.zlc.work.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import com.zlc.work.R
import com.zlc.work.widget.bubble.BubblePopupWindow

/**
 *
 * author: liuchun
 * date: 2019-11-30
 */
class UiCompActivity : AppCompatActivity(), View.OnClickListener {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ui_comp)

        findViewById<Button>(R.id.bubble_anchor).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.bubble_anchor) {
            showBubble(v)
        }
    }

    fun showBubble(view: View) {
        val window = BubblePopupWindow(this)
        window.show(view, Gravity.TOP)
    }
}