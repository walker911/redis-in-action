package com.walker.redis.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author walker
 * @date 2019/7/24
 */
@RestController
public class CartController {

    private final RedisTemplate<String, Object> redisTemplate;

    public CartController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String addToCart(String session, String item, Long count) {
        // 从购物车里面移除指定的商品
        if (count <= 0) {
            redisTemplate.opsForHash().delete("cart:" + session, item);
        } else {
            redisTemplate.opsForHash().put("cart:" + session, item, count);
        }
        return "SUCCESS";
    }
}
