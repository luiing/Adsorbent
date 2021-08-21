/*
 * Copyright (c) 2021 by uis
 * Author: uis
 * Github: https://github.com/luiing
 */

package com.uis.adsorbent

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

/**
 * @autho uis
 * @date 2021/8/21
 * @github https://github.com/luiing
 */
open class ConflictRecyclerView : RecyclerView{
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

}