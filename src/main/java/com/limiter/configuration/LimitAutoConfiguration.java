package com.limiter.configuration;

import com.limiter.advisor.LimitAnnotationAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author yangge
 * @version 1.0.0
 * @date 2020/9/10 14:34
 */
@Configuration
public class LimitAutoConfiguration {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Bean
    public LimitAnnotationAdvisor limitAdvisor() {
        LimitAnnotationAdvisor advisor = new LimitAnnotationAdvisor(redisTemplate);
        return advisor;
    }
}
