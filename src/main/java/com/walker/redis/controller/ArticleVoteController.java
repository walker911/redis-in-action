package com.walker.redis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author walker
 * @date 2019/7/17
 */
@RestController
public class ArticleVoteController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/vote")
    public String vote() {

        return "SUCCESS";
    }
}
