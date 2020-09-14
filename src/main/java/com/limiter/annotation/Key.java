package com.limiter.annotation;

import java.lang.annotation.*;

/**
 * @author yangge
 * @version 1.0.0
 * @date 2020/9/11 9:31
 */

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Key {

}
