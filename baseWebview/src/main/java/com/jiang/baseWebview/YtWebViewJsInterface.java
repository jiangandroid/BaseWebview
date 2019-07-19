package com.jiang.baseWebview;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import static com.jiang.baseWebview.KitWebviewUtil.exceptionToString;

public class YtWebViewJsInterface implements YtJsInterface {
    public static String JS_BRIDGE = "MallJsBridge";

    /**
     * 初始化 JS_BRIDGE 对应name，主要针对不同项目使用不同js name
     *
     * @param jsName
     */
    public static void initJsName(@NonNull String jsName) {
        JS_BRIDGE = jsName;
    }

    protected YtJsCallbackHandler mYtJsCallbackHandler;

    public YtWebViewJsInterface() {

    }

    public YtWebViewJsInterface(YtJsCallbackHandler activity) {
        this.mYtJsCallbackHandler = activity;
    }

    @Override
    public void resetContext() {
        this.mYtJsCallbackHandler = null;
    }

    @Override
    public void setYtJsCallbackHandler(YtJsCallbackHandler handler) {
        this.mYtJsCallbackHandler = handler;
    }

    //h5通过该接口调用本地方法，进行统一分发
    @JavascriptInterface
    public void jsMethod(String input) {
        if (mYtJsCallbackHandler == null) {
            return;
        }
        try {
            JsBean jsBean = YtKitWebJsonUtil.jsonToBean(input, JsBean.class);
            String funcName = jsBean.getFunc();
            String jsCallbackFunName = jsBean.getJsCallbackFunName();
            String paramStr = jsBean.getParamsStr();
            if (!TextUtils.isEmpty(funcName)) {
                //首字母统一转换成小写
                funcName = funcName.substring(0, 1).toLowerCase() + funcName.substring(1);
                if (!mYtJsCallbackHandler.handleJsFunctionSelf(funcName, paramStr, jsCallbackFunName)) {
                    YTJavaScriptFactory.handleFunction(this.mYtJsCallbackHandler.getActivity(), funcName, paramStr, jsCallbackFunName);
                }
            }
        } catch (Exception e) {
            Log.e("YtWebViewJsInterface", exceptionToString(e));
        }
    }
}
