package com.uis.groupadater.demo.adsorbent

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

    fun getPinViewLayoutParam():ViewGroup.LayoutParams =
        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val manager = recyclerView.layoutManager
        if(manager is LinearLayoutManager) {
            val first = manager.findFirstVisibleItemPosition()
            val last = manager.findLastVisibleItemPosition()
            //val down = recyclerView.canScrollVertically(1)
            //val up = recyclerView.canScrollVertically(-1)
            //Log.e("xx", "first=$first, vertically down=$down, up=$up, dy=$dy")
            val position = getPinViewPosition()
            if (first >= position && dy > 0) {
                addPin2Ui()
            } else if (position in (first + 1)..last) {
                addPin2ViewHolder(recyclerView,position-first)
            }
        }
    }

    /** 吸顶*/
    private fun addPin2Ui(){
        val uiView = getUiViewGroup()
        val pinView = getPinView()
        if(uiView.indexOfChild(pinView) < 0){
            val p = pinView.parent
            if(p is ViewGroup){
                p.removeView(pinView)
            }
            uiView.addView(pinView, getPinViewLayoutParam())
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
            holderView.addView(pinView, getPinViewLayoutParam())
        }
    }
}