package com.yt228.kit_webview;

import android.app.Activity;

import com.jiang.annotations.JsRegister;

/**
 * 提供原生组件功能的实现
 */
public class NativeComponentHandlerImplTest {

//    public static void registry() {
//        YTJavaScriptFactory.registry(HandlerRegistry.Function.TOAST, NativeComponentHandlerImplTest.class);
//        YTJavaScriptFactory.registry(HandlerRegistry.Function.COPY_TO_PASTEBOARD, NativeComponentHandlerImplTest.class);
//        YTJavaScriptFactory.registry(HandlerRegistry.Function.COMMON_SHARE, NativeComponentHandlerImplTest.class);
//        YTJavaScriptFactory.registry(HandlerRegistry.Function.VIDEO_UPLOAD, NativeComponentHandlerImplTest.class);
//        YTJavaScriptFactory.registry(HandlerRegistry.Function.NAVIGATION_ACTIONS, NativeComponentHandlerImplTest.class);
//        YTJavaScriptFactory.registry(HandlerRegistry.Function.CALL_PHONE, NativeComponentHandlerImplTest.class);
//        YTJavaScriptFactory.registry(HandlerRegistry.Function.PAGE_REFRESH, NativeComponentHandlerImplTest.class);
//        YTJavaScriptFactory.registry(HandlerRegistry.Function.PAGE_CLOSE, NativeComponentHandlerImplTest.class);
//    }

    /**
     * 关闭当前页面
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void pageClose(Activity activity, String params, String jsCallbackFunName) {
    }

    @JsRegister
    public static void navigationActions(Activity activity, String params, String jsCallbackFunName) {
    }


    /**
     * 调用原生toast
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void toast(Activity activity, String params, String jsCallbackFunName) {
    }

    /**
     * 打电话
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void callPhone(Activity activity, String params, String jsCallbackFunName) {
    }

    /**
     * 刷新当前url
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void pageRefresh(Activity activity, String params, String jsCallbackFunName) {
    }

    /**
     * 调用原生复制到剪贴板
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void copyToPasteboard(Activity activity, String params, String jsCallbackFunName) {
    }

    /**
     * 调用原生分享，包括分享链接，图文，单图，小程序
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void commonShare(Activity activity, String params, String jsCallbackFunName) {
    }

    /**
     * 调用视频上传
     */
    @JsRegister
    public static void videoUpload(Activity activity, String params, String jsCallbackFunName) {
    }
}
