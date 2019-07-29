package com.walker.redis.publish;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

/**
 * @author walker
 * @date 2019/7/26
 */
public class PublishAndSubscribe {

    public static void main(String[] args) {
        RedisClient client = RedisClient.create("redis://localhost");
        RedisPubSubCommands<String, String> connection = client.connectPubSub().sync();
        connection.getStatefulConnection().addListener(new RedisPubSubListener<String, String>() {
            @Override
            public void message(String s, String s2) {

            }

            @Override
            public void message(String s, String k1, String s2) {

            }

            @Override
            public void subscribed(String s, long l) {

            }

            @Override
            public void psubscribed(String s, long l) {

            }

            @Override
            public void unsubscribed(String s, long l) {
                System.out.println(s);
            }

            @Override
            public void punsubscribed(String s, long l) {

            }
        });
        connection.subscribe("channel");
    }

    class Publisher implements Runnable {

        @Override
        public void run() {

        }
    }
}
