package com.walker.redis.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.*;

/**
 * @author walker
 * @date 2019/8/27
 */
@Component
public class StatisticsRedis {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<Object> updateStats(String context, String type, Double value) {
        String destination = String.format("%s:%s", context, type);
        String startKey = destination + ":start";

        redisTemplate.setEnableTransactionSupport(true);
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            redisTemplate.watch(startKey);
            LocalTime time = LocalTime.now();
            int hour = time.getHour();
            Object existing = redisTemplate.opsForValue().get(startKey);

            redisTemplate.multi();
            // 归档
            boolean isLessNow = Integer.parseInt(String.valueOf(existing)) < hour;
            if (existing != null && isLessNow) {
                redisTemplate.rename(destination, destination + ":last");
                redisTemplate.rename(startKey, destination + ":pstart");
                redisTemplate.opsForValue().set(startKey, hour);
            }

            // 将值添加到临时键
            String key1 = UUID.randomUUID().toString().replaceAll("-", "");
            String key2 = UUID.randomUUID().toString().replaceAll("-", "");
            redisTemplate.opsForZSet().add(key1, "min", value);
            redisTemplate.opsForZSet().add(key2, "max", value);

            // 使用聚合函数
            redisTemplate.opsForZSet().unionAndStore(destination, Collections.singleton(key1), destination,
                    RedisZSetCommands.Aggregate.MIN);
            redisTemplate.opsForZSet().unionAndStore(destination, Collections.singleton(key2), destination,
                    RedisZSetCommands.Aggregate.MAX);
            // 删除临时键
            List<String> deleteKeys = new ArrayList<>(Arrays.asList(key1, key2));
            redisTemplate.delete(deleteKeys);
            // 对成员进行更新
            redisTemplate.opsForZSet().incrementScore(destination, "count", 1);
            redisTemplate.opsForZSet().incrementScore(destination, "sum", value);
            redisTemplate.opsForZSet().incrementScore(destination, "sump", value * value);

            return redisTemplate.exec();
        }

        return null;
    }
}
