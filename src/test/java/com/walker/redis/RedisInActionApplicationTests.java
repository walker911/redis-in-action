package com.walker.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

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

    /**
     * 测试没有事务
     *
     * @throws InterruptedException
     */
    @Test
    public void noTransTest() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                try {
                    noTrans();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    private void noTrans() throws InterruptedException {
        System.out.println(redisTemplate.opsForValue().increment("notrans:"));
        TimeUnit.MILLISECONDS.sleep(100);
        redisTemplate.opsForValue().increment("notrans:", -1);
    }

    /**
     * 测试有事务
     *
     * @throws InterruptedException
     */
    @Test
    public void transTest() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                try {
                    trans();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    private void trans() throws InterruptedException {
        // 开启事务
        redisTemplate.setEnableTransactionSupport(true);

        redisTemplate.multi();
        redisTemplate.opsForValue().increment("trans:");
        TimeUnit.MILLISECONDS.sleep(100);
        redisTemplate.opsForValue().increment("trans:", -1);
        System.out.println(redisTemplate.exec().get(0));
    }

    @SuppressWarnings("unchecked")
    private void sessionCallback() {
        SessionCallback<Object> callback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForValue().increment("trans:");
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                redisTemplate.opsForValue().increment("trans:", -1);
                return operations.exec();
            }
        };

        System.out.println(redisTemplate.execute(callback));
    }
}
