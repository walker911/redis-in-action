package com.walker.redis.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
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
        // 尝试获取信号量
        redisTemplate.opsForZSet().add(semname, identifier, now);

        // 检查是否成功取得了信号量
        redisTemplate.opsForZSet().rank(semname, identifier);
        List<Object> objects = redisTemplate.exec();
        if (Integer.parseInt(String.valueOf(objects.get(objects.size() - 1))) < limit) {
            return identifier;
        }

        // 获取信号量失败，删除之前添加的标识
        redisTemplate.opsForZSet().remove(semname, identifier);
        return null;
    }

    public Long releaseSemaphore(String semname, String identifier) {
        return redisTemplate.opsForZSet().remove(semname, identifier);
    }

    public String acquireFairSemaphore(String semname, int limit, int timeout) {
        String identifier = UUID.randomUUID().toString();
        String czset = semname + ":owner";
        String ctr = semname + ":counter";

        long now = System.currentTimeMillis();
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.multi();

        // 删除超时的信号量
        redisTemplate.opsForZSet().removeRangeByScore(semname, 0, now - timeout);
        redisTemplate.opsForZSet().intersectAndStore(czset, semname, czset);

        redisTemplate.opsForValue().increment(ctr);
        List<Object> objects = redisTemplate.exec();
        int counter = Integer.parseInt(String.valueOf(objects.get(objects.size() - 1)));

        // 尝试获取信号量
        redisTemplate.opsForZSet().add(semname, identifier, now);
        redisTemplate.opsForZSet().add(czset, identifier, counter);

        // 通过检查排名来判断客户端是否取得了信号量
        redisTemplate.opsForZSet().rank(czset, identifier);
        List<Object> results = redisTemplate.exec();
        int result = Integer.parseInt(String.valueOf(results.get(results.size() - 1)));
        if (result < limit) {
            return identifier;
        }

        // 未取得信号量，清理数据
        redisTemplate.opsForZSet().remove(semname, identifier);
        redisTemplate.opsForZSet().remove(czset, identifier);
        redisTemplate.exec();

        return null;
    }

    public Object releaseFairSemaphore(String semname, String identifier) {
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.multi();
        redisTemplate.opsForZSet().remove(semname, identifier);
        redisTemplate.opsForZSet().remove(semname + ":owner", identifier);
        return redisTemplate.exec().get(0);
    }
}
