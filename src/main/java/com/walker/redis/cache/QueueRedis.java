package com.walker.redis.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.walker.redis.dto.EmailDTO;
import com.walker.redis.dto.TaskDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author walker
 * @date 2019/9/6
 */
@Component
public class QueueRedis {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private LockRedis lockRedis;

    public void sendSoldEmailViaQueue(EmailDTO email) {
        // 将待发送邮件推入队列里面
        redisTemplate.opsForList().rightPush("queue:email", JSON.toJSONString(email));
    }

    public void processSoldEmailQueue() {
        while (true) {
            Object packed = redisTemplate.opsForList().leftPop("queue:email", 30, TimeUnit.SECONDS);
            if (packed == null) {
                continue;
            }
            EmailDTO toSend = JSON.parseObject(String.valueOf(packed), EmailDTO.class);

            fetchDataAndSendSoldEmail(toSend);
        }
    }

    private void fetchDataAndSendSoldEmail(EmailDTO toSend) {
    }

    public void workWatchQueue(String queue, Class<?> clazz, Callback callback) {
        while (true) {
            Object packed = redisTemplate.opsForList().leftPop(queue, 30, TimeUnit.SECONDS);
            if (packed == null) {
                continue;
            }

            Object object = JSON.parseObject(String.valueOf(packed), clazz);
            List<String> callbacks = new ArrayList<>();
            if (!callbacks.contains(object)) {
                continue;
            }
            callback.process();
        }
    }

    public String executeLater(String queue, String name, String args, int delay) {
        String identifier = UUID.randomUUID().toString();
        JSONObject object = new JSONObject();
        object.put("identifier", identifier);
        object.put("queue", queue);
        object.put("name", name);
        object.put("args", args);

        if (delay > 0) {
            redisTemplate.opsForZSet().add("delayed:", JSON.toJSONString(object), System.currentTimeMillis() + delay);
        } else {
            redisTemplate.opsForList().rightPush("queue:" + queue, JSON.toJSONString(object));
        }

        return identifier;
    }

    public void pollQueue() throws InterruptedException {
        while (true) {
            Set<Object> objects = redisTemplate.opsForZSet().range("delayed:", 0, 0);

            // 队列没有包含任何任务
            if (objects == null || objects.isEmpty()) {
                Thread.sleep(100);
                continue;
            }

            TaskDTO dto = (TaskDTO) objects.iterator().next();
            long now = System.currentTimeMillis();
            // 任务的执行时间未到
            if (dto.getTime() > now) {
                Thread.sleep(100);
                continue;
            }

            // 解码任务，确定放入哪个任务队列
            String task = dto.getTask();
            JSONObject object = JSON.parseObject(task);

            // 尝试获取锁
            String locked = lockRedis.acquireLock(object.getString("identifier"), 10);
            if (StringUtils.isBlank(locked)) {
                continue;
            }

            // 将任务放入适当的队列
            Long isRemove = redisTemplate.opsForZSet().remove("delayed:", task);
            if (isRemove != null && isRemove == 1) {
                redisTemplate.opsForList().rightPush("queue:" + object.getString("queue"), isRemove);
            }

            // 释放锁
            lockRedis.releaseLock(object.getString("identifier"), locked);
        }
    }
}
