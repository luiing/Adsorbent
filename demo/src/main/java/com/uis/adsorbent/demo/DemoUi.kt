/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent.demo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.uis.groupadater.demo.R
import kotlinx.android.synthetic.main.ui_demo_main.*


class DemoUi: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_demo_main)

        bt_single.setOnClickListener{
            val intent = Intent(this, SingleRecyclerUi::class.java)
            startActivity(intent)
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