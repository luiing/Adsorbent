/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.*
import kotlin.math.abs
import kotlin.math.max

class ParentRecyclerView :RecyclerView, OnFlingListener {
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var isChildTop = true
    private var startDy = 0f
    private var startDx = 0f
    private var swipSlideVertical = false
    private var velocity : VelocityTracker? = null
    private var velocityY = 0
    private var isSelfTouch = true
    private var mTouchSlop = 0

    /** true 开启滑动冲突处理*/
    var enableConflict = true
    /** 开启快速滚动parent带动child联动效果(默认false)*/
    var enableParentChain = false
    /** 开启快速滚动child带动parent联动效果*/
    var enableChildChain = true
    private var draggingY = 0
    private var draggingTime = 0L
    private var disallowIntercept = false

    init {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        addOnScrollListener(object :OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                /** 滚动停止且到了底部,快速滑动事件下发给child view*/
                if(enableParentChain && SCROLL_STATE_IDLE == newState && isScrollBottom() && velocityY != 0 && isChildTop){
                    val manager = layoutManager
                    if(manager is LinearLayoutManager){
                        val first = manager.findFirstVisibleItemPosition()
                        val total = manager.itemCount - 1
                        manager.getChildAt(total - first)?.let {
                            searchViewGroup(recyclerView,it)
                        }
                    }
                }else if(SCROLL_STATE_DRAGGING == newState){
                    draggingY = 0
                    draggingTime = System.currentTimeMillis()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(SCROLL_STATE_DRAGGING == scrollState) {
                    draggingY += dy
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
                if (childX >= parentX && childX< (parentX+parent.measuredWidth) && child is OnChildFlingListener) {
                    val step = (System.currentTimeMillis()-draggingTime).toInt()
                    val speed = velocityY - 1000*draggingY/max(1000,step)
                    child.onChildFling(speed)
                    return true
                }
                return false
            }
        })
    }

    override fun onScrollTop(isTop: Boolean) {
        isChildTop = isTop
    }

    override fun onFling(childSpeed: Int) {
        /** child带动parent联动效果,快速滑动事件下发给self view*/
        if(enableChildChain && isChildTop ) {
            fling(0,velocityY-childSpeed)
        }
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
        this.disallowIntercept = disallowIntercept
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if(enableConflict){
            dispatchConflictTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun dispatchConflictTouchEvent(ev: MotionEvent){
        velocity = velocity?:VelocityTracker.obtain()
        velocity?.addMovement(ev)
        when(ev.action){
            MotionEvent.ACTION_DOWN ->{
                startDx = ev.x
                startDy = ev.y
                swipSlideVertical = false
                disallowIntercept = false
                velocityY = 0
            }
            MotionEvent.ACTION_MOVE ->{
                conflictTouchEvent(ev)
            }
            MotionEvent.ACTION_UP ->{
                /**fixed刚吸顶会触发点击事件*/
                if(!isUseTouchSlop(ev) && !swipSlideVertical && isScrollBottom()){
                    ev.action = MotionEvent.ACTION_CANCEL
                }
                isSelfTouch = true
                velocity?.let {
                    it.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                    velocityY = -it.yVelocity.toInt()
                    velocity?.recycle()
                }
                velocity = null
            }
            MotionEvent.ACTION_CANCEL->velocity?.clear()
        }
    }

    private fun isUseTouchSlop(ev: MotionEvent):Boolean{
        val isClicked = startDx == ev.x && startDy == ev.y
        val isTouchSlop = abs(startDx-ev.x)>mTouchSlop || abs(startDy-ev.y)>mTouchSlop
        return isClicked || isTouchSlop
    }

    private fun conflictTouchEvent(ev: MotionEvent){
        /** 纵向滑动处理，横向滑动过滤*/
        if(!swipSlideVertical){
            val dy = abs(ev.y-startDy)
            val dx = abs(ev.x-startDx)
            if((dx>mTouchSlop && dy>dx/2) || dy>mTouchSlop){
                swipSlideVertical = true
            }
        }
        /** true向上滑动*/
        val directUp = (ev.y - startDy) < 0
        if(isScrollBottom()){
            if(isChildTop && !directUp){
                dispatchSelfTouch(ev)
            }else if(swipSlideVertical && !disallowIntercept){
                dispatchChildTouch(ev)
            }
        }else{
            dispatchSelfTouch(ev)
        }
    }

    private fun isScrollBottom()=!canScrollVertically(1)

    /** 给selft view分发*/
    private fun dispatchSelfTouch(ev: MotionEvent){
        if(!isSelfTouch){
            isSelfTouch = true
            requestDisallowInterceptTouchEvent(false)
        }
    }

    /** 给child view分发*/
    private fun dispatchChildTouch(ev: MotionEvent){
        if (isSelfTouch){
            isSelfTouch = false
            cancelTouchEvent(ev)
            val dy = max(ev.y, layoutManager?.getChildAt(childCount-1)?.top?.toFloat() ?:0f)
            val down = MotionEvent.obtain(ev.downTime,ev.eventTime,MotionEvent.ACTION_DOWN,ev.x,dy,0)
            dispatchTouchEvent(down)
            requestDisallowInterceptTouchEvent(true)
        }
    }

    private fun cancelTouchEvent(ev: MotionEvent){
        val cancel = MotionEvent.obtain(ev)
        cancel.action = MotionEvent.ACTION_CANCEL
        onTouchEvent(cancel)
    }
}