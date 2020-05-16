/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

abstract class SingleAdsorbentListener : RecyclerView.OnScrollListener(){

    /** 获取被吸顶ViewGroup*/
    abstract fun getUiViewGroup():ViewGroup
    /** 获取吸顶View*/
    abstract fun getPinView(): View
    /** 获取吸顶View在RecyclerView中的位置*/
    abstract fun getPinViewPosition():Int
    /** 吸顶的时候 停止滚动并定位在吸顶位置*/
    open fun stopWhenAdsorbent():Boolean = true

    open fun getPinViewLayoutParam():ViewGroup.LayoutParams =
        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val manager = recyclerView.layoutManager
        if(manager is LinearLayoutManager) {
            val first = manager.findFirstVisibleItemPosition()
            val last = manager.findLastVisibleItemPosition()
            //val down = recyclerView.canScrollVertically(1)
            //val up = recyclerView.canScrollVertically(-1)
            val position = getPinViewPosition()
            if (first >= position && dy > 0) {
                addPin2Ui(recyclerView,position)
            } else if (position in (first + 1)..last) {
                addPin2ViewHolder(recyclerView,position-first)
            }
        }
    }

    /** 吸顶*/
    private fun addPin2Ui(recyclerView: RecyclerView,position :Int){
        val uiView = getUiViewGroup()
        val pinView = getPinView()
        if(uiView.indexOfChild(pinView) < 0){
        /** 吸顶的时候 停靠在吸顶viewholder位置，符合吸顶习惯*/
            if(stopWhenAdsorbent()) {
                recyclerView.stopScroll()
                recyclerView.scrollToPosition(position)
            }
            val p = pinView.parent
            if(p is ViewGroup){
                p.removeView(pinView)
            }
            runCatching {
                uiView.addView(pinView, getPinViewLayoutParam())
            }.exceptionOrNull()?.printStackTrace()
        }
    }

    /** 恢复或添加 到ViewHolder*/
    private fun addPin2ViewHolder(recyclerView: RecyclerView,childPosition:Int){
        val uiView = getUiViewGroup()
        val pinView = getPinView()
        val holderView = recyclerView.getChildAt(childPosition)
        if(uiView.indexOfChild(pinView) > 0){
            uiView.removeView(pinView)
        }
        if(holderView is ViewGroup && holderView.indexOfChild(pinView) < 0) {
            runCatching{
                holderView.addView(pinView, getPinViewLayoutParam())
            }.exceptionOrNull()?.printStackTrace()
        }
    }
}