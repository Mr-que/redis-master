package com.aqwx.seckill.config;

import com.aqwx.seckill.model.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisLockConfiguration {

    @Autowired
    private RedisTemplate redisTemplate;

    @Bean
    public RedisLock redisLocck(){
        RedisLock redisLock = new RedisLock(redisTemplate);
        return redisLock;
    }


}
