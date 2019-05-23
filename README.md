
### 吸顶解决方案(终极版)

    1. Single RecyclerView：简单模式
    【利用RecyclerView.OnScrollListener监听滑动位置，吸顶View被 ViewHolder和Activity复用】
    
    2. Double RecyclerView：RecyclerView嵌套RecyclerView
    【事件分发，吸顶View是个单独ViewHolder,无须做其他处理】
    
    3. Viewpager RecyclerView:RecyclerView嵌套ViewPager(其中包含的页面内容是RecyclerView)
    【事件分发，吸顶View是个单独ViewHolder,无须做其他处理】
    
    4. 项目无偿使用，请注明出处和作者信息
    

### USE
##### Single
    recyclerView.addOnScrollListener(object : SingleAdsorbentListener(){
            /** 获取被吸顶ViewGroup*/
            override fun getUiViewGroup(): ViewGroup = relative
            /** 获取吸顶View*/
            override fun getPinView(): View = pin
            /** 获取吸顶View在RecyclerView中的位置*/
            override fun getPinViewPosition(): Int = 15
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
    

### USE by Kotlin
    implementation 'com.uis:adsorbent:0.1.2
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation "com.android.support:recyclerview-v7:$supportVer"
    

``` 项目中使用的是compileOnly,使用者需自行加入外部依赖库 ```

### VERSION

Version|Descipt|Fixed|Time
----|----|----|----
0.1.1|初始版本| |2019/05/23
0.1.2|fixed快速滑动联动效果| |2019/05/24


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