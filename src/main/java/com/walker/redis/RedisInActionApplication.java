package com.walker.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class RedisInActionApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisInActionApplication.class, args);
    }

}
