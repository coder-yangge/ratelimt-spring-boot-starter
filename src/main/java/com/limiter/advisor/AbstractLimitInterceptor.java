package com.limiter.advisor;

import com.limiter.annotation.Key;
import com.limiter.annotation.Limit;
import com.limiter.annotation.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;

/**
 * @author yangge
 * @version 1.0.0
 * @title: AbstractLimitInterceptor
 * @date 2020/9/11 9:43
 */
@Slf4j
public abstract class AbstractLimitInterceptor {


    protected ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();


    protected ExpressionParser parser = new SpelExpressionParser();

    /**
     * 组装key
     * @param limit Limit
     * @param method 方法
     * @param arguments 入参
     * @return
     */
    protected StringBuilder getKey(Limit limit, Method method, Object[] arguments) {
        StringBuilder finalKey = new StringBuilder();
        String[] keys = limit.key();
        String prefix = limit.prefix();
        String userId = limit.userId();
        String separator = limit.separator();
        boolean existUser = false;
        boolean existKey = false;
        Parameter[] parameters = method.getParameters();
        if (StringUtils.isNotBlank(prefix)) {
            finalKey.append(prefix).append(separator);
        }
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                // resolve user
                Object argument = arguments[i];
                Class<?> type = parameter.getType();
                if (parameter.isAnnotationPresent(User.class)) {
                    String s = supportAndGet(type, argument);
                    if (StringUtils.isBlank(s)) {
                        throw new RuntimeException("Limit of User is not be null");
                    }
                    finalKey.append(s).append(separator);
                    existUser = true;
                }
                // resolve key
                if (parameter.isAnnotationPresent(Key.class)) {
                    String s = supportAndGet(type, argument);
                    if (StringUtils.isBlank(s)) {
                        throw new RuntimeException("Limit of key is not be null");
                    }
                    finalKey.append(s).append(separator);
                    existKey = true;
                }
            }
        }

        EvaluationContext context = new MethodBasedEvaluationContext(null, method, arguments, nameDiscoverer);
        //@User not  exist
        if (!existUser && StringUtils.isNotBlank(userId)) {
            Object objKey = parser.parseExpression(userId).getValue(context);
            String s = ObjectUtils.nullSafeToString(objKey);
            finalKey.append(s).append(separator);
        }

        // @Key not exist
        if (!existKey) {
            if (keys == null || keys.length < 0) {
                finalKey.append(method.getDeclaringClass().getSimpleName() + method.getName());
            } else {
                for (String key : keys) {
                    if (StringUtils.isNotBlank(key)) {
                        try {
                            Object objKey = parser.parseExpression(key).getValue(context);
                            String s = ObjectUtils.nullSafeToString(objKey);
                            finalKey.append(s).append(separator);
                        } catch (EvaluationException e) {
                            log.error("Method {} parse key {} exception", method.getName(), key);
                            throw e;
                        } catch (ParseException e) {
                            log.error("Method {} parse key {} exception", method.getName(), key);
                            throw e;
                        }
                    }
                }
            }
        }
        return finalKey;
    }

    String supportAndGet(Class<?> type, Object args) {
        if (args instanceof Integer) {
            return String.valueOf(args);
        }
        if (args instanceof Long) {
            return String.valueOf(args);
        }
        if (args instanceof Float) {
            return String.valueOf(args);
        }
        if (args instanceof Double) {
            return String.valueOf(args);
        }
        if (args instanceof String) {
            return (String)args;
        }
        if (args instanceof Character) {
            return String.valueOf(args);
        }
        if (args instanceof Boolean) {
            return String.valueOf(args);
        }
        if (args instanceof BigDecimal) {
           return args.toString();
        }
        return args.toString();
    }

}
