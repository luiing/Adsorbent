/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import kotlin.math.max

class ChildRecyclerView : RecyclerView{
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    /** true 开启滑动冲突处理*/
    var enableConflict = true
    private var draggingY = 0
    private var draggingTime = 0L
    /** 记录上次的parent,避免递归频繁*/
    private var parentView :WeakReference<IFling>? = null

    init {
        addOnScrollListener(object :OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                /** 滚动停止且到了顶部,快速滑动事件往上给parent view*/
                if(enableConflict && SCROLL_STATE_IDLE == newState) {
                    induceParentOfChildTopStatus()
                    val step = (System.currentTimeMillis()-draggingTime).toInt()
                    val speed = 1000*draggingY/max(1000,step)
                    parentView?.get()?.onFling(speed)
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
        (parentView?.get() ?: run {
            var pv = parent
            while (pv != null) {
                if (pv is IFling) {
                    parentView = WeakReference(pv)
                    break
                }
                pv = pv.parent
            }
            (pv as? IFling)
        })?.onStatus(!canScrollVertically(-1),top)
    }
}