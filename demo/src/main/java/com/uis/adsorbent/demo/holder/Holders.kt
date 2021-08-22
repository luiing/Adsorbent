/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent.demo.holder

import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.uis.groupadapter.GroupAdapter
import com.uis.groupadapter.GroupEntity
import com.uis.groupadapter.GroupHolder
import com.uis.groupadater.demo.R
import kotlinx.android.synthetic.main.ui_item_recyclerview.view.*
import kotlinx.android.synthetic.main.ui_item_txt.view.*
import kotlinx.android.synthetic.main.ui_item_txt_large.view.*
import kotlinx.android.synthetic.main.ui_item_viewpager.view.*
import kotlinx.android.synthetic.main.ui_view_pin.view.*

const val VT_TXT        = 0
const val VT_TXT_LARGE  = 1
const val VT_TXT_BLUE   = 2
const val VT_PIN   = 3
const val VT_PIN_SINGLE  = 6
const val VT_VIEWPAGER   = 4
const val VT_RECYCLER   = 5
const val VT_VIEWPAGER_FULL   = 7


class DemoGroupAdapter : GroupAdapter(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder<out Any>{
        return when(viewType){
            VT_TXT_LARGE -> TxtLargeVH(parent)
            VT_TXT_BLUE -> TxtBlueVH(parent)
            VT_PIN -> PinVH(parent)
            VT_VIEWPAGER -> ViewPagerVH(parent)
            VT_VIEWPAGER_FULL -> ViewPagerFullVH(parent)
            VT_RECYCLER -> RecyclerVH(parent)
            VT_PIN_SINGLE -> PinSingleVH(parent)
            else         -> TxtVH(parent)
        }
    }
}

class RecyclerVH(parent: ViewGroup) : GroupHolder<String>(R.layout.ui_item_recyclerview,parent){
    init {
        val p = itemView.recyclerview.layoutParams
        /** 吸顶高+child recyclerView高 = recyclerView高*/
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, parent.resources.displayMetrics) + 0.5f
        p.height = parent.measuredHeight - height.toInt()
        itemView.recyclerview.layoutParams = p

        val adapter = DemoGroupAdapter()
        for(i in 0 until 50) {
            adapter.addEntity(GroupEntity(VT_TXT, "Child RecyclerView item $i"))
        }
        val recycler = itemView.recyclerview
        recycler.layoutManager = LinearLayoutManager(parent.context)
        recycler.adapter = adapter
    }
    override fun bindVH(item: String) {

    }
}

class ViewPagerFullVH(parent: ViewGroup) : GroupHolder<String>(R.layout.ui_item_viewpager,parent){
    init {
        val viewpager = itemView.viewpager
        val p = viewpager.layoutParams
        p.height = parent.measuredHeight

        viewpager.layoutParams = p
        viewpager.adapter = SimplePagerAdapter()

        viewpager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(p0: Int) {
//                if(parent is RecyclerView){
//                    /** 定位到吸顶位置*/
//                    parent.layoutManager?.let {
//                        parent.scrollToPosition(it.itemCount-1)
//                    }
//                }
            }
        })
    }

    override fun bindVH(item: String) {

    }
}

class ViewPagerVH(parent: ViewGroup) : GroupHolder<String>(R.layout.ui_item_viewpager,parent){
    init {
        val viewpager = itemView.viewpager
        val p = viewpager.layoutParams
        /** 吸顶高+viewpage高 = recyclerView高*/
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, parent.resources.displayMetrics) + 0.5f
        p.height = parent.measuredHeight - height.toInt()

        viewpager.layoutParams = p
        viewpager.adapter = SimplePagerAdapter()

        viewpager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(p0: Int) {
                if(parent is RecyclerView){
                    /** 定位到吸顶位置*/
                    parent.layoutManager?.let {
                        parent.scrollToPosition(it.itemCount-1)
                    }

                }
            }
        })
    }

    override fun bindVH(item: String) {

    }
}

class PinSingleVH(parent: ViewGroup) : GroupHolder<View>(R.layout.ui_item_pin,parent){
    override fun bindVH(item: View) {

    }
}

class PinVH(parent: ViewGroup) : GroupHolder<View>(R.layout.ui_item_pin,parent){
    override fun bindVH(item: View) {
        if(itemView is ViewGroup && itemView.childCount <= 0){
            itemView.addView(item,ViewGroup.LayoutParams(-1,-1))
        }
        itemView.bt_add.setOnClickListener {
            Toast.makeText(it.context,"add",Toast.LENGTH_SHORT).show()
        }
    }
}

class TxtVH(parent: ViewGroup) : GroupHolder<String>(R.layout.ui_item_txt,parent){

    init {
        itemView.setOnClickListener{
            Toast.makeText(it.context,"click txt here",Toast.LENGTH_SHORT).show()
            Log.e("xx","onCLicked")
        }
        itemView.setOnLongClickListener{
            return@setOnLongClickListener true
        }
    }
    override fun bindVH(item: String) {
        itemView.tv_txt.text = item
    }
}

class TxtBlueVH(parent: ViewGroup) : GroupHolder<String>(R.layout.ui_item_txt_blue,parent){
    override fun bindVH(item: String) {
        itemView.tv_txt.text = item
    }
}

class TxtLargeVH(parent: ViewGroup) : GroupHolder<String>(R.layout.ui_item_txt_large,parent){
    override fun bindVH(item: String) {
        itemView.tv_txt_large.text = item
    }
}