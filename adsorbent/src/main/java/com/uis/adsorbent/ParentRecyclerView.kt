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
import android.util.Log
import android.view.*
import kotlin.math.abs
import kotlin.math.max

class ParentRecyclerView :RecyclerView, OnFlingListener {
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var isChildTop = true
    private var childTop = 0
    private var initDownY = 0f
    private var initDownX = 0f
    private var moveDownY = 0f
    private var swipeChildY = 0f
    private var swipeSlideVertical = false
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
    var enableChildSwipeRefresh = false
    private var draggingY = 0
    private var draggingTime = 0L
    private var disallowIntercept = false
    private var cancelTouch = false

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
                    child.onChildFling(speed/2)
                    return true
                }
                return false
            }
        })
    }

    override fun onScrollTop(isTop: Boolean,top:Int) {
        isChildTop = isTop
        childTop = top
    }

    override fun onFling(childSpeed: Int) {
        /** child带动parent联动效果,快速滑动事件下发给self view*/
        if(enableChildChain && isChildTop ) {
            val speed = velocityY-childSpeed
            fling(0,speed/2)
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
        //Log.e("xx","action=${ev.action},dx=${ev.x},dy=${ev.y}")
        if(null == velocity) velocity = VelocityTracker.obtain()
        velocity?.addMovement(ev)
        when(ev.action){
            MotionEvent.ACTION_DOWN ->{
                initDownX = ev.x
                initDownY = ev.y
                moveDownY = initDownY
                swipeSlideVertical = false
                disallowIntercept = false
                velocityY = 0
            }
            MotionEvent.ACTION_MOVE ->{
                conflictTouchEvent(ev)
            }
            MotionEvent.ACTION_UP ->{
                /**fixed刚吸顶会触发点击事件*/
                if(cancelTouch && !swipeSlideVertical && isScrollBottom()){
                    ev.action = MotionEvent.ACTION_CANCEL
                }
                swipeChildY = 0f
                isSelfTouch = true
                cancelTouch = false
                velocity?.let {
                    it.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                    velocityY = -it.getYVelocity(0).toInt()//.yVelocity.toInt()
                    velocity?.recycle()
                }
                velocity = null
            }
            MotionEvent.ACTION_CANCEL->{
                cancelTouch = true
                velocity?.clear()
            }
        }
    }

    private fun conflictTouchEvent(ev: MotionEvent){
        /** 纵向滑动处理，横向滑动过滤*/
        if(!swipeSlideVertical){
            val dy = abs(ev.y-initDownY)
            val dx = abs(ev.x-initDownX)
            if((dx>=mTouchSlop && dy>dx/2) || dy>=mTouchSlop){
                swipeSlideVertical = true
            }
        }
        /** true向上滑动*/
        val directUp = (ev.y - moveDownY) <= 0
        moveDownY = ev.y
        if(enableChildSwipeRefresh && isScrollTop()){
            if (!directUp && !disallowIntercept) {
                swipeChildY = moveDownY
                dispatchChildTouch(ev)
            } else if(  directUp && disallowIntercept && moveDownY < swipeChildY){
                swipeChildY = 0f
                dispatchSelfTouch(ev)
            }
        }else if(isScrollBottom()){
            if(isChildTop && !directUp){
                dispatchSelfTouch(ev,true)
            }else if(swipeSlideVertical && !disallowIntercept){
                dispatchChildTouch(ev)
            }
        }
    }

    private fun isScrollBottom()=!canScrollVertically(1)
    private fun isScrollTop()=!canScrollVertically(-1)

    /** 给selft view分发*/
    private fun dispatchSelfTouch(ev: MotionEvent,force:Boolean=false){
        if(!isSelfTouch || force){
            isSelfTouch = true
            requestDisallowInterceptTouchEvent(false)
        }
    }

    /** 给child view分发*/
    private fun dispatchChildTouch(ev: MotionEvent){
        if (isSelfTouch){
            isSelfTouch = false
            dispatchCancelTouch(ev)
            val top = childTop + (layoutManager?.getChildAt(childCount-1)?.top?:0)
            val dy = max(ev.y, top.toFloat())
            val down = MotionEvent.obtain(ev.downTime,ev.eventTime,MotionEvent.ACTION_DOWN,ev.x,dy,0)
            dispatchTouchEvent(down)
            requestDisallowInterceptTouchEvent(true)
        }
    }

    private fun dispatchCancelTouch(ev: MotionEvent){
        val cancel = MotionEvent.obtain(ev)
        cancel.action = MotionEvent.ACTION_CANCEL
        dispatchTouchEvent(cancel)
    }
}