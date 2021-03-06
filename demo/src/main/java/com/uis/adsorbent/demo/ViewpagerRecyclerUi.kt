/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.uis.adsorbent.demo.holder.*
import com.uis.groupadapter.GroupEntity
import com.uis.groupadater.demo.R
import kotlinx.android.synthetic.main.ui_demo.*
import kotlinx.android.synthetic.main.ui_view_pin.view.*


class ViewpagerRecyclerUi: AppCompatActivity() {

    val adapter = DemoGroupAdapter()
    lateinit var pin: View
    lateinit var manager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        //Fresco.initialize(applicationContext)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_demo)

        pin = layoutInflater.inflate(R.layout.ui_view_pin,null)
        pin.bt_add.setOnClickListener{

        }
        pin.bt_clear.setOnClickListener{

        }
        adapter.initGroup(10)
        for(i in 0 until 2) {
            //adapter.addEntity(GroupEntity(VT_TXT, "txt $i"))
        }
        for(i in 0 until 2) {
            //adapter.addEntity(GroupEntity(VT_TXT_BLUE, "txt blue $i"))
        }
        for(i in 0 until 2) {
            //adapter.addEntity(GroupEntity(VT_TXT, "txt position $i"))
        }
        adapter.addEntity(GroupEntity(VT_PIN,pin))
        adapter.addEntity(GroupEntity(VT_VIEWPAGER,""))

        manager = LinearLayoutManager(this)
        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter
        recyclerView.enableParentChain = true
        recyclerView.enableChildSwipeRefresh = true
        recyclerView.singleScreen = true
    }



}