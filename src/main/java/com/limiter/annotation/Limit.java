package com.limiter.annotation;

import java.lang.annotation.*;

/**
 * @author yangge
 * @version 1.0.0
 * @date 2020/9/10 10:15
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Limit {

    String prefix() default "";

    /**
     * if key is null, use the method name as key
     */
    String[] key() default {};

    Rule[] ruleGroup();

    String separator() default ":";

    /**
     * login user
     */
    String userId() default "";
}
