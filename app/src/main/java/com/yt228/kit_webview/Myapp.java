package com.yt228.kit_webview;

import android.app.Application;

import com.jiang.baseWebview.YtJsRegistor;

/**
 * Created by xiyou on 2019/4/12
 */
public class Myapp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        YtJsRegistor.register(NativeComponentHandlerImplTest.class);
        YtJsRegistor.register(BusinessHandlerImplTest.class);
    }
}
