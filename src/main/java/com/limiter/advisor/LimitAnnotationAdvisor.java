package com.limiter.advisor;

import com.limiter.annotation.Limit;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author yangge
 * @version 1.0.0
 * @date 2020/9/10 10:32
 */
public class LimitAnnotationAdvisor extends AbstractPointcutAdvisor {

    private Advice advice;

    private Pointcut pointcut;

    public LimitAnnotationAdvisor(StringRedisTemplate redisTemplate) {
        this.advice = buildAdvice(redisTemplate);
        this.pointcut = buildPointcut();
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    protected Advice buildAdvice(StringRedisTemplate redisTemplate) {
        return new AnnotationLimitInterceptor(redisTemplate);
    }

    protected Pointcut buildPointcut() {
        Pointcut mpc = new AnnotationMatchingPointcut(null, Limit.class, true);
        return mpc;
    }
}
