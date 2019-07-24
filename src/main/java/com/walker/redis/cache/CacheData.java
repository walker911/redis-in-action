package com.walker.redis.cache;

import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author walker
 * @date 2019/7/24
 */
@Component
public class CacheData {

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheData(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void scheduleRowCache(String rowId, long delay) {
        // 先设置缓存行的延迟值
        redisTemplate.opsForZSet().add("delay:", rowId, delay);
        long timestamp = System.currentTimeMillis();
        // 立即对需要缓存的数据进行调度
        redisTemplate.opsForZSet().add("schedule:", rowId, timestamp);
    }

    public void cacheRows() throws InterruptedException {
        while (true) {
            Set<ZSetOperations.TypedTuple<Object>> next = redisTemplate.opsForZSet().rangeWithScores("schedule:", 0, 0);
            long now = System.currentTimeMillis();

            if (null == next) {
                TimeUnit.MILLISECONDS.sleep(50);
                continue;
            }

            ZSetOperations.TypedTuple[] typedTuples = new ZSetOperations.TypedTuple[next.size()];
            next.toArray(typedTuples);
            Double time = typedTuples[0].getScore();
            Object rowId = typedTuples[0].getValue();

            if (null == time || time > now) {
                TimeUnit.MILLISECONDS.sleep(50);
                continue;
            }

            Double delay = redisTemplate.opsForZSet().score("delay:", rowId);
            if (null == delay || delay <= 0) {
                // 不必缓存这个行, 从缓存中移除
                redisTemplate.opsForZSet().remove("delay:", rowId);
                redisTemplate.opsForZSet().remove("schedule:", rowId);
                redisTemplate.delete("inv:" + rowId);
                continue;
            }

            // 从数据库读取数据行
            Object row = Inventory.get(rowId);
            redisTemplate.opsForZSet().add("schedule:", rowId, now + delay);
            redisTemplate.opsForValue().set("inv:" + rowId, JSON.toJSONString(row));
        }
    }
}
