package com.walker.redis.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author walker
 * @date 2019/8/23
 */
@Component
public class InventoryRedis {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public boolean sellItem(String itemId, String sellerId, Double price) {
        String inventory = String.format("inventory:%s", sellerId);
        String item = String.format("%s.%s", itemId, sellerId);
        long end = System.currentTimeMillis() + 5000;

        // 开启事务
        redisTemplate.setEnableTransactionSupport(true);
        while (System.currentTimeMillis() < end) {
            try {
                // 监视用户包裹的变化
                redisTemplate.watch(inventory);
                Boolean isMember = redisTemplate.opsForSet().isMember(inventory, itemId);
                if (isMember == null || !isMember) {
                    redisTemplate.unwatch();
                    return false;
                }

                redisTemplate.multi();
                redisTemplate.opsForZSet().add("market:", item, price);
                redisTemplate.opsForSet().remove(inventory, itemId);
                redisTemplate.exec();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public boolean purchaseItem(String buyerId, String itemId, String sellerId, Double price) {
        String buyer = String.format("user:%s", buyerId);
        String seller = String.format("user:%s", sellerId);
        String item = String.format("%s.%s", itemId, sellerId);
        String inventory = String.format("inventory:%s", buyerId);
        long end = System.currentTimeMillis() + 10000;

        redisTemplate.setEnableTransactionSupport(true);
        while (System.currentTimeMillis() < end) {
            try {
                // 监视市场和买家
                redisTemplate.watch(Arrays.asList("market:", buyer));

                // 检查价格
                Double sellerPrice = redisTemplate.opsForZSet().score("market:", item);
                Object obj = redisTemplate.opsForHash().get(buyer, "funds");
                Double funds = Double.parseDouble(String.valueOf(obj));
                if (!price.equals(sellerPrice) || price > funds) {
                    redisTemplate.unwatch();
                    return false;
                }

                redisTemplate.multi();
                redisTemplate.opsForHash().increment(seller, "funds", price);
                redisTemplate.opsForHash().increment(buyer, "funds", -price);
                redisTemplate.opsForSet().add(inventory, itemId);
                redisTemplate.opsForZSet().remove("market", item);
                redisTemplate.exec();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
