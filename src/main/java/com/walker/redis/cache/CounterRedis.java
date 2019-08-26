package com.walker.redis.cache;

import com.walker.redis.dto.CounterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 计数器
 *
 * @author walker
 * @date 2019/8/26
 */
@Component
public class CounterRedis {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 存储计数
     *
     * @param name
     * @param count
     */
    @SuppressWarnings("unchecked")
    public void updateCounter(String name, long count) {
        int[] precision = {1, 5, 60, 300, 3600, 18000, 86400};
        long now = System.currentTimeMillis() / 1000;

        SessionCallback<Object> session = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for (int prec : precision) {
                    // 取得当前时间片的开始时间
                    int pnow = (int) (now / prec) * prec;
                    String hash = String.format("%s:%s", prec, name);
                    operations.opsForZSet().add("known:", hash, 0);
                    operations.opsForHash().increment("count:" + hash, pnow, count);
                }
                return operations.exec();
            }
        };
    }

    /**
     * 获取计数
     *
     * @param name
     * @param precision
     * @return
     */
    public List<CounterDTO> getCounter(String name, String precision) {
        String hash = String.format("%s:%s", precision, name);
        Map<Object, Object> params = redisTemplate.opsForHash().entries("count:" + hash);

        List<CounterDTO> counters = new ArrayList<>();
        params.forEach((key, value) -> {
            CounterDTO dto = new CounterDTO();
            dto.setKey((Integer) key);
            dto.setValue((Integer) value);
            counters.add(dto);
        });
        counters.sort(Comparator.comparingInt(CounterDTO::getKey));

        return counters;
    }

    public void cleanCounters() {
        redisTemplate.multi();
        long passes = 0;

        while (true) {
            long start = System.currentTimeMillis();
            int index = 0;
            while (index < redisTemplate.opsForZSet().zCard("known:")) {
                Set<Object> hash = redisTemplate.opsForZSet().range("known:", index, index);
                index += 1;
            }
        }
    }
}
