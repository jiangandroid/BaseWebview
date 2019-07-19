package com.jiang.baseWebview;

import android.util.ArrayMap;

import java.util.Map;
import java.util.Set;

/**
 * Created by xiyou on 2019/4/12
 */
public class YtJsRegistor {
    static final Map<String, String> handlerRegistryMap = new ArrayMap<>();

    static {
        new YtBaseJsHandler$$YtJsMethodRegister().register(handlerRegistryMap);
    }

    public static Map<String, String> getHandlerRegistryMap() {
        return handlerRegistryMap;
    }

    public static Set<String> getJsMethods() {
        return handlerRegistryMap.keySet();
    }

    public static String getFuncHandler(String funcName) {
        return handlerRegistryMap.get(funcName);
    }

    public static void register(Class clazz) {
        try {
            Class clazzRegister = Class.forName(clazz.getCanonicalName() + "$$YtJsMethodRegister");
            ((YtJsMethodRegister) clazzRegister.newInstance()).register(handlerRegistryMap);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

}
