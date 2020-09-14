package com.limiter.configuration;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yangge
 * @version 1.0.0
 * @date 2020/9/11 11:07
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LimitAutoConfiguration.class)
public @interface EnableLimit {
}
