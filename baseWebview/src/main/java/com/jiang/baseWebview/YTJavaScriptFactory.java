package com.jiang.baseWebview;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

import static com.jiang.baseWebview.KitWebviewUtil.exceptionToString;

/**
 * JsMethod分发
 */
public class YTJavaScriptFactory {

    /**
     * @param activity
     * @param funcName
     * @param params
     * @return
     */
    public static boolean handleFunction(Activity activity, String funcName, String params, String jsCallbackFunName) {
        String fullClassName = YtJsRegistor.getFuncHandler(funcName);
        if (!TextUtils.isEmpty(fullClassName)) {
            try {
                Class clazz = Class.forName(fullClassName);
                Method method = clazz.getMethod(funcName, Activity.class, String.class, String.class);
                method.invoke(null, activity, params, jsCallbackFunName);
            } catch (Exception e) {
                Log.e("handleFunction", exceptionToString(e));
            }
        }
        return false;
    }
}