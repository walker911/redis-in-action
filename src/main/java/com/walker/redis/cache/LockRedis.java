package com.walker.redis.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author walker
 * @date 2019/9/4
 */
@Component
public class LockRedis {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public String acquireLock(String lockName, int timeout) {
        String identifier = UUID.randomUUID().toString();
        long endTime = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < endTime) {
            Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock:" + lockName, identifier, 10, TimeUnit.SECONDS);
            if (flag != null && flag) {
                return identifier;
            }
        }
        return null;
    }

    public boolean releaseLock(String lockName, String identifier) {
        lockName = "lock:" + lockName;

        redisTemplate.setEnableTransactionSupport(true);
        while (true) {
            try {
                redisTemplate.watch(lockName);

                String result = String.valueOf(redisTemplate.opsForValue().get(lockName));
                if (identifier.equals(result)) {
                    redisTemplate.multi();
                    redisTemplate.delete(lockName);
                    redisTemplate.exec();

                    return true;
                }

                redisTemplate.unwatch();
                break;
            } catch (Exception e) {
                // 有其它客户端修改了锁，重试
                e.printStackTrace();
            }
        }
        return false;
    }
}
