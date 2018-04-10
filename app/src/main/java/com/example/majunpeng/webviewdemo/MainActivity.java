package com.example.majunpeng.webviewdemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Set;

public class MainActivity extends AppCompatActivity{

    WebView mWebView;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setWebView();
        initListener();
    }

    private void initListener() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过Handler发送消息
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        //方法一
                        //注意调用的JS方法名要对应上
                        //调用javascript的callJS（）方法
//                        mWebView.loadUrl("javascript:callJS()");

                        /**
                         * 方法二
                         * 优点：该方法比第一种方法效率更高，使用更简洁
                         * 1.因为该方法的执行不会使页面刷新，而第一种方法（loadUrl ）的执行则会。
                         * 2.Android 4.4 后才可使用
                         *
                         * 对比：                       优点                     缺点                             使用场景
                         * loadUrl                  方便简洁        效率低，获取返回值麻烦            不需要获取返回值，对性能需求较低时
                         * evaluateJavascript       效率高          向下兼容性差（4.4以上）             Android4.4以上
                         */
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                            mWebView.evaluateJavascript("javascript:callJS()", new ValueCallback<String>() {
//                                @Override
//                                public void onReceiveValue(String value) {
//                                    //此处为js返回的结果
//                                    Toast.makeText(MainActivity.this,value, Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }

                        /**
                         * 混合使用
                         */
                        if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.KITKAT){
                            mWebView.evaluateJavascript("javascript:callJS()", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    //此处为js返回的结果
                                }
                            });
                        }else {
                            mWebView.loadUrl("javascript:callJS()");
                        }

                    }
                });
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setWebView() {
        WebSettings webSettings = mWebView.getSettings();
        //设置与JS交互的权限
        webSettings.setJavaScriptEnabled(true);

        //JS通过WebView调用 Android 代码

        //通过addJavascriptInterface()将Java对象映射到JS对象
        //参数1：Javascript对象名
        //参数2：Java对象名
        mWebView.addJavascriptInterface(new AndroidToJs(),"test");

        //设置允许JS弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 先载入JS代码
        // 格式规定为:file:///android_asset/文件名.html
        mWebView.loadUrl("file:///android_asset/javascript.html");

        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                Uri uri = Uri.parse(message);
                if (uri.getScheme().equals("js")){
                    if (uri.getAuthority().equals("demo")){
                        // 执行JS所需要调用的逻辑
                        System.out.println("js调用了Android的方法");
                        // 可以在协议上带有参数并传递到Android上
                        HashMap<String, String> params = new HashMap<>();
                        Set<String> collection = uri.getQueryParameterNames();

                        //参数result:代表消息框的返回值(输入值)
                        result.confirm("js调用了Android的方法成功啦");
                    }
                    return true;
                }

                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
        });

        /**
         * 优点：不存在方式1的漏洞
         * 缺点：JS获取Android方法的返回值复杂。
         */
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //步骤2：根据协议的参数，判断是否是所需要的url
                // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                //假定传入进来的 url = "js://webview?arg1=111&arg2=222"（同时也是约定好的需要拦截的）

                Uri uri = Uri.parse(url);
                // 如果url的协议 = 预先约定的 js 协议
                // 就解析往下解析参数
                if (uri.getScheme().equals("js")){
                    // 如果 authority  = 预先约定协议里的 webview，即代表都符合约定的协议
                    // 所以拦截url,下面JS开始调用Android需要的方法
                    if (uri.getAuthority().equals("webview")){
                        //  步骤3：
                        // 执行JS所需要调用的逻辑
                        System.out.println("js调用了Android的方法");
                        // 可以在协议上带有参数并传递到Android上
                        HashMap<String, String> params = new HashMap<>();
                        Set<String> collection = uri.getQueryParameterNames();
                    }

                    return true;
                }

                return super.shouldOverrideUrlLoading(view,url );
            }
        });
    }

    private void initView() {
        mWebView = (WebView) findViewById(R.id.webview);
        button = (Button) findViewById(R.id.btn);
    }
}
