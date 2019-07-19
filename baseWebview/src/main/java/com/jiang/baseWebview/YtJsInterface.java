package com.jiang.baseWebview;

/**
 * Created by xiyou on 2019/1/16
 * 所有自定义Js接口文件都实现该接口，重写上下文context的绑定及重置
 */
public interface YtJsInterface {
    void resetContext();

    void setYtJsCallbackHandler(YtJsCallbackHandler handler);
}
