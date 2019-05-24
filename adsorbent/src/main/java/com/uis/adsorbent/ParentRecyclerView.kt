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
import android.view.VelocityTracker

class ParentRecyclerView :RecyclerView, OnInterceptListener {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var isChildTop = true
    private var startdy = 0f
    private var startdx = 0f
    private var needDispatchChild = true
    private var needDispatchSelf = true
    /** 是否从吸顶后开始滑动*/
    private var isStartSelfBottom = false
    private var velocity :VelocityTracker? = null
    private var verticalSpeed = 0f

    /** true 开启滑动冲突处理*/
    var enableConflict = true
    /** 开启快速滚动parent带动child联动效果(默认false)*/
    var enableParentChain = false
    /** 开启快速滚动child带动parent联动效果*/
    var enableChildChain = true

    init {
        /** parent带动child联动，此处违背吸顶原则*/
        addOnScrollListener(object :OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                /** 滚动停止且到了底部,快速滑动事件下发给childview*/
                if(enableParentChain && SCROLL_STATE_IDLE == newState && !canScrollVertically(1)
                    && Math.abs(verticalSpeed) >= maxFlingVelocity/2){
                    val manager = layoutManager
                    if(manager is LinearLayoutManager){
                        val speed = Math.signum(verticalSpeed)*200
                        var dy = 2000f
                        for(i in 0 until 4){
                            dispatchChildTouch(manager,obtainMoveEvent(startdx,dy))
                            dy += speed
                        }
                        dispatchChildTouch(manager,obtainUpEvent(startdx,dy))
                    }
                }
            }
        })
    }

    override fun onTopChild(isTop: Boolean) {
        isChildTop = isTop
    }

    override fun onScrollChain() {
        if(enableChildChain && isStartSelfBottom && Math.abs(verticalSpeed) >= maxFlingVelocity) {
            val speed = Math.signum(verticalSpeed)*200
            var dy = 1000f
            for(i in 0 until 4){
                dispatchSelfTouch(obtainMoveEvent(startdx, dy))
                dy += speed
            }
            dispatchSelfTouch(obtainUpEvent(startdx, dy))
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return (enableConflict && dispatchConflictTouchEvent(ev)) || super.dispatchTouchEvent(ev)
    }

    private fun dispatchConflictTouchEvent(ev: MotionEvent):Boolean{
        if(velocity == null){
            velocity = VelocityTracker.obtain()
        }
        velocity?.addMovement(ev)
        when(ev.action){
            MotionEvent.ACTION_DOWN ->{
                startdx = ev.x
                startdy = ev.y
                stopScroll()
                needDispatchChild = true
                needDispatchSelf = true
                isStartSelfBottom = !canScrollVertically(1)
                verticalSpeed = 0f
            }
            MotionEvent.ACTION_MOVE ->{
                if(conflictMoveEvent(ev)){
                    return true
                }
            }
            MotionEvent.ACTION_UP ->{
                velocity?.let {
                    it.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                    verticalSpeed = it.getYVelocity()
                }
                velocity?.recycle()
                velocity = null
            }
        }
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
            SystemClock.sleep(2)
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
                SystemClock.sleep(2)
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