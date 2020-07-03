/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent

internal interface OnFlingListener {
    fun onScrollTop(isTop: Boolean,top:Int)
    fun onFling(childSpeed: Int)
}