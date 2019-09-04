package com.walker.redis.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author walker
 * @date 2019/9/4
 */
@Component
public class SemaphoreRedis {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public String acquireSemaphore(String semname, int limit, int timeout) {
        String identifier = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.multi();
        // 清理过期信号量持有者
        redisTemplate.opsForZSet().removeRangeByScore(semname, 0, now - timeout);
        return null;
    }
}
