package com.limiter.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author yangge
 * @version 1.0.0
 * @date 2020/9/10 10:25
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Rule {

    String name();

    long limit() default 10L;

    long expire() default 1L;

    TimeUnit unit() default TimeUnit.SECONDS;

    int order() default 5;

    String exceptionMsg() default "";
}
