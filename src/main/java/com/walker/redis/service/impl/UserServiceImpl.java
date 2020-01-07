package com.walker.redis.service.impl;

import com.walker.redis.model.User;
import com.walker.redis.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * <p>
 *
 * </p>
 *
 * @author mu qin
 * @date 2020/1/7
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "userCache")
public class UserServiceImpl implements UserService {

    private static final String USER_UID_PREFIX = "'userCache:'+";

    @Override
    @CachePut(key = USER_UID_PREFIX + "T(String).valueOf(#user.uid)")
    public User saveUser(User user) {
        log.info("user: save to redis");
        return user;
    }

    @Override
    @Cacheable(key = USER_UID_PREFIX + "T(String).valueOf(#id)")
    public User getUser(Long id) {
        log.info("user: is null");
        return null;
    }

    @Override
    @CacheEvict(key = USER_UID_PREFIX + "T(String).valueOf(#id)")
    public void deleteUser(Long id) {
        log.info("delete user: {}", id);
    }
}
