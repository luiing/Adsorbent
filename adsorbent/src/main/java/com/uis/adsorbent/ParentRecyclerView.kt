/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent

import android.content.Context
import android.os.SystemClock
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

class ParentRecyclerView :RecyclerView, OnInterceptListener {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var isChildTop = true
    private var startdy = 0f
    private var startdx = 0f
    private var enddy = 0f
    private var needDispatchChild = true
    private var needDispatchSelf = true

    /** true 开启滑动冲突处理*/
    var enableConflict = true
    /** 开启快速滚动parent带动child联动效果(默认false)*/
    var enableParentChain = false
    /** 开启快速滚动child带动parent联动效果*/
    var enableChildChain = true

    init {
        addOnScrollListener(object :OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                /** 滚动停止且到了底部,快速滑动事件下发给child view*/
                if(enableParentChain && SCROLL_STATE_IDLE == newState && !canScrollVertically(1)){
                    val manager = layoutManager
                    if(manager is LinearLayoutManager){
                        val first = manager.findFirstVisibleItemPosition()
                        val total = manager.itemCount - 1
                        manager.getChildAt(total - first)?.let {
                            if(it is RecyclerView){
                                it.smoothScrollBy(0,(startdy-enddy).toInt())
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onTopChild(isTop: Boolean) {
        isChildTop = isTop
    }

    override fun onScrollChain() {
        /** child带动parent联动效果,快速滑动事件下发给self view*/
        if(enableChildChain && isChildTop ) {
            smoothScrollBy(0,(startdy-enddy).toInt())
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        enableConflict && dispatchConflictTouchEvent(ev)
        return  super.dispatchTouchEvent(ev)
    }

    private fun dispatchConflictTouchEvent(ev: MotionEvent):Boolean{
        when(ev.action){
            MotionEvent.ACTION_DOWN ->{
                startdx = ev.x
                startdy = ev.y
                enddy = ev.y
                stopScroll()
            }
            MotionEvent.ACTION_MOVE ->{
                conflictTouchEvent(ev)
            }
            MotionEvent.ACTION_UP ->{
                enddy = ev.y
            }
        }
        return false
    }


    private fun conflictTouchEvent(ev: MotionEvent){
        /** 纵向滑动处理，横向滑动过滤*/
        if(Math.abs(ev.y-startdy) > Math.abs(ev.x-startdx)){
            /** true 在底部*/
            val isBottom = !canScrollVertically(1)
            /** true向上滑动*/
            val directUp = (ev.y - startdy) < 0
            if(isBottom){
                if(isChildTop && !directUp){
                    dispatchSelfTouch(ev)
                }else{
                    dispatchChildTouch(ev)
                }
            }else{
                dispatchSelfTouch(ev)
            }
        }
    }

    /** 给selft view分发*/
    private fun dispatchSelfTouch(ev: MotionEvent){
        if(needDispatchSelf){
            needDispatchSelf = false
            if(!needDispatchChild) {
                dispatchTouchEvent(obtainCancelEvent(ev.x, ev.y))
                dispatchTouchEvent(obtainDownEvent(ev.x, ev.y))
            }
        }
        requestDisallowInterceptTouchEvent(false)
        needDispatchChild = true
    }

    /** 给child view分发*/
    private fun dispatchChildTouch(ev: MotionEvent){
        if (needDispatchChild){
            needDispatchChild = false
            dispatchTouchEvent(obtainCancelEvent(ev.x,ev.y))
            dispatchTouchEvent(obtainDownEvent(ev.x,ev.y))
        }
        requestDisallowInterceptTouchEvent(true)
        needDispatchSelf = true
    }

    private fun obtainDownEvent(dx :Float, dy :Float,downTime :Long=0) :MotionEvent{
        return obtainActionEvent(MotionEvent.ACTION_DOWN,dx,dy,downTime)
    }

    private fun obtainMoveEvent(dx :Float, dy :Float,downTime :Long=0) :MotionEvent{
        return obtainActionEvent(MotionEvent.ACTION_MOVE,dx,dy,downTime)
    }

    private fun obtainUpEvent(dx :Float, dy :Float,downTime :Long=0) :MotionEvent{
        return obtainActionEvent(MotionEvent.ACTION_UP,dx,dy,downTime)
    }

    private fun obtainCancelEvent(dx :Float, dy :Float,downTime :Long=0) :MotionEvent{
        return obtainActionEvent(MotionEvent.ACTION_CANCEL,dx,dy,downTime)
    }

    private fun obtainActionEvent(action :Int,dx :Float, dy :Float,downTime :Long=0) :MotionEvent{
        val now = SystemClock.uptimeMillis()
        return MotionEvent.obtain(if(downTime<=0)now else downTime,now,action,dx,dy,0)
    }
}