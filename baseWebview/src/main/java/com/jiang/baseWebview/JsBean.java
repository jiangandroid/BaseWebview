package com.jiang.baseWebview;

import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by xiyou on 2019/4/4
 * H5调用native js接口传递的参数实例对象
 */
public class JsBean {
    private String func;//native对应的方法名
    private String jsCallbackFunName;//需要回调的H5方法名
    private JsonObject params;//H5传递过来需要解析的具体参数数据

    public String getFunc() {
        return func;
    }

    public String getParamsStr() {
        return params == null ? null : params.toString();
    }

    public String getJsCallbackFunName() {
        if (!TextUtils.isEmpty(jsCallbackFunName)) {
            return jsCallbackFunName;
        } else if (params != null) {
            JsonElement callBack = params.get("jsCallbackFunName");
            if (callBack != null) {
                return callBack.getAsString();
            }
        }
        return null;
    }
}
