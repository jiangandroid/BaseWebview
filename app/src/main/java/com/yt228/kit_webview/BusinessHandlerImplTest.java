package com.yt228.kit_webview;

import android.app.Activity;
import android.util.Log;

import com.jiang.annotations.JsRegister;

/**
 * 业务相关方法
 */
public class BusinessHandlerImplTest {

    /**
     * 详情页分享按钮
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void share(Activity activity, String params, String jsCallbackFunName) {
        Log.e("YtJsRegistor Test", "shareshareshareshareshareshareshareshareshareshare");
    }

    /**
     * 保税商品客户自购分享
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void ordershare(Activity activity, String params, String jsCallbackFunName) {
    }

    /**
     * 保税商品客户自购展示付款码
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void receiptCodeShare(Activity activity, String params, String jsCallbackFunName) {
    }

    /**
     * 嗨口令
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void copySecretKey(Activity activity, String params, String jsCallbackFunName) {
    }

    /**


    /**
     * 跳转主页tab
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void popToRoot(Activity activity, String params, String jsCallbackFunName) {
    }

    /**
     * 取消订单原因弹框
     *
     * @param activity
     * @param params
     */
    @JsRegister
    public static void cancelOrderReason(Activity activity, String params, String jsCallbackFunName) {
    }
}
