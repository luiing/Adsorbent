package com.uis.groupadater.demo.holder

import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.uis.groupadapter.GroupEntity
import com.uis.groupadater.demo.adsorbent.ChildRecyclerView
import java.util.*

class SimplePagerAdapter : PagerAdapter(){
    val views = LinkedList<RecyclerView>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var view :RecyclerView? = null
        while(views.size > 0 && view == null){
            view = views.removeLast()
        }
        if(view == null){
            view = ChildRecyclerView(container.context)
            view.layoutManager = LinearLayoutManager(container.context)
        }
        if(view.adapter == null){
            val adapter = DemoGroupAdapter()
            for(i in 0 until 50) {
                adapter.addEntity(GroupEntity(VT_TXT, "ViewPager嵌套RecyclerView item $i"))
            }
            view.adapter = adapter
        }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        if(obj is RecyclerView){
            container.removeView(obj)
            views.addFirst(obj)
        }
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean = p0 == p1

    override fun getCount(): Int {
        return 5
    }
}