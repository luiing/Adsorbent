package com.uis.groupadater.demo.adsorbent

import android.content.Context
import android.os.SystemClock
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent

class ParentRecyclerView :RecyclerView,OnInterceptListener{

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

    private var lastdy = 0f

    /** true 开启滑动冲突处理*/
    var enableConflict = true

    override fun onTopChild(isTop: Boolean) {
        isChildTop = isTop
        //Log.e("xx","onTopChild $isTop")
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return dispatchConflictTouchEvent(ev) || super.dispatchTouchEvent(ev)
    }

    private fun dispatchConflictTouchEvent(ev: MotionEvent):Boolean{
        if(!enableConflict) return false
        when(ev.action){
            MotionEvent.ACTION_DOWN ->{
                startdx = ev.x
                startdy = ev.y
                lastdy = ev.y
                stopScroll()
                needDispatchChild = true
                needDispatchSelf = true
                isStartSelfBottom = !canScrollVertically(1)
            }
            MotionEvent.ACTION_MOVE ->{
                if(conflictMoveEvent(ev)){
                    return true
                }
            }
        }
        return false
    }

    private fun conflictMoveEvent(ev: MotionEvent):Boolean{
        /** 纵向滑动处理，横向滑动过滤*/
        if(Math.abs(ev.y-startdy) > Math.abs(ev.x-startdx)){
            lastdy = ev.y
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
                            dispatchChildTouch(manager,ev,directUp)
                            return true
                        }
                    }else if(!isChildTop && directUp){
                        if(!isStartSelfBottom) {
                            dispatchChildTouch(manager,ev,directUp)
                            return true
                        }else{
                            requestDisallowInterceptTouchEvent(true)
                        }
                    }else if(isChildTop && !directUp){
                        /** 事件交给self view*/
                        if(isStartSelfBottom){
                            requestDisallowInterceptTouchEvent(false)
                        }else{
                            dispatchSelfTouch(ev,directUp)
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

    private fun dispatchSelfTouch(ev: MotionEvent,directUp :Boolean){
        if(needDispatchSelf) {
            needDispatchSelf = false
            val v = if(directUp) -20 else 20
            onTouchEvent(obtainDownEvent(ev.x, lastdy))
            onTouchEvent(obtainMoveEvent(ev.x, lastdy + v))
        }
        onTouchEvent(ev)
    }

    /** 给child view分发*/
    private fun dispatchChildTouch(manager:LinearLayoutManager,ev: MotionEvent,directUp :Boolean){
        val first = manager.findFirstVisibleItemPosition()
        val total = manager.itemCount - 1
        manager.getChildAt(total - first)?.let {
            if (needDispatchChild) {
                needDispatchChild = false
                val v = if(directUp) -20 else 20
                it.dispatchTouchEvent(obtainDownEvent(ev.x, lastdy))
                it.dispatchTouchEvent(obtainMoveEvent(ev.x, lastdy + v))
            }
            it.dispatchTouchEvent(ev)
        }
    }

    private fun obtainDownEvent(dx :Float, dy :Float) :MotionEvent{
        val now = SystemClock.uptimeMillis() - 10
        return MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,dx,dy,0)
    }

    private fun obtainMoveEvent(dx :Float, dy :Float) :MotionEvent{
        val now = SystemClock.uptimeMillis() - 10
        return MotionEvent.obtain(now,now,MotionEvent.ACTION_MOVE,dx,dy,0)
    }

    private fun obtainCancelEvent(dx :Float, dy :Float) :MotionEvent{
        val now = SystemClock.uptimeMillis()
        return MotionEvent.obtain(now,now,MotionEvent.ACTION_CANCEL,dx,dy,0)
    }
}