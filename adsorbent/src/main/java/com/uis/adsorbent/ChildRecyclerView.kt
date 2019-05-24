/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import java.lang.ref.WeakReference

class ChildRecyclerView :RecyclerView{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    /** true 开启滑动冲突处理*/
    var enableConflict = true
    /** 记录上次的parent,避免递归频繁*/
    private var parentView :WeakReference<OnInterceptListener>? = null

    init {
        addOnScrollListener(object :OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                /** 滚动停止且到了顶部,快速滑动事件往上给parent view*/
                if(SCROLL_STATE_IDLE == newState && !canScrollVertically(-1)){
                    parentView?.get()?.onScrollChain()
                }
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if(enableConflict) {
            induceParentOfChildTopStatus()
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun induceParentOfChildTopStatus(){
        /** true child在顶部*/
        val isChildTop = !canScrollVertically(-1)
        parentView?.get()?.let {
            it.onTopChild(isChildTop)
            return
        }
        var pv = parent
        while (pv != null) {
            if (pv is OnInterceptListener) {
                pv.onTopChild(isChildTop)
                parentView = WeakReference(pv)
                break
            }
            pv = pv.parent
        }
    }
}