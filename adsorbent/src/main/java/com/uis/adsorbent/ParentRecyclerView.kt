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
    private var lastdy = 0f
    private var enddy = 0f
    private var lastTouchTime = 0L
    private var needDispatchChild = true
    private var needDispatchSelf = true
    private var scrollValue = ArrayList<Float>(12)

    /** 是否从吸顶后开始滑动*/
    private var isStartSelfBottom = false
    /** 抬手动作记录滚动状态*/
    private var actionUpState = SCROLL_STATE_IDLE

    /** true 开启滑动冲突处理*/
    var enableConflict = true
    /** 开启快速滚动child带动parent联动效果*/
    var enableScrollChain = true

    init {
        /** parent带动child联动，此处违背吸顶原则*/
//        addOnScrollListener(object :OnScrollListener(){
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                Log.e("xx","parent statechanged $newState, isBottom="+!canScrollVertically(1))
//                /** 滚动停止且到了底部,快速滑动事件下发给childview*/
//                if(enableScrollChain && SCROLL_STATE_IDLE == newState && !canScrollVertically(1)
//                    && SCROLL_STATE_DRAGGING == actionUpState && scrollValue.size > 2){
//                    val manager = layoutManager
//                    if(manager is LinearLayoutManager){
//                        var realValue = 0f
//                        for(v in scrollValue){
//                            realValue += v
//                            dispatchChildTouch(manager,obtainMoveEvent(startdx,realValue))
//                        }
//                        dispatchChildTouch(manager,obtainUpEvent(startdx,realValue))
//                    }
//                    scrollValue.clear()
//                }
//            }
//        })
    }

    override fun onTopChild(isTop: Boolean) {
        isChildTop = isTop
        //Log.e("xx","onTopChild $isTop")
    }

    override fun onScrollChain() {
        actionUpState = 0
        if(enableScrollChain && !canScrollVertically(1) && scrollValue.size > 2) {
            //Log.e("xx", "parent onScrollChain...")
            var realValue = 0f
            for (v in scrollValue) {
                realValue += v
                dispatchSelfTouch(obtainMoveEvent(startdx, realValue))
            }
            dispatchSelfTouch(obtainUpEvent(startdx, realValue))
            scrollValue.clear()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return (enableConflict && dispatchConflictTouchEvent(ev)) || super.dispatchTouchEvent(ev)
    }

    private fun dispatchConflictTouchEvent(ev: MotionEvent):Boolean{
        val now = SystemClock.currentThreadTimeMillis()
        when(ev.action){
            MotionEvent.ACTION_DOWN ->{
                startdx = ev.x
                startdy = ev.y
                stopScroll()
                needDispatchChild = true
                needDispatchSelf = true
                isStartSelfBottom = !canScrollVertically(1)
                scrollValue.clear()
                scrollValue.add(startdy)
            }
            MotionEvent.ACTION_MOVE ->{
                scrollValue.add( (ev.y-lastdy)/(now-lastTouchTime))
                if(conflictMoveEvent(ev)){
                    lastdy = ev.y
                    lastTouchTime = now
                    return true
                }
            }
            MotionEvent.ACTION_UP ->{
                actionUpState = scrollState
                enddy = (ev.y-lastdy)/(now-lastTouchTime)
            }
        }
        lastdy = ev.y
        lastTouchTime = now
        //Log.e("xx","parent action= ${ev.action} ,state= $scrollState ,y= ${ev.y}, isBotton="+!canScrollVertically(1))
        return false
    }

    private fun conflictMoveEvent(ev: MotionEvent):Boolean{
        /** 纵向滑动处理，横向滑动过滤*/
        if(Math.abs(ev.y-startdy) > Math.abs(ev.x-startdx)){
            val manager = layoutManager
            if(manager is LinearLayoutManager){
                /** true 在底部*/
                val isBottom = !canScrollVertically(1)
                /** true向上滑动*/
                val directUp = (ev.y - startdy) < 0
                //Log.e("xx","isBottom= $isBottom, directUp= $directUp, isTopChild= $isChildTop, isStartSelfBottom=$isStartSelfBottom, dy=${ev.y}")
                if(isBottom){
                    if(isChildTop && directUp){
                        /** 事件交分发给 child view*/
                        if(isStartSelfBottom) {
                            requestDisallowInterceptTouchEvent(true)
                        }else {
                            dispatchChildTouch(manager,ev)
                            return true
                        }
                    }else if(!isChildTop && directUp){
                        if(!isStartSelfBottom) {
                            dispatchChildTouch(manager,ev)
                            return true
                        }else{
                            requestDisallowInterceptTouchEvent(true)
                        }
                    }else if(isChildTop && !directUp){
                        /** 事件交给self view*/
                        if(isStartSelfBottom){
                            requestDisallowInterceptTouchEvent(false)
                        }else{
                            dispatchSelfTouch(ev)
                        }
                    }else{
                        /** 事件交给child view*/
                        requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
        }
        return false
    }

    private fun dispatchSelfTouch(ev: MotionEvent){
        if(needDispatchSelf) {
            needDispatchSelf = false
            onTouchEvent(obtainDownEvent(ev.x, ev.y))
        }
        onTouchEvent(ev)
    }

    /** 给child view分发*/
    private fun dispatchChildTouch(manager:LinearLayoutManager,ev: MotionEvent){
        val first = manager.findFirstVisibleItemPosition()
        val total = manager.itemCount - 1
        manager.getChildAt(total - first)?.let {
            if (needDispatchChild) {
                needDispatchChild = false
                it.dispatchTouchEvent(obtainDownEvent(ev.x, ev.y))
            }
            it.dispatchTouchEvent(ev)
        }
    }

    private fun obtainDownEvent(dx :Float, dy :Float) :MotionEvent{
        return obtainActionEvent(MotionEvent.ACTION_DOWN,dx,dy)
    }

    private fun obtainMoveEvent(dx :Float, dy :Float) :MotionEvent{
        return obtainActionEvent(MotionEvent.ACTION_MOVE,dx,dy)
    }

    private fun obtainCancelEvent(dx :Float, dy :Float) :MotionEvent{
        return obtainActionEvent(MotionEvent.ACTION_CANCEL,dx,dy)
    }

    private fun obtainUpEvent(dx :Float, dy :Float,mills :Long=0) :MotionEvent{
        return obtainActionEvent(MotionEvent.ACTION_UP,dx,dy)
    }

    private fun obtainActionEvent(action :Int,dx :Float, dy :Float) :MotionEvent{
        val now = SystemClock.uptimeMillis()
        return MotionEvent.obtain(now,now,action,dx,dy,0)
    }
}