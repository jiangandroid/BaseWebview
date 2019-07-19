package com.jiang.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by xiyou on 2019/4/8
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface JsRegister {
}
