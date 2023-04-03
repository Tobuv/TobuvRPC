package com.tobuv.rpc.annotation;

import java.lang.annotation.*;

/**
 * 用于声明接口支持SPI扩展
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
