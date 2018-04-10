package com.example.majunpeng.webviewdemo;

import android.webkit.JavascriptInterface;

/**
 * 优点：使用简单
 * 仅将Android对象和JS对象映射即可
 * 缺点：存在严重的漏洞问题，https://www.jianshu.com/p/3a345d27cd42
 * Created by majunpeng on 2018/4/9.
 */

public class AndroidToJs extends Object{

    /**
     * 定义JS需要调用的方法
     * 被JS调用的方法必须加入@JavascriptInterface注解
     * @param msg
     */
    @JavascriptInterface
    public void hello(String msg){
        System.out.println("JS调用了Android的hello方法");
    }


}
