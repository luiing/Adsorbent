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
    /** 抬手动作记录滚动状态*/
    private var actionUpState = SCROLL_STATE_IDLE
    /** 记录上次的parent,避免递归频繁*/
    private var parentView :WeakReference<OnInterceptListener>? = null

    init {
        addOnScrollListener(object :OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //Log.e("xx","child statechanged $newState, isTop="+!canScrollVertically(-1))
                /** 滚动停止且到了顶部,快速滑动事件往上给parent view*/
                if(SCROLL_STATE_IDLE == newState && !canScrollVertically(-1) && SCROLL_STATE_DRAGGING == actionUpState){
                    parentView?.get()?.let {
                        it.onScrollChain()
                    }
                }
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        /** true child在顶部*/
        if(enableConflict) {
            induceParentOfChildTopStatus()
            when(ev.action){
                MotionEvent.ACTION_DOWN ->{
                    actionUpState = SCROLL_STATE_IDLE
                }
                MotionEvent.ACTION_UP -> {
                    actionUpState = scrollState
                }
            }
        }
        //Log.e("xx","child action ${ev.action} state $scrollState,y= ${ev.y}")
        return super.dispatchTouchEvent(ev)
    }

    private fun induceParentOfChildTopStatus(){
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