package com.walker.redis.controller;

import com.walker.redis.common.ProjectConstant;
import com.walker.redis.common.ResultResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author walker
 * @date 2019/7/23
 */
@RestController
public class TokenController {

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/check/token")
    public ResultResponse checkToken(String token) {
        return ResultResponse.success(redisTemplate.opsForHash().get("login:", token));
    }

    @PostMapping("/update/token")
    public ResultResponse updateToken(String token, String user, String item) {
        long timestamp = System.currentTimeMillis();
        redisTemplate.opsForHash().put("login:", token, user);
        redisTemplate.opsForZSet().add("recent:", token, timestamp);
        if (StringUtils.isNotBlank(item)) {
            redisTemplate.opsForList().leftPush("viewed:" + token, item);
            redisTemplate.opsForZSet().add("viewed:" + token, item, timestamp);
            // 移除旧的记录, 只保留最近浏览的25条
            redisTemplate.opsForList().trim("viewed:" + token, 0, 25);
            redisTemplate.opsForZSet().removeRange("viewed:" + token, 0, -26);
            // 记录商品浏览次数
            redisTemplate.opsForZSet().incrementScore("viewed:", item, -1);
        }
        return ResultResponse.success();
    }

    /**
     * 清理旧会话
     *
     * @throws InterruptedException
     */
    public void cleanSessions() throws InterruptedException {
        while (true) {
            Long size = redisTemplate.opsForZSet().zCard("recent:");
            if (Objects.isNull(size)) {
                return;
            }
            if (size < ProjectConstant.LIMIT) {
                TimeUnit.SECONDS.sleep(1);
                continue;
            }
            long endIndex = Math.min(size - ProjectConstant.LIMIT, 100);
            Set<Object> tokens = redisTemplate.opsForZSet().range("recent:", 0, endIndex - 1);
            if (tokens == null) {
                return;
            }
            List<String> sessionKeys = new ArrayList<>();
            tokens.forEach(token -> {
                sessionKeys.add("viewed:" + token);
                // 用于删除购物车
                sessionKeys.add("cart:" + token);
            });
            // 移除最旧的那些令牌
            redisTemplate.delete(sessionKeys);
            redisTemplate.opsForHash().delete("login:", sessionKeys.toArray());
            redisTemplate.opsForZSet().remove("recent:", sessionKeys.toArray());
        }
    }
}
