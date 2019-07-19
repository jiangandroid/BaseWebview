package com.jiang.baseWebview;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jiang.annotations.JsRegister;

import java.util.Map;

/**
 * Created by xiyou on 2019/4/8
 */
public class YtBaseJsHandler {
    /**
     * 打电话
     *
     * @param activity
     * @param funcParams
     * @param jsCallbackFunName
     */
    @JsRegister
    public static void callPhone(Activity activity, String funcParams, String jsCallbackFunName) {
        try {
            JsonObject jsonObject = YtKitWebJsonUtil.jsonToBean(funcParams, JsonObject.class);
            JsonElement mobile = jsonObject.get("mobile");
            if (mobile != null) {
                String phoneNum = mobile.getAsString();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri data = Uri.parse("tel:" + phoneNum);
                intent.setData(data);
                activity.startActivity(intent);
            }
        } catch (Exception e) {
            Log.d("YtBaseJsHandler", "CallPhone error" + e.toString());
        }
    }


    /**
     * 调用原生复制到剪贴板
     *
     * @param activity
     * @param funcParams
     */
    @JsRegister
    public static void copyToPasteboard(Activity activity, String funcParams, String jsCallbackFunName) {
        try {
            Map<String, String> map = (Map<String, String>) YtKitWebJsonUtil.jsonToMap(funcParams, new TypeToken<Map<String, String>>() {
            }.getType());
            String content = map.get("content");
            ClipboardManager cm = (ClipboardManager) activity.getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(content);
        } catch (Exception e) {
            Log.d("YtBaseJsHandler", "handleCopyClipboard error" + e.toString());
        }
    }
}
