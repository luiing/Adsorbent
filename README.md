# 吸顶终极版

    1. Single RecyclerView：简单模式
    【利用RecyclerView.OnScrollListener监听滑动位置，吸顶View被 ViewHolder和Activity复用】
    
    2. Double RecyclerView：RecyclerView嵌套RecyclerView
    【事件分发，吸顶View是个单独ViewHolder,无须做其他处理】
    
    3. Viewpager RecyclerView:RecyclerView嵌套ViewPager(其中包含的页面内容是RecyclerView)
    【事件分发，吸顶View是个单独ViewHolder,无须做其他处理】

    4. 拓展View需要调用ParentRecyclerView接口OnFlingListener,并实现OnChildFlingListener
     【拓展View支持下拉刷新parent.onScrollBottom(false),支持联动onChildFling(speed: Int)】
    
### USE by Kotlin
    implementation 'com.uis:adsorbent:0.5.7
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation "androidx.recyclerview:recyclerview:1.0.0"

``` 项目中使用的是compileOnly,使用者需自行加入外部依赖库 ```

### VERSION

Version|Descipt|Fixed|Time
----|----|----|----
0.1.1|初始版本| |2019/5
0.1.2|新增|快速滑动联动效果|2019/5
0.1.3|更改|快速滑动联动处理|2019/5
0.2.0|优化|联动平滑过渡,冲突后重新分发|2019/5
0.2.1|优化|联动支持fling|2019/5
0.3.0|优化|冲突事件分发优化,更简单易懂|2019/6
0.3.2|fixed|The specified child already has a parent. You must call removeView() on the child's parent first|2019/10
0.3.3|fixed|联动子view效果|2020/2
0.3.4|fixed|吸顶后子view不在顶部时父类在动|2020/2
0.3.5|fixed|吸顶后对view滑动事件放行处理|2020/3
0.3.6|fixed|纵向滑动判断|2020/3
0.4.0|优化|优化事件分发|2020/5
0.4.2|优化|优化fling联动速度,支持拓展|2020/5
0.5.0|新增|支持吸顶部分下拉刷新|2020/6
0.5.1|fixed|吸顶部分下拉刷新事件冲突|2020/7
0.5.5|fixed|内容在一个屏幕内支持下拉刷新|2020/7
0.5.6|优化|要吸顶View与Parent交集以外区域，parent事件不传递给吸顶View|2020/7
0.5.7|优化|刚吸顶时横向滑动和纵向冲突|2020/7

### USE
##### 事件分发ParentRecyclerView设置
    /** true 开启滑动冲突处理(默认true)*/
    recyclerView.enableConflict = true
    /** 开启快速滚动parent带动child联动效果(默认false)*/
    recyclerView.enableParentChain = false
    /** 开启快速滚动child带动parent联动效果(默认true)*/
    recyclerView.enableChildChain = true
    /** 开启child下拉刷新(默认false)*/
    recyclerView.enableChildSwipeRefresh = true
    
##### Single
    recyclerView.addOnScrollListener(object : SingleAdsorbentListener(){
            /** 获取被吸顶ViewGroup*/
            override fun getUiViewGroup(): ViewGroup = relative
            /** 获取吸顶View*/
            override fun getPinView(): View = pin
            /** 获取吸顶View在RecyclerView中的位置*/
            override fun getPinViewPosition(): Int = 15
            /** 吸顶的时候 true:停止滚动并定位在吸顶位置,false:可以继续fling*/
            override fun stopWhenAdsorbent(): Boolean = false
        })
##### Double
    //recyclerView is ParentRecyclerView
    manager = LinearLayoutManager(this)
    recyclerView.layoutManager = manager
    recyclerView.adapter = adapter
    
    //itemView.recyclerview is ChildRecyclerView as ViewHolder
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

##### Viewpager
    //recyclerView is ParentRecyclerView
    manager = LinearLayoutManager(this)
    recyclerView.layoutManager = manager
    recyclerView.adapter = adapter
    
    //ViewPager as ViewHolder,ChildRecycler is ViewPager Item
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

### LICENSE
MIT License

Copyright (c) 2019 uis

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.