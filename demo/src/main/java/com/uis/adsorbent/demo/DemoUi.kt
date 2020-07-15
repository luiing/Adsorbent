/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent.demo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.uis.groupadater.demo.R
import kotlinx.android.synthetic.main.ui_demo_main.*
import org.json.JSONArray


class DemoUi: AppCompatActivity() {

    var list:MutableList<String> = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_demo_main)


        bt_single.setOnClickListener{
            val intent = Intent(this, SingleRecyclerUi::class.java)
            //startActivity(intent)
            val id = "3"
            var sub:MutableList<String> = list
            for((index,item) in list.withIndex()){
                Log.e("xx",""+index+","+item)
                if(item == id){
                    sub = list.subList(index+1,list.size)
                    break
                }
            }
            sub.add(0,id)
            list = sub
            val ja = JSONArray()
            for(i in list){
                ja.put(i)
            }
            Log.e("xx",sub.toString()+"\n"+ja.toString())
            //list.reverse()
        }

        bt_double.setOnClickListener{
            val intent = Intent(this, DoubleRecyclerUi::class.java)
            startActivity(intent)
        }

        bt_viewpager.setOnClickListener{
            val intent = Intent(this, ViewpagerRecyclerUi::class.java)
            startActivity(intent)
        }
    }
}