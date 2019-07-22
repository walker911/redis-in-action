package com.walker.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisInActionApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void contextLoads() {
        Boolean result = redisTemplate.opsForZSet().add("time:", "article:3", System.currentTimeMillis());
        System.out.println(result);
    }

}
