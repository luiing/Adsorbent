/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent

class ChildRecyclerView :RecyclerView{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    var enableConflict = true

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        /** true child在顶部*/
        if(enableConflict) {
            val isChildTop = !canScrollVertically(-1)
            var pv = parent
            while (pv != null) {
                if (pv is OnInterceptListener) {
                    pv.onTopChild(isChildTop)
                    break
                }
                pv = pv.parent
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}