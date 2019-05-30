/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent.demo.holder

import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.uis.adsorbent.ChildRecyclerView
import com.uis.groupadapter.GroupEntity
import java.util.*

class SimplePagerAdapter : PagerAdapter(){
    val views = LinkedList<ViewGroup>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var view :ViewGroup? = null
        while(views.size > 0 && view == null){
            view = views.removeLast()
        }
        if(view == null){
            val v = ChildRecyclerView(container.context)
            v.layoutManager = LinearLayoutManager(container.context)
            view = v
        }
        view as RecyclerView
            if(view.adapter == null){
                val adapter = DemoGroupAdapter()
                for(i in 0 until 50) {
                    adapter.addEntity(GroupEntity(VT_TXT, "ViewPager嵌套RecyclerView item $i"))
                }
                view.adapter = adapter
            }
        view.scrollToPosition(0)


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