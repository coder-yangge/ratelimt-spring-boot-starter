package com.limiter.advisor;

import com.limiter.annotation.Limit;
import com.limiter.annotation.Rule;
import com.limiter.exception.LimitException;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yangge
 * @version 1.0.0
 * @date 2020/9/10 10:41
 */
@Slf4j
public class AnnotationLimitInterceptor extends AbstractLimitInterceptor implements MethodInterceptor {

    private StringRedisTemplate redisTemplate;

    private DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();

    public AnnotationLimitInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/script/limit.lua")));
        redisScript.setResultType(Boolean.class);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        Class<?> targetClass = (methodInvocation.getThis() != null ? AopUtils.getTargetClass(methodInvocation.getThis()) : null);
        Method specificMethod = ClassUtils.getMostSpecificMethod(methodInvocation.getMethod(), targetClass);
        final Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        Limit limit = AnnotatedElementUtils.findMergedAnnotation(userDeclaredMethod, Limit.class);
        Rule[] rules = limit.ruleGroup();
        if (rules == null || rules.length < 1) {
            methodInvocation.proceed();
        }
        StringBuilder finalKey = getKey(limit, specificMethod, methodInvocation.getArguments());
        // 根据规则排序
        sortRule(rules);
        Map<String, Rule> passed = new HashMap<>();
        for (Rule rule : rules) {
            String key = finalKey.toString();
            String name = rule.name();
            long max = rule.limit();
            long expire = rule.expire();
            TimeUnit unit = rule.unit();
            key = StringUtils.isBlank(name) ? key : key + name;
            if (!acquire(key, expire, max, unit)) {
                log.info("由于超过单位时间={}-允许的请求次数={}[触发限流]", expire, max);
                // 通过的规则限流-1
                if (!CollectionUtils.isEmpty(passed)) {
                    for (String passedKey : passed.keySet()) {
                        redisTemplate.opsForValue().decrement(passedKey);
                    }
                }
                throw new LimitException(rule.exceptionMsg());
            }
            passed.put(key, rule);
        }

        return methodInvocation.proceed();
    }

    private void sortRule(Rule[] rules) {
        Arrays.sort(rules, (o1, o2) -> o1.order() - o2.order());
    }

    public Boolean acquire(String key, long expire, long maxCount, TimeUnit timeUnit) {
        long time;
        switch (timeUnit) {
            case SECONDS:
                time = TimeUnit.MILLISECONDS.convert(expire, TimeUnit.SECONDS);
                break;
            case MINUTES:
                time = TimeUnit.MILLISECONDS.convert(expire, TimeUnit.MINUTES);
                break;
            case HOURS:
                time = TimeUnit.MILLISECONDS.convert(expire, TimeUnit.HOURS);
                break;
            case DAYS:
                time = TimeUnit.MILLISECONDS.convert(expire, TimeUnit.DAYS);
                break;
            default:
                time = expire;
                break;
        }
        return redisTemplate.execute(redisScript, Collections.singletonList(key), String.valueOf(time), String.valueOf(maxCount));
    }
}
