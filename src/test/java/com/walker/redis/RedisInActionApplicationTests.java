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
        redisTemplate.opsForValue().set("hello", "world");
        String result = redisTemplate.opsForValue().get("hello");
        System.out.println(result);
    }

}
