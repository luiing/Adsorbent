/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent.demo

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.uis.adsorbent.SingleAdsorbentListener
import com.uis.adsorbent.demo.holder.DemoGroupAdapter
import com.uis.adsorbent.demo.holder.VT_PIN_SINGLE
import com.uis.adsorbent.demo.holder.VT_TXT
import com.uis.adsorbent.demo.holder.VT_TXT_BLUE
import com.uis.groupadapter.GroupEntity
import com.uis.groupadater.demo.R
import kotlinx.android.synthetic.main.ui_demo.*
import kotlinx.android.synthetic.main.ui_view_pin.view.*


class SingleRecyclerUi: AppCompatActivity() {

    val adapter = DemoGroupAdapter()
    lateinit var pin: View
    lateinit var manager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_demo)
        pin = layoutInflater.inflate(R.layout.ui_view_pin,null)
        pin.bt_add.setOnClickListener{

        }
        pin.bt_clear.setOnClickListener{

        }
        for(i in 0 until 3) {
            adapter.addEntity(GroupEntity(VT_TXT, "txt $i"))
        }
        for(i in 0 until 3) {
            adapter.addEntity(GroupEntity(VT_TXT_BLUE, "txt blue $i"))
        }
        for(i in 0 until 3) {
            adapter.addEntity(GroupEntity(VT_TXT, "txt position $i"))
        }
        adapter.addEntity(GroupEntity(VT_PIN_SINGLE,pin))
        for(i in 0 until 50) {
            adapter.addEntity(GroupEntity(VT_TXT, "txt bottom $i"))
        }


        manager = LinearLayoutManager(this)
        recyclerView.enableConflict = false
        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : SingleAdsorbentListener(){
            /** 获取被吸顶ViewGroup*/
            override fun getUiViewGroup(): ViewGroup = relative
            /** 获取吸顶View*/
            override fun getPinView(): View = pin
            /** 获取吸顶View在RecyclerView中的位置*/
            override fun getPinViewPosition(): Int = 9

            override fun stopWhenAdsorbent(): Boolean = false
        })
    }



}