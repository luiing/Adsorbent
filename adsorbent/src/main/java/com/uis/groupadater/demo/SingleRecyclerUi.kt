package com.uis.groupadater.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import com.uis.groupadapter.GroupEntity
import com.uis.groupadater.demo.holder.*
import com.uis.groupadater.demo.adsorbent.SingleAdsorbentListener
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
        for(i in 0 until 5) {
            adapter.addEntity(GroupEntity(VT_TXT, "txt $i"))
        }
        for(i in 0 until 5) {
            adapter.addEntity(GroupEntity(VT_TXT_BLUE, "txt blue $i"))
        }
        for(i in 0 until 5) {
            adapter.addEntity(GroupEntity(VT_TXT, "txt position $i"))
        }
        adapter.addEntity(GroupEntity(VT_PIN_SINGLE,pin))
        for(i in 0 until 50) {
            adapter.addEntity(GroupEntity(VT_TXT, "txt bottom $i"))
        }


        manager = LinearLayoutManager(this)
        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : SingleAdsorbentListener(){
            /** 获取被吸顶ViewGroup*/
            override fun getUiViewGroup(): ViewGroup = relative
            /** 获取吸顶View*/
            override fun getPinView(): View = pin
            /** 获取吸顶View在RecyclerView中的位置*/
            override fun getPinViewPosition(): Int = 15
        })
    }



}