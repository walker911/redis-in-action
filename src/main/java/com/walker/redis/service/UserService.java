package com.walker.redis.service;

import com.walker.redis.model.User;

/**
 * <p>
 *
 * </p>
 *
 * @author mu qin
 * @date 2020/1/7
 */
public interface UserService {

    User saveUser(User user);

    User getUser(Long id);

    void deleteUser(Long id);
}
