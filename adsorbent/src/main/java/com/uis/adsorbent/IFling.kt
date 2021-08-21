/*
 * Copyright (c) 2019 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent

internal interface IFling {
    fun onStatus(isTop: Boolean,top:Int)
    fun onFling(childSpeed: Int)
}