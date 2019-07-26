package com.walker.redis.publish;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author walker
 * @date 2019/7/26
 */
public class Publisher {

    public void publish(RedisTemplate<String, Object> redisTemplate, int size) throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        for (int i = 0; i < size; i++) {
            redisTemplate.convertAndSend("channel", i);
        }
    }
}
