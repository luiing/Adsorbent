
### 吸顶解决方案(demo模块)
    1. Single RecyclerView：简单模式【利用RecyclerView.OnScrollListener监听滑动位置，吸顶View被 ViewHolder和Activity复用】
    
    2. Double RecyclerView：RecyclerView嵌套RecyclerView【事件分发，吸顶View是个单独ViewHolder,无须做其他处理】
    
    3. Viewpager RecyclerView:RecyclerView嵌套ViewPager(其中包含的页面内容是RecyclerView)【事件分发，吸顶View是个单独ViewHolder,无须做其他处理】
    

### PREVIEW
![](/preview/001.png) 

### USE by Kotlin
    implementation 'com.uis:groupadapter:0.4.0
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation "com.android.support:recyclerview-v7:$supportVer"

``` 项目中使用的是compileOnly,使用者需自行加入外部依赖库 ```

```

```




### VERSION

Version|Descipt|Fixed|Time
----|----|----|----
0.1.0|初始版本| |2019/05/23


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