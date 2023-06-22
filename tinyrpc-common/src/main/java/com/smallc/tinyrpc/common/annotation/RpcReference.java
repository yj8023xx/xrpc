package com.smallc.tinyrpc.common.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.annotation
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcReference {

    String version() default "";

}
