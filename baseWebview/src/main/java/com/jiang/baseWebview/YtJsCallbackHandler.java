package com.jiang.baseWebview;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * YtWebView载体需要实现的接口，主要方法是 handleJsFunctionSelf ，
 * 用来处理某些非全局开放的js方法，返回true表示本地已处理，无需经过统一的js分发
 */
public interface YtJsCallbackHandler {
    Activity getActivity();

    /**
     * 处理业务专有js方法
     *
     * @param funcName js方法名称
     * @param paramStr js方法参数，json格式
     * @return
     */
    boolean handleJsFunctionSelf(@NonNull String funcName, @Nullable String paramStr, String jsCallbackFunName);
}
