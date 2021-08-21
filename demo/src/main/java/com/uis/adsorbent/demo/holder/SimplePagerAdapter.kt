/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent.demo.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.PagerAdapter
import com.uis.adsorbent.ChildRecyclerView
import com.uis.adsorbent.SingleAdsorbentListener
import com.uis.groupadapter.GroupEntity
import com.uis.groupadater.demo.R
import kotlinx.android.synthetic.main.ui_demo.*
import java.util.*

class SimplePagerAdapter : PagerAdapter(){
    val views = LinkedList<ViewGroup>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var view :ViewGroup? = null
        while(views.size > 0 && view == null){
            view = views.removeLast()
        }
        if(view == null){
            val refresh = SwipeRefreshLayout(container.context)//
            val v = ChildRecyclerView(container.context)
            v.layoutManager = LinearLayoutManager(container.context)


            refresh.addView(v)
            view = refresh


            if(v.adapter == null){
                val layoutInflater = LayoutInflater.from(container.context)
                val pin = layoutInflater.inflate(R.layout.ui_view_pin,null)
                val adapter = DemoGroupAdapter()
                for(i in 0 until 50) {
                    adapter.addEntity(GroupEntity(VT_TXT, "ViewPager嵌套RecyclerView item $i"))
                }
                adapter.changePositionEntity(5,GroupEntity(VT_PIN_SINGLE,pin))
                v.adapter = adapter
                v.addOnScrollListener(object : SingleAdsorbentListener(){
                    /** 获取被吸顶ViewGroup*/
                    override fun getUiViewGroup(): ViewGroup = refresh
                    /** 获取吸顶View*/
                    override fun getPinView(): View = pin
                    /** 获取吸顶View在RecyclerView中的位置*/
                    override fun getPinViewPosition(): Int = 5

                    override fun stopWhenAdsorbent(): Boolean = false
                })
            }
            v.scrollToPosition(0)
        }



        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        if(obj is ViewGroup){
            container.removeView(obj)
            views.addFirst(obj)
        }
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean = p0 == p1

    override fun getCount(): Int {
        return 5
    }
}