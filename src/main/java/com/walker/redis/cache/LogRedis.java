package com.walker.redis.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * redis 记录日志
 *
 * @author walker
 * @date 2019/8/26
 */
@Component
public class LogRedis {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 最新日志
     *
     * @param name
     * @param severity
     * @param message
     */
    @SuppressWarnings("unchecked")
    public void recentLog(String name, String severity, String message) {
        String destination = String.format("recent:%s:%s", name, severity);
        String log = System.currentTimeMillis() + " " + message;

        SessionCallback<Object> callback = new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                operations.multi();
                operations.opsForList().leftPush((K) destination, (V) log);
                operations.opsForList().trim((K) destination, 0, 99);
                return operations.exec();
            }
        };

        redisTemplate.execute(callback);
    }

    /**
     * 常见日志
     *
     * @param name
     * @param message
     * @param severity
     */
    public void commonLog(String name, String message, String severity) {
        String destination = String.format("common:%s:%s", name, severity);
        String startKey = destination + ":start";

        redisTemplate.setEnableTransactionSupport(true);
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            try {
                redisTemplate.watch(startKey);
                LocalTime time = LocalTime.now();
                int hour = time.getHour();
                Object existing = redisTemplate.opsForValue().get(startKey);

                redisTemplate.multi();
                // 旧日志归档
                boolean isLessNow = Integer.parseInt(String.valueOf(existing)) < hour;
                if (existing != null && isLessNow) {
                    redisTemplate.rename(destination, destination + ":last");
                    redisTemplate.rename(startKey, destination + ":pstart");
                }

                redisTemplate.opsForZSet().incrementScore(destination, message, 1);
                recentLog(name, message, severity);

                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
