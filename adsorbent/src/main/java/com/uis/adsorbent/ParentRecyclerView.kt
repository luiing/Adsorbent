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
import android.view.View
import android.view.ViewGroup

class ParentRecyclerView :RecyclerView, OnInterceptListener {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var isChildTop = true
    private var startdy = 0f
    private var startdx = 0f
    private var velocity : VelocityTracker? = null
    private var velocityY = 0
    private var isMoveY = false
    private var isSelfTouch = true

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
                if(enableParentChain && SCROLL_STATE_IDLE == newState && !canScrollVertically(1) && velocityY != 0){
                    val manager = layoutManager
                    if(manager is LinearLayoutManager){
                        val first = manager.findFirstVisibleItemPosition()
                        val total = manager.itemCount - 1
                        manager.getChildAt(total - first)?.let {
                            searchViewGroup(recyclerView,it)
                        }
                    }
                }
            }

            /** 采用递归算法遍历*/
            fun searchViewGroup(parent :ViewGroup,child : View):Boolean{
                if(flingChild(parent,child)){
                    return true
                }
                if(child is ViewGroup) {
                    for (i in 0 until child.childCount) {
                        val subChild = child.getChildAt(i)
                        if(subChild is ViewGroup && searchViewGroup(parent,subChild)){
                            return true
                        }
                    }
                }
                return false
            }

            /** 确保child的屏幕坐标在parent内，主要适配ViewPager*/
            fun flingChild(parent :ViewGroup,child : View):Boolean{
                val locChild = IntArray(2)
                val locParent = IntArray(2)
                parent.getLocationOnScreen(locParent)
                child.getLocationOnScreen(locChild)
                val childX = locChild[0]
                val parentX = locParent[0]
                if (childX >= parentX && childX< (parentX+parent.measuredWidth) && child is RecyclerView) {
                    child.fling(0, velocityY/2)
                    return true
                }
                return false
            }
        })
    }

    override fun onTopChild(isTop: Boolean) {
        isChildTop = isTop
    }

    override fun onScrollChain() {
        /** child带动parent联动效果,快速滑动事件下发给self view*/
        if(enableChildChain && isChildTop ) {
            fling(0,velocityY/2)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if(enableConflict){
            dispatchConflictTouchEvent(ev)
        }
        return  super.dispatchTouchEvent(ev)
    }

    private fun dispatchConflictTouchEvent(ev: MotionEvent){
        //Log.e("xx","dispatchConfict action=${ev.action},dy=${ev.y}")
        velocity?:{
            velocity = VelocityTracker.obtain()
        }.invoke()
        velocity?.addMovement(ev)
        when(ev.action){
            MotionEvent.ACTION_DOWN ->{
                startdx = ev.x
                startdy = ev.y
                velocityY = 0
                isMoveY = false
            }
            MotionEvent.ACTION_MOVE ->{
                conflictTouchEvent(ev)
            }
            MotionEvent.ACTION_UP ->{
                isSelfTouch = true
                velocity?.let {
                    it.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                    velocityY = -it.yVelocity.toInt()
                    velocity?.recycle()
                }
                velocity = null
            }
        }
    }


    private fun conflictTouchEvent(ev: MotionEvent){
        /** 纵向滑动处理，横向滑动过滤*/
        if(isMoveY || Math.abs(ev.y-startdy) > Math.abs(ev.x-startdx)){
            isMoveY = true
            /** true 在底部*/
            val isBottom = !canScrollVertically(1)
            /** true向上滑动*/
            val directUp = (ev.y - startdy) < 0
            //Log.e("xx","isBootom=$isBottom,directUp=$directUp,ischildTop=$isChildTop,y=${ev.y}")
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
        if(!isSelfTouch){
            isSelfTouch = true
            dispatchTouchEvent(obtainCancelEvent(ev.x, ev.y))
            dispatchTouchEvent(obtainDownEvent(ev.x, ev.y))
        }
    }

    /** 给child view分发*/
    private fun dispatchChildTouch(ev: MotionEvent){
        if (isSelfTouch){
            isSelfTouch = false
            dispatchTouchEvent(obtainCancelEvent(ev.x,ev.y))
            dispatchTouchEvent(obtainDownEvent(ev.x,ev.y))
            requestDisallowInterceptTouchEvent(true)
        }
    }

    private fun obtainDownEvent(dx :Float, dy :Float) :MotionEvent{
        return obtainActionEvent(MotionEvent.ACTION_DOWN,dx,dy)
    }

    private fun obtainCancelEvent(dx :Float, dy :Float) :MotionEvent{
        return obtainActionEvent(MotionEvent.ACTION_CANCEL,dx,dy)
    }

    private fun obtainActionEvent(action :Int,dx :Float, dy :Float) :MotionEvent{
        val now = SystemClock.uptimeMillis()
        return MotionEvent.obtain(now,now,action,dx,dy,0)
    }
}