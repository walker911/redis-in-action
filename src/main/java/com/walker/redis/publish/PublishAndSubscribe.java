package com.walker.redis.publish;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;

/**
 * @author walker
 * @date 2019/7/26
 */
public class PublishAndSubscribe {

    public static void main(String[] args) {
        RedisClient client = RedisClient.create("redis://localhost");
        StatefulRedisConnection<String, String> connection = client.connect();
        RedisStringCommands<String, String> sync = connection.sync();
        sync.append("test", "1");
        System.out.println(sync.get("test"));
    }
}
