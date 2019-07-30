package com.walker.redis.publish;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.util.concurrent.TimeUnit;

/**
 * @author walker
 * @date 2019/7/26
 */
public class PublishAndSubscribe {

    public static void main(String[] args) {
        Thread thread = new Thread(new Publisher());
        thread.start();

        RedisClient client = RedisClient.create("redis://localhost");
        RedisPubSubCommands<String, String> connection = client.connectPubSub().sync();
        connection.getStatefulConnection().addListener(new RedisPubSubListener<String, String>() {
            @Override
            public void message(String s, String s2) {
                System.out.println("message|" + s + "|" + s2);
            }

            @Override
            public void message(String s, String k1, String s2) {
            }

            @Override
            public void subscribed(String s, long l) {
                System.out.println("s|" + s + "|" + l);
            }

            @Override
            public void psubscribed(String s, long l) {
            }

            @Override
            public void unsubscribed(String s, long l) {
                System.out.println("un" + s + "|" + l);
            }

            @Override
            public void punsubscribed(String s, long l) {
            }
        });
        connection.subscribe("channel");
    }

    static class Publisher implements Runnable {

        @Override
        public void run() {
            RedisClient client = RedisClient.create("redis://localhost");
            RedisPubSubCommands<String, String> connection = client.connectPubSub().sync();

            for (int i = 0; i < 10; i++) {
                connection.publish("channel", String.valueOf(i));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
