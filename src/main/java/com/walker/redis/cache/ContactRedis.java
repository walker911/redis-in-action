package com.walker.redis.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author walker
 * @date 2019/8/28
 */
@Component
public class ContactRedis {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void addOrUpdateContact(String user, String contact) {
        String acList = String.format("recent:%s", user);

        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.multi();
        // 若存在，则移除
        redisTemplate.opsForList().remove(acList, 1, contact);
        // 添加到头部
        redisTemplate.opsForList().leftPush(acList, contact);
        // 保留前100个
        redisTemplate.opsForList().trim(acList, 0, 99);
        redisTemplate.exec();
    }

    public void removeContact(String user, String contact) {
        redisTemplate.opsForList().remove(user, 1, contact);
    }

    public List<String> fetchAutocomplete(String user, String prefix) {
        List<Object> candidates = redisTemplate.opsForList().range("recent:" + user, 0, -1);
        if (candidates == null) {
            throw new IllegalArgumentException();
        }

        List<String> matches = candidates.stream().filter(candidate -> String.valueOf(candidate).startsWith(prefix))
                .map(String::valueOf).collect(Collectors.toList());

        return matches;
    }

    public String findPrefixRange(String prefix) {
        String validCharacters = "`abcdefghijklmnopqrstuvwxyz{";
        int position = validCharacters.indexOf(prefix.charAt(0));
        String suffix = validCharacters.substring(position, position + 1);

        return prefix.substring(0, prefix.length() - 1) + suffix + "{";
    }

    public void autocompleteOnPrefix(String guild, String prefix) {
        // 计算查找范围起点和终点
        String start = findPrefixRange(prefix);
        String end = prefix + "{";
        String identifier = UUID.randomUUID().toString().replaceAll("-", "");
        start = start + identifier;
        end = end + identifier;
        String zsetName = "members:" + guild;

        // 插入
        redisTemplate.opsForZSet().add(zsetName, start, 0);
        redisTemplate.opsForZSet().add(zsetName, end, 0);

        redisTemplate.setEnableTransactionSupport(true);
        while (true) {
            redisTemplate.watch(zsetName);
            // 找到两个插入元素的排名
            Long sindex = redisTemplate.opsForZSet().rank(zsetName, start);
            Long eindex = redisTemplate.opsForZSet().rank(zsetName, end);
            long erange = Math.min(sindex + 9, eindex - 2);

            redisTemplate.multi();
            redisTemplate.opsForZSet().remove(zsetName, start, end);
            redisTemplate.opsForZSet().range(zsetName, sindex, erange);
            redisTemplate.exec();
        }
    }
}
