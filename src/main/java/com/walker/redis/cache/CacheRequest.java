package com.walker.redis.cache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author walker
 * @date 2019/7/24
 */
@Component
public class CacheRequest {

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheRequest(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 网页缓存
     *
     * @param request
     * @return
     */
    public Object cacheRequest(String request) {
        if (!canCache(request)) {
            return callback(request);
        }

        String pageKey = "cache:" + request;
        Object content = redisTemplate.opsForValue().get(pageKey);
        if (Objects.isNull(content)) {
            content = callback(request);
            redisTemplate.opsForValue().set(pageKey, content, 300, TimeUnit.SECONDS);
        }

        return content;
    }

    /**
     * 调整商品浏览次数
     *
     * @throws InterruptedException
     */
    public void rescaleViewed() throws InterruptedException {
        while (true) {
            // 删除20000名之后的商品
            redisTemplate.opsForZSet().removeRange("viewed:", 0, -20001);
            // 将浏览次数降低为原来的一半
            List<String> otherKeys = new ArrayList<>();
            otherKeys.add("viewed:");
            redisTemplate.opsForZSet().intersectAndStore("viewed:", otherKeys, "viewed:",
                    RedisZSetCommands.Aggregate.MAX, RedisZSetCommands.Weights.of(0.5));
            TimeUnit.SECONDS.sleep(300);
        }
    }

    /**
     * 是否缓存
     *
     * @param request
     * @return
     */
    private boolean canCache(String request) {
        // 商品ID
        String itemId = extractItemId(request);
        // 是否缓存以及是否商品页面
        if (StringUtils.isBlank(itemId) || isDynamic(request)) {
            return false;
        }
        // 商品排名
        Long rank = redisTemplate.opsForZSet().rank("viewed:", itemId);
        // 根据浏览次数排名是否缓存
        return Objects.nonNull(rank) && rank < 10000;
    }

    private boolean isDynamic(String request) {
        return false;
    }

    private String extractItemId(String request) {
        if (StringUtils.isBlank(request)) {
            return null;
        }
        return request.substring(1);
    }

    private String callback(String request) {
        return "";
    }
}
