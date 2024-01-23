package com.smallc.xrpc.common.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.common.annotation
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcReference {

    String version() default "";

    String loadbalance() default "random";

    String serialize() default "hessian";

}
