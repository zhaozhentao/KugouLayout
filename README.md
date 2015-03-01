# KugouLayout
an interesting layout

一个模仿酷狗播放器滑动返回的layout

可以让你的app有更丰富 便捷的手势操作

支持activity滑动返回和普通layout的滑动 显示/隐藏 两种模式,可以参考demo里面的Activity1和Acitivyt2

**有喜欢KugouLayout的朋友 Star一下哦**

# 个人建议使用场景

用于activity的滑动返回

需要使用网络加载要长时间等待的activity,可以用KugouLayout加入摇屏功能,让用户可以在等待加载期间拽一拽屏幕

and...


# ScreenShot
图片较大 加载会慢一些 耐心等待

Demo Activity1

![image](https://github.com/zhaozhentao/KugouLayout/blob/master/screenshot/screen2.gif)

Demo Activity2

![image](https://github.com/zhaozhentao/KugouLayout/blob/master/screenshot/screen1.gif)

# Usage
###Activity滑动返回

    <!--设置主题属性-->
    <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
        <item name="android:windowIsTranslucent">true</item>
    </style>

    kugouLayout = new KugouLayout(this);
    //依附到activity 控制屏幕滑动
    kugouLayout.attach(this);
    //需要保护kugoulayout内横向滑动事件的view添加到这里
    kugouLayout.addHorizontalScrollableView(findViewById(R.id.horizontalScrollView));
    
###普通layout的滑动隐藏于显示

    //设置要显示的内容即可
    kugouLayout.setContentView(R.layout.activity_main);

# Me

QQ:344696734


